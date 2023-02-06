package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;

import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Payment;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendPaymentResultTest {
	
	final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String TRANSACTION_ID		= "517a4216840E461fB011036A0fd134E1";
	
	@InjectMock
	PaymentService paymentService;

	ReceivePaymentStatusRequest paymentStatus;

	@BeforeAll
	void createTestObjects() {

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

	}


	@Test
	void testGetPaymentStatus_200() {

		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().item(paymentStatus));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
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
		
		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().nullItem());
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());

	}
	
	
	@Test
	void testGetSendPaymentResult_500_RedisError() {
		
		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.REDIS_ERROR_WHILE_RETRIEVING_PAYMENT_RESULT));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paymentDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("payments"));
	}
	

	@Test
	void testReceivePaymentStatus_200() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any(ReceivePaymentStatusRequest.class)))
				.thenReturn(Uni.createFrom().voidItem());

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.body(paymentStatus)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
	}
	
	@Test
	void testPostSendPaymentResult_500_RedisError() {
		
		
		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().failure(() -> new RuntimeException()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}
	
}
