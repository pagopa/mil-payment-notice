package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.Assertions;
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
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Payments;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.RedisPaymentService;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ClosePaymentResource;

@QuarkusTest
@TestHTTPEndpoint(ClosePaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendPaymentResultTest {
	
	private final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	private final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	private final static String TRANSACTION_ID		= "517a4216840E461fB011036A0fd134E1";
	
	@InjectMock
	private RedisPaymentService redisPaymentService;
	
	@Test
	void testGetSendPaymentResult_200() throws ParseException, DatatypeConfigurationException {
		
		PaymentBody paymentBody = new PaymentBody();
		paymentBody.setOutcome(Outcome.OK.toString());
		paymentBody.setPaymentDate("2022-11-12T08:53:55");
		Payments payments = new Payments();
		payments.setCompany("ASL Roma");
		payments.setCreditorReferenceId("4839d50603fssfW5X");
		payments.setDebtor("Mario Rossi");
		payments.setDescription("Health ticket for chest x-ray");
		payments.setFiscalCode("15376371009");
		payments.setOffice("Ufficio di Roma");
		payments.setPaymentToken("648fhg36s95jfg7DS");
		List<Payments> paymentList = new ArrayList<>();
		paymentList.add(payments);
		paymentBody.setPayments(paymentList);
		Mockito.when(redisPaymentService.get(TRANSACTION_ID))
			.thenReturn(Uni.createFrom().item(paymentBody)); 
		
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
				.get("/payments/{transactionId}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	     
	}
	
	@Test
	void testGetSendPaymentResult_notFound() throws ParseException, DatatypeConfigurationException {
		
		Mockito.when(redisPaymentService.get(TRANSACTION_ID))
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
				.get("/payments/{transactionId}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(404, response.statusCode());
	        Assertions.assertEquals("{\"errors\":[\"008000023\"]}", response.getBody().asString());
	}
	
	
	@Test
	void testGetSendPaymentResult_500() throws ParseException, DatatypeConfigurationException {
		
		Mockito.when(redisPaymentService.get(TRANSACTION_ID))
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
				.get("/payments/{transactionId}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(500, response.statusCode());
	        Assertions.assertEquals("{\"errors\":[\"008000022\"]}", response.getBody().asString());
	}
	
	
	//POST
	@Test
	void testPostSendPaymentResult_200() throws ParseException, DatatypeConfigurationException {
		
		
		Mockito.when(redisPaymentService.set(Mockito.any(String.class), Mockito.any(PaymentBody.class)))
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
				.when()
				.post("/payments/{transactionId}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
	}
	
	@Test
	void testPostSendPaymentResult_500() throws ParseException, DatatypeConfigurationException {
		
		
		Mockito.when(redisPaymentService.set(Mockito.any(String.class), Mockito.any()))
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
				.post("/payments/{transactionId}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(500, response.statusCode());
	        Assertions.assertEquals("{\"errors\":[\"008000024\"]}", response.getBody().asString());
	}
	
}
