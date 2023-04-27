package it.gov.pagopa.swclient.mil.paymentnotice.it;

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
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentTransactionOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(ClosePaymentResourceTestIT.class);

	DevServicesContext devServicesContext;

	Map<String, String> validMilHeaders;

	MongoClient mongoClient;

	CodecRegistry pojoCodecRegistry;

	String unknownTransactionId;

	String paymentTimestamp;

	final String paymentMethod = "PAGOBANCOMAT";


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

		// store transactions on DB
		unknownTransactionId = RandomStringUtils.random(32, true, true);

		List<String> transactionIdList = List.of(
				PaymentTestData.PAY_TID_NODE_OK,
				PaymentTestData.PAY_TID_NODE_KO,
				PaymentTestData.PAY_TID_NODE_400,
				PaymentTestData.PAY_TID_NODE_404,
				PaymentTestData.PAY_TID_NODE_408,
				PaymentTestData.PAY_TID_NODE_422,
				PaymentTestData.PAY_TID_NODE_500,
				PaymentTestData.PAY_TID_NODE_UNPARSABLE,
				PaymentTestData.PAY_TID_NODE_TIMEOUT
		);

		List<PaymentTransactionEntity> paymentTransactionEntities = transactionIdList.stream()
				.map(tx -> PaymentTestData.getPaymentTransaction(tx, PaymentTransactionStatus.PRE_CLOSE, validMilHeaders, 1))
				.toList();

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		collection.insertMany(paymentTransactionEntities);

		paymentTimestamp =  LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

	}

	@AfterAll
	void destroyTestObjects() {

		try {
			mongoClient.close();
		} catch (Exception e){
			logger.error("Error while closing mongo client", e);
		}

	}

	ClosePaymentRequest getClosePaymentRequest(PaymentTransactionOutcome outcome) {

		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		closePaymentRequest.setOutcome(outcome.name());
		closePaymentRequest.setPaymentMethod(paymentMethod);
		closePaymentRequest.setPaymentTimestamp(paymentTimestamp);

		return closePaymentRequest;
	}

	@Test
	void testClosePayment_200_node200_OK() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_OK)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + PaymentTestData.PAY_TID_NODE_OK));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check transaction written on DB
		checkDatabaseData(PaymentTestData.PAY_TID_NODE_OK, PaymentTransactionStatus.PENDING);

	}

	@Test
	void testClosePayment_200_node200_KO() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_KO)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.KO.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

		// check transaction written on DB
		checkDatabaseData(PaymentTestData.PAY_TID_NODE_KO, PaymentTransactionStatus.ERROR_ON_CLOSE);

	}


	@ParameterizedTest
	@ValueSource(strings = {
			PaymentTestData.PAY_TID_NODE_400,
			PaymentTestData.PAY_TID_NODE_404})
	void testClosePayment_200_nodeError_KO(String paymentTransactionId) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", paymentTransactionId)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.KO.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

		// check transaction written on DB
		checkDatabaseData(paymentTransactionId, PaymentTransactionStatus.ERROR_ON_CLOSE);

	}


	@ParameterizedTest
	@ValueSource(strings = {
			PaymentTestData.PAY_TID_NODE_408,
			PaymentTestData.PAY_TID_NODE_500,
			PaymentTestData.PAY_TID_NODE_422,
			PaymentTestData.PAY_TID_NODE_UNPARSABLE})
	void testClosePayment_200_nodeError_OK(String paymentTransactionId) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", paymentTransactionId)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + paymentTransactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check transaction written on DB
		checkDatabaseData(paymentTransactionId, PaymentTransactionStatus.PENDING);
	}


	@Test
	void testClosePayment_200_nodeTimeout() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_TIMEOUT)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNotNull(response.getHeader("Location"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + PaymentTestData.PAY_TID_NODE_TIMEOUT));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check transaction written on DB
		checkDatabaseData(PaymentTestData.PAY_TID_NODE_TIMEOUT, PaymentTransactionStatus.PENDING);
	}

	@Test
	void testClosePayment_404_transactionNotFound() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", unknownTransactionId)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@Test
	void testClosePayment_404_transactionMismatch() {

		Map<String, String> invalidClientHeaderMap = new HashMap<>(validMilHeaders);
		invalidClientHeaderMap.put("MerchantId", "abdce");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidClientHeaderMap)
				.and()
				.pathParam("transactionId",PaymentTestData.PAY_TID_NODE_OK)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@Test
	void testClosePayment_500_unknownAcquirer() {


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(PaymentTestData.getMilHeaders(true, false))
				.and()
				.pathParam("transactionId",PaymentTestData.PAY_TID_NODE_OK)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.CLOSE))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));

	}


	@Test
	void testClosePaymentKO_200_nodeOK() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_TIMEOUT)
				.and()
				.body(getClosePaymentRequest(PaymentTransactionOutcome.ERROR_ON_PAYMENT))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());

		// check transaction written on DB
		checkDatabaseData(PaymentTestData.PAY_TID_NODE_TIMEOUT, PaymentTransactionStatus.ERROR_ON_PAYMENT);

	}

	private void checkDatabaseData(String transactionId, PaymentTransactionStatus transactionStatus) {

		MongoCollection<PaymentTransactionEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("paymentTransactions", PaymentTransactionEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		Bson filter = Filters.in("_id", transactionId);
		FindIterable<PaymentTransactionEntity> documents  = collection.find(filter);

		try (MongoCursor<PaymentTransactionEntity> iterator = documents.iterator()) {
			Assertions.assertTrue(iterator.hasNext());
			PaymentTransactionEntity paymentTransactionEntity = iterator.next();

			logger.info("Found transaction on DB: {}", paymentTransactionEntity.paymentTransaction);

			PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;

			Assertions.assertEquals(transactionId, paymentTransaction.getTransactionId());
			Assertions.assertEquals(transactionStatus.name(), paymentTransaction.getStatus());
			Assertions.assertEquals(paymentMethod, paymentTransaction.getPaymentMethod());
			Assertions.assertEquals(paymentTimestamp, paymentTransaction.getPaymentTimestamp());
			Assertions.assertNotNull(paymentTransaction.getCloseTimestamp());
			Assertions.assertNull(paymentTransaction.getPaymentDate());
			Assertions.assertNull(paymentTransaction.getCallbackTimestamp());

		}
	}

}
