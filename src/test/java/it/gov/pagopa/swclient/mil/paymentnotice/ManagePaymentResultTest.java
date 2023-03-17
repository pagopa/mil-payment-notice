package it.gov.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Payment;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentService;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.ws.rs.InternalServerErrorException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ManagePaymentResultTest {

	final static String TRANSACTION_ID		= "517a4216840E461fB011036A0fd134E1";
	
	@InjectMock
	PaymentService paymentService;

	ReceivePaymentStatusRequest paymentStatus;

	Map<String, String> commonHeaders;

	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = PaymentTestData.getMilHeaders(true, true);

		Payment payment = new Payment();
		payment.setCompany("ASL Roma");
		payment.setCreditorReferenceId("4839d50603fssfW5X");
		payment.setDebtor("Mario Rossi");
		payment.setDescription("Health ticket for chest x-ray");
		payment.setFiscalCode("15376371009");
		payment.setOffice("Ufficio di Roma");
		payment.setPaymentToken("648fhg36s95jfg7DS");

		paymentStatus = new ReceivePaymentStatusRequest();
		paymentStatus.setOutcome(Outcome.OK.toString());
		paymentStatus.setPaymentDate("2022-11-12T08:53:55");
		paymentStatus.setPayments(List.of(payment));

	}


	@Test
	void testGetPaymentStatus_200() {

		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().item(paymentStatus));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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

		// TODO add check of clients
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testGetPaymentStatus_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode)  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@Test
	void testGetPaymentStatus_400_invalidPathParam()  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", "abc_")
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@Test
	void testGetPaymentStatus_404_transactionNotFound()  {
		
		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().nullItem());
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
	void testGetPaymentStatus_404_outcomeNotFound()  {
		
		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().item(new ReceivePaymentStatusRequest()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
	void testGetPaymentResult_500_RedisErrorGet() {
		
		Mockito
				.when(paymentService.get(TRANSACTION_ID))
				.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paymentDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("payments"));
	}
	

	@Test
	void testReceivePaymentStatus_200() {

		Mockito
				.when(paymentService.get(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(new ReceivePaymentStatusRequest()));

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any(ReceivePaymentStatusRequest.class)))
				.thenReturn(Uni.createFrom().voidItem());

		Response response = given()
				.contentType(ContentType.JSON)
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
	void testReceivePaymentStatus_404() {

		Mockito
				.when(paymentService.get(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().nullItem());

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any(ReceivePaymentStatusRequest.class)))
				.thenReturn(Uni.createFrom().voidItem());

		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.body(paymentStatus)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals("PAYMENT_NOT_FOUND", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
	}

	@Test
	void testReceivePaymentStatus_500_RedisErrorGet() {

		Mockito
				.when(paymentService.get(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(new RuntimeException()));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}
	
	@Test
	void testReceivePaymentStatus_500_RedisErrorSet() {

		Mockito
				.when(paymentService.get(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(new ReceivePaymentStatusRequest()));

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().failure(new RuntimeException()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", TRANSACTION_ID)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_DATA_INTO_REDIS));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}
	
}
