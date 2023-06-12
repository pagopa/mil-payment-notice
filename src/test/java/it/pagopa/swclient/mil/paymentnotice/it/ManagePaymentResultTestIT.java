package it.pagopa.swclient.mil.paymentnotice.it;

import static io.restassured.RestAssured.given;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import it.pagopa.swclient.mil.paymentnotice.util.KafkaUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
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
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.bean.Payment;
import it.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ManagePaymentResultTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(ManagePaymentResultTestIT.class);

	DevServicesContext devServicesContext;

	Map<String, String> milHeaders;

	Map<String, String> milHeadersGetTransactions;

	MongoClient mongoClient;

	CodecRegistry pojoCodecRegistry;

	KafkaConsumer<String, PaymentTransaction> paymentTransactionConsumer;

	String closedTransactionId;

	String pendingTransactionIdOK;

	String pendingTransactionIdKO;

	Payment paymentOK;

	Payment paymentKO;

	int totalTransactions;

	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}


	@BeforeAll
	void createTestObjects() {

		milHeaders = PaymentTestData.getMilHeaders(true, true);

		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);


		// store transactions on DB
		closedTransactionId = RandomStringUtils.random(32, true, true);
		pendingTransactionIdOK = RandomStringUtils.random(32, true, true);
		pendingTransactionIdKO = RandomStringUtils.random(32, true, true);

		List<PaymentTransactionEntity> paymentTransactionEntities = new ArrayList<>();
		paymentTransactionEntities.add(PaymentTestData.getPaymentTransaction(closedTransactionId, PaymentTransactionStatus.CLOSED, milHeaders, 1, null));
		PaymentTransactionEntity transactionOK = PaymentTestData.getPaymentTransaction(pendingTransactionIdOK, PaymentTransactionStatus.PENDING, milHeaders, 1, PaymentTestData.getPreset());
		paymentTransactionEntities.add(transactionOK);
		PaymentTransactionEntity transactionKO = PaymentTestData.getPaymentTransaction(pendingTransactionIdKO, PaymentTransactionStatus.PENDING, milHeaders, 1, PaymentTestData.getPreset());
		paymentTransactionEntities.add(transactionKO);

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		collection.insertMany(paymentTransactionEntities);

		paymentOK = new Payment();
		paymentOK.setCompany("ASL Roma");
		paymentOK.setCreditorReferenceId("4839d50603fssfW5X");
		paymentOK.setDebtor("Mario Rossi");
		paymentOK.setDescription("Health ticket for chest x-ray");
		paymentOK.setFiscalCode("15376371009");
		paymentOK.setOffice("Ufficio di Roma");
		paymentOK.setPaymentToken(transactionOK.paymentTransaction.getNotices().get(0).getPaymentToken());

		paymentKO = new Payment();
		paymentKO.setCompany("ASL Roma");
		paymentKO.setCreditorReferenceId("4839d50603fssfW5Y");
		paymentKO.setDebtor("Mario Verdi");
		paymentKO.setDescription("Health ticket for chest x-ray");
		paymentKO.setFiscalCode("15376371009");
		paymentKO.setOffice("Ufficio di Roma");
		paymentKO.setPaymentToken(transactionKO.paymentTransaction.getNotices().get(0).getPaymentToken());

		// storing transactions for get
		totalTransactions = RandomUtils.nextInt(5, 15);
		milHeadersGetTransactions = new HashMap<>(milHeaders);
		milHeadersGetTransactions.put("TerminalId", "0aB9wZyX");

		List<PaymentTransactionEntity> paymentTransactionGet = new ArrayList<>();
		for(int i=0; i<totalTransactions; i++) {
			String transactionId = RandomStringUtils.random(32, true, true);
			paymentTransactionGet.add(PaymentTestData.getPaymentTransaction(transactionId, PaymentTransactionStatus.CLOSED, milHeadersGetTransactions, 1, null));
		}

		collection.insertMany(paymentTransactionGet);
		logger.debug("Total documents on DB: {}", collection.countDocuments());

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
			paymentTransactionConsumer.unsubscribe();
			paymentTransactionConsumer.close();
		} catch (Exception e){
			logger.error("Error while closing kafka consumer", e);
		}

	}

	@Test
	void testGetPayments() {

		Response response = given()
				.headers(milHeadersGetTransactions)
				.when()
				.get("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertNotNull(response.jsonPath().getJsonObject("transactions"));
		Assertions.assertEquals(totalTransactions, response.jsonPath().getList("transactions").size());

	}

	@Test
	void testGetPaymentStatus_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(milHeaders)
				.and()
				.pathParam("transactionId", closedTransactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(PaymentTransactionStatus.CLOSED.name(), response.jsonPath().getString("status"));
	}

	@Test
	void testGetPaymentStatus_404_transactionNotFound()  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(milHeaders)
				.and()
				.pathParam("transactionId", UUID.randomUUID().toString().replaceAll("-", ""))
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@Test
	void testGetPaymentStatus_404_transactionMismatch()  {

		Map<String, String> invalidClientHeaderMap = new HashMap<>(milHeaders);
		invalidClientHeaderMap.put("MerchantId", "abdce");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidClientHeaderMap)
				.and()
				.pathParam("transactionId", UUID.randomUUID().toString().replaceAll("-", ""))
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}


	@Test
	void testReceivePaymentStatus_200_paymentOK() {

		String timestamp = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

		ReceivePaymentStatusRequest paymentStatusRequest = new ReceivePaymentStatusRequest();
		paymentStatusRequest.setOutcome(Outcome.OK.name());
		paymentStatusRequest.setPaymentDate(timestamp);
		paymentStatusRequest.setPayments(List.of(paymentOK));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(milHeaders)
				.and()
				.pathParam("transactionId", pendingTransactionIdOK)
				.body(paymentStatusRequest)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check transaction written on DB
		PaymentTransaction dbPaymentTransaction = checkDatabaseData(pendingTransactionIdOK, timestamp, PaymentTransactionStatus.CLOSED, List.of(paymentOK));

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

	@Test
	void testReceivePaymentStatus_200_paymentKO() {

		String timestamp = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

		ReceivePaymentStatusRequest paymentStatusRequest = new ReceivePaymentStatusRequest();
		paymentStatusRequest.setOutcome(Outcome.KO.name());
		paymentStatusRequest.setPaymentDate(timestamp);
		paymentStatusRequest.setPayments(List.of(paymentKO));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(milHeaders)
				.and()
				.pathParam("transactionId", pendingTransactionIdKO)
				.body(paymentStatusRequest)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check transaction written on DB
		PaymentTransaction dbPaymentTransaction = checkDatabaseData(pendingTransactionIdKO, timestamp, PaymentTransactionStatus.ERROR_ON_RESULT, List.of(paymentKO));

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


	@Test
	void testReceivePaymentStatus_404() {

		String timestamp = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

		ReceivePaymentStatusRequest paymentStatusRequest = new ReceivePaymentStatusRequest();
		paymentStatusRequest.setOutcome(Outcome.OK.name());
		paymentStatusRequest.setPaymentDate(timestamp);
		paymentStatusRequest.setPayments(List.of(paymentOK));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(milHeaders)
				.and()
				.pathParam("transactionId", UUID.randomUUID().toString().replaceAll("-", ""))
				.body(paymentStatusRequest)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals("PAYMENT_NOT_FOUND", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

	}

	private PaymentTransaction checkDatabaseData(String transactionId, String paymentDate, PaymentTransactionStatus transactionStatus,
								   List<Payment> paymentList) {

		PaymentTransaction paymentTransaction;

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		Bson filter = Filters.in("_id", transactionId);
		FindIterable<PaymentTransactionEntity> documents  = collection.find(filter);

		try (MongoCursor<PaymentTransactionEntity> iterator = documents.iterator()) {
			Assertions.assertTrue(iterator.hasNext());
			PaymentTransactionEntity paymentTransactionEntity = iterator.next();

			logger.info("Found transaction on DB: {}", paymentTransactionEntity.paymentTransaction);

			paymentTransaction = paymentTransactionEntity.paymentTransaction;

			Assertions.assertEquals(transactionId, paymentTransaction.getTransactionId());
			Assertions.assertEquals(transactionStatus.name(), paymentTransaction.getStatus());
			Assertions.assertNotNull(paymentTransaction.getPaymentMethod());
			Assertions.assertNotNull(paymentTransaction.getPaymentTimestamp());
			Assertions.assertNotNull(paymentTransaction.getCloseTimestamp());
			Assertions.assertEquals(paymentDate, paymentTransaction.getPaymentDate());
			Assertions.assertNotNull(paymentTransaction.getCallbackTimestamp());

			validateNotices(paymentList, paymentTransaction.getNotices());

		}

		return paymentTransaction;
	}

	private void validateNotices(List<Payment> nodePayments, List<Notice> returnedNotices) {
		for (Notice returnedNotice: returnedNotices) {
			Optional<Payment> optNodePayment = nodePayments.stream()
					.filter(n -> n.getPaymentToken().equals(returnedNotice.getPaymentToken()))
					.findAny();
			Assertions.assertTrue(optNodePayment.isPresent());
			Assertions.assertEquals(optNodePayment.get().getCreditorReferenceId(), returnedNotice.getCreditorReferenceId());
			Assertions.assertEquals(optNodePayment.get().getDebtor(), returnedNotice.getDebtor());
		}
	}


}
