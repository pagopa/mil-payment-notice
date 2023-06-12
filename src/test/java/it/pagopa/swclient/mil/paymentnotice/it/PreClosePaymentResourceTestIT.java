package it.pagopa.swclient.mil.paymentnotice.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.net.impl.TrustAllTrustManager;
import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.bean.PaymentTransactionOutcome;
import it.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.pagopa.swclient.mil.paymentnotice.util.KafkaUtils;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreClosePaymentResourceTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(PreClosePaymentResourceTestIT.class);

	DevServicesContext devServicesContext;

	Map<String, String> validMilHeaders;

	JedisPool jedisPool;

	MongoClient mongoClient;

	CodecRegistry pojoCodecRegistry;

	KafkaConsumer<String, PaymentTransaction> paymentTransactionConsumer;

	List<String> paymentTokens;

	List<Notice> notices;

	long totalAmount;

	String existingTransactionId;

	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}

	@BeforeAll
	void createTestObjects() {

		validMilHeaders = PaymentTestData.getMilHeaders(true, true);

		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);

		// initializing redis client
		String redisExposedPort = devServicesContext.devServicesProperties().get("test.redis.exposed-port");
		String password = devServicesContext.devServicesProperties().get("test.redis.password");
		boolean tlsEnabled = BooleanUtils.toBoolean(devServicesContext.devServicesProperties().get("test.redis.tls"));
		String redisURI = "redis" + (tlsEnabled ? "s" : "") + "://:" + password + "@127.0.0.1:" + redisExposedPort;
		if (tlsEnabled) {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[] { TrustAllTrustManager.INSTANCE }, null);
				jedisPool = new JedisPool(redisURI, sslContext.getSocketFactory(), null, (hostname, session) -> true);
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				logger.error("Error while initializing sslContext for jedis", e);
			}
		} else {
			jedisPool = new JedisPool(redisURI);
		}

		// store notice data in cache
		String paymentToken = RandomStringUtils.random(32, true, true);
		Notice notice = PaymentTestData.getNotice(paymentToken);

		try (Jedis jedis = jedisPool.getResource()) {
			jedis.set(paymentToken, new ObjectMapper().writeValueAsString(notice));
		} catch (JsonProcessingException e) {
			logger.error("Error while saving payment notice in redis", e);
		}

		paymentTokens = List.of(paymentToken);

		notices = List.of(notice);

		totalAmount = notice.getAmount();

		// store existing transaction on DB
		existingTransactionId = RandomStringUtils.random(32, true, true);
		PaymentTransactionEntity existingTransactionEntity =
				PaymentTestData.getPaymentTransaction(existingTransactionId, PaymentTransactionStatus.CLOSED, validMilHeaders, 1, null);

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		collection.insertMany(List.of(existingTransactionEntity));

		paymentTransactionConsumer = KafkaUtils.getKafkaConsumer(devServicesContext, PaymentTransaction.class);

	}



	@AfterAll
	void destroyTestObjects() {

		try {
			mongoClient.close();
		} catch (Exception e){
			logger.error("Error while closing mongo client", e);
		}

		try {
			jedisPool.destroy();
		} catch (Exception e){
			logger.error("Error while destroying Jedis pool", e);
		}

		try {
			paymentTransactionConsumer.unsubscribe();
			paymentTransactionConsumer.close();
		} catch (Exception e){
			logger.error("Error while closing kafka consumer", e);
		}
	}

	PreCloseRequest getPreCloseRequest(String transactionId, PaymentTransactionOutcome outcome, List<String> tokens,
									   long totalAmount, boolean hasPreset) {

		PreCloseRequest preCloseRequest = new PreCloseRequest();
		preCloseRequest.setOutcome(outcome.name());
		preCloseRequest.setTransactionId(transactionId);
		preCloseRequest.setPaymentTokens(tokens);
		if (outcome.equals(PaymentTransactionOutcome.PRE_CLOSE)) {
			preCloseRequest.setTotalAmount(totalAmount);
			preCloseRequest.setFee(100L);
		}

		if (hasPreset) preCloseRequest.setPreset(PaymentTestData.getPreset());

		return preCloseRequest;
	}

	@Test
	void testPreClose_201() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.PRE_CLOSE, paymentTokens, totalAmount, true))
				.when()
				.post("/")
				.then()
				.extract()
				.response();


		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + transactionId));

		// check transaction written on DB
		PaymentTransaction dbPaymentTransaction = checkDatabaseData(transactionId);

		// check transaction sent to topic
		Instant start = Instant.now();
		ConsumerRecords<String, PaymentTransaction> records = paymentTransactionConsumer.poll(Duration.ofSeconds(10));
		paymentTransactionConsumer.commitSync();
		logger.info("Finished polling in {} seconds, found {} records", Duration.between(start, Instant.now()), records.count());
		Assertions.assertEquals(1, records.count());

		PaymentTransaction topicPaymentTransaction = records.iterator().next().value();
		Assertions.assertEquals(dbPaymentTransaction.getTransactionId(), topicPaymentTransaction.getTransactionId());
		Assertions.assertEquals(dbPaymentTransaction.getStatus(), topicPaymentTransaction.getStatus());
		Assertions.assertEquals(dbPaymentTransaction.getPreset().getPresetId(), topicPaymentTransaction.getPreset().getPresetId());
		Assertions.assertEquals(dbPaymentTransaction.getPreset().getSubscriberId(), topicPaymentTransaction.getPreset().getSubscriberId());

	}

	private PaymentTransaction checkDatabaseData(String transactionId) {

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		Bson filter = Filters.in("_id", transactionId);
		FindIterable<PaymentTransactionEntity> documents  = collection.find(filter);

		PaymentTransaction paymentTransaction;

		try (MongoCursor<PaymentTransactionEntity> iterator = documents.iterator()) {
			Assertions.assertTrue(iterator.hasNext());
			PaymentTransactionEntity paymentTransactionEntity = iterator.next();

			logger.info("Found transaction on DB: {}", paymentTransactionEntity.paymentTransaction);

			paymentTransaction = paymentTransactionEntity.paymentTransaction;

			Assertions.assertEquals(transactionId, paymentTransaction.getTransactionId());
			Assertions.assertEquals(validMilHeaders.get("AcquirerId"), paymentTransaction.getAcquirerId());
			Assertions.assertEquals(validMilHeaders.get("Channel"), paymentTransaction.getChannel());
			Assertions.assertEquals(validMilHeaders.get("MerchantId"), paymentTransaction.getMerchantId());
			Assertions.assertEquals(validMilHeaders.get("TerminalId"), paymentTransaction.getTerminalId());
			Assertions.assertNotNull(paymentTransaction.getInsertTimestamp());
			Assertions.assertEquals(paymentTokens.size(), paymentTransaction.getNotices().size());
			validateNotices(notices, paymentTransaction.getNotices());
			Assertions.assertEquals(totalAmount, paymentTransaction.getTotalAmount());
			Assertions.assertEquals(100L, paymentTransaction.getFee());
			Assertions.assertEquals(PaymentTransactionStatus.PRE_CLOSE.name(), paymentTransaction.getStatus());
			Assertions.assertNull(paymentTransaction.getPaymentMethod());
			Assertions.assertNull(paymentTransaction.getPaymentTimestamp());
			Assertions.assertNull(paymentTransaction.getCloseTimestamp());
			Assertions.assertNull(paymentTransaction.getPaymentDate());
			Assertions.assertNull(paymentTransaction.getCallbackTimestamp());

		}

		return paymentTransaction;
	}

	private void validateNotices(List<Notice> cachedNotices, List<Notice> returnedNotices) {
		for (Notice returnedNotice: returnedNotices) {
			Optional<Notice> optCachedNotice = cachedNotices.stream()
					.filter(n -> n.getPaymentToken().equals(returnedNotice.getPaymentToken()))
					.findAny();
			Assertions.assertTrue(optCachedNotice.isPresent());
			Assertions.assertEquals(optCachedNotice.get().getPaTaxCode(), returnedNotice.getPaTaxCode());
			Assertions.assertEquals(optCachedNotice.get().getNoticeNumber(), returnedNotice.getNoticeNumber());
			Assertions.assertEquals(optCachedNotice.get().getAmount(), returnedNotice.getAmount());
			Assertions.assertEquals(optCachedNotice.get().getDescription(), returnedNotice.getDescription());
			Assertions.assertNull(returnedNotice.getCreditorReferenceId());
			Assertions.assertNull(returnedNotice.getDebtor());
		}
	}

	@Test
	void testPreClose_400_amountNotMatch() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, true))
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.PRE_CLOSE, paymentTokens, totalAmount-10, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE));

	}

	@Test
	void testPreClose_400_noticeNotFound() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, true))
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.PRE_CLOSE, List.of(transactionId), totalAmount, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.CACHED_NOTICE_NOT_FOUND));

	}

	@Test
	void testPreClose_409_transactionAlreadyExists() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, true))
				.and()
				.body(getPreCloseRequest(existingTransactionId, PaymentTransactionOutcome.PRE_CLOSE, paymentTokens, totalAmount, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(409, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}


	@Test
	void testAbort_201() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, true))
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.ABORT, paymentTokens, totalAmount, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));

	}

	@Test
	void testAbort_201_noticeNotFound() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, true))
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.ABORT, paymentTokens, totalAmount, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(201, response.statusCode());

	}

	@Test
	void testAbort_500_unknownAcquirer() {

		String transactionId = RandomStringUtils.random(32, true, true);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, false))
				.and()
				.body(getPreCloseRequest(transactionId, PaymentTransactionOutcome.ABORT, paymentTokens, totalAmount, false))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));

	}

}
