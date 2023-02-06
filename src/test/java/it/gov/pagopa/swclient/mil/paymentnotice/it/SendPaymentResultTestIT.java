package it.gov.pagopa.swclient.mil.paymentnotice.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Payment;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendPaymentResultTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(SendPaymentResultTestIT.class);

	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";


	ReceivePaymentStatusRequest paymentStatus;

	DevServicesContext devServicesContext;

	JedisPool jedisPool;

	String transactionId;

	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}


	@BeforeAll
	void createTestObjects() {

		String redisExposedPort = devServicesContext.devServicesProperties().get("test.redis.exposed-port");
		jedisPool = new JedisPool("127.0.0.1", Integer.parseInt(redisExposedPort));

		transactionId = UUID.randomUUID().toString().replaceAll("-", "");

		Payment payment = new Payment();
		payment.setCompany("ASL Roma");
		payment.setCreditorReferenceId("4839d50603fssfW5X");
		payment.setDebtor("Mario Rossi");
		payment.setDescription("Health ticket for chest x-ray");
		payment.setFiscalCode("15376371009");
		payment.setOffice("Ufficio di Roma");
		payment.setPaymentToken("648fhg36s95jfg7DS");
		List<Payment> paymentList = new ArrayList<>();
		paymentList.add(payment);

		paymentStatus = new ReceivePaymentStatusRequest();
		paymentStatus.setOutcome(Outcome.OK.toString());
		paymentStatus.setPaymentDate("2022-11-12T08:53:55");
		paymentStatus.setPayments(paymentList);

		try (Jedis jedis = jedisPool.getResource()) {
			jedis.set(transactionId, new ObjectMapper().writeValueAsString(paymentStatus));
		} catch (JsonProcessingException e) {
			logger.error("Error while saving payment transaction in redis", e);
		}
	}


	@Test
	void testGetPaymentStatus_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(paymentStatus.getPaymentDate(), response.jsonPath().getString("paymentDate"));
		Assertions.assertNotNull(response.jsonPath().getJsonObject("payments"));
		Assertions.assertEquals(1, response.jsonPath().getList("payments").size());
		for (int i = 0; i < response.jsonPath().getList("payments").size(); i++) {
			Payment payment = response.jsonPath().getList("payments", Payment.class).get(i);
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getCompany(), payment.getCompany());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getCreditorReferenceId(), payment.getCreditorReferenceId());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getDebtor(), payment.getDebtor());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getDescription(), payment.getDescription());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getFiscalCode(), payment.getFiscalCode());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getOffice(), payment.getOffice());
			Assertions.assertEquals(paymentStatus.getPayments().get(i).getPaymentToken(), payment.getPaymentToken());
		}
	}
	
	@Test
	void testGetPaymentStatus_404_transactionNotFound()  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", UUID.randomUUID().toString().replaceAll("-", ""))
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());

	}
	
	
//	@Test
//	void testGetSendPaymentResult_500_RedisError() {
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(
//						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
//						"Version", API_VERSION,
//						"AcquirerId", "4585625",
//						"Channel", "ATM",
//						"TerminalId", "0aB9wXyZ",
//						"SessionId", SESSION_ID)
//				.and()
//				.pathParam("transactionId", UUID.randomUUID().toString())
//				.when()
//				.get("/{transactionId}")
//				.then()
//				.extract()
//				.response();
//
//		Assertions.assertEquals(500, response.statusCode());
//		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.REDIS_ERROR_WHILE_RETRIEVING_PAYMENT_RESULT));
//		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
//		Assertions.assertNull(response.jsonPath().getJsonObject("paymentDate"));
//		Assertions.assertNull(response.jsonPath().getJsonObject("payments"));
//	}
	

	@Test
	void testReceivePaymentStatus_200() {

		String receivedTransactionId = UUID.randomUUID().toString().replaceAll("-", "");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", receivedTransactionId)
				.body(paymentStatus)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// test data on redis
		try (Jedis jedis = jedisPool.getResource()) {
			String redisPaymentTransaction = jedis.get(transactionId);
			Assertions.assertNotNull(redisPaymentTransaction);
			ReceivePaymentStatusRequest storedPaymentStatus =
					new ObjectMapper().readValue(redisPaymentTransaction, ReceivePaymentStatusRequest.class);
			logger.info("stored payment transaction status -> {}", storedPaymentStatus);
			Assertions.assertEquals(paymentStatus.getOutcome(), storedPaymentStatus.getOutcome());
			Assertions.assertEquals(paymentStatus.getPaymentDate(), storedPaymentStatus.getPaymentDate());
			for (Payment storedPayment : storedPaymentStatus.getPayments()) {
				Optional<Payment> optPayment = paymentStatus.getPayments()
						.stream().filter(p -> p.getPaymentToken().equals(storedPayment.getPaymentToken())).findFirst();
				Assertions.assertTrue(optPayment.isPresent());
				if (optPayment.isPresent()) {
					Payment requestPayment = optPayment.get();
					Assertions.assertEquals(requestPayment.getCompany(), storedPayment.getCompany());
					Assertions.assertEquals(requestPayment.getCreditorReferenceId(), storedPayment.getCreditorReferenceId());
					Assertions.assertEquals(requestPayment.getDebtor(), storedPayment.getDebtor());
					Assertions.assertEquals(requestPayment.getDescription(), storedPayment.getDescription());
					Assertions.assertEquals(requestPayment.getFiscalCode(), storedPayment.getFiscalCode());
					Assertions.assertEquals(requestPayment.getOffice(), storedPayment.getOffice());
					Assertions.assertEquals(requestPayment.getPaymentToken(), storedPayment.getPaymentToken());
				}
			}

		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}


//	@Test
//	void testPostSendPaymentResult_500_RedisError() {
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(
//						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
//						"Version", API_VERSION,
//						"AcquirerId", "4585625",
//						"Channel", "ATM",
//						"TerminalId", "0aB9wXyZ",
//						"SessionId", SESSION_ID)
//				.and()
//				.pathParam("transactionId", UUID.randomUUID().toString())
//				.when()
//				.post("/{transactionId}")
//				.then()
//				.extract()
//				.response();
//
//		Assertions.assertEquals(500, response.statusCode());
//		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT));
//		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
//
//	}
	
}
