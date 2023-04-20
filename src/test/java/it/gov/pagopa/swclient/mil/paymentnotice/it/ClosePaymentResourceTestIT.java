package it.gov.pagopa.swclient.mil.paymentnotice.it;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTestIT implements DevServicesContext.ContextAware {

	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";

	DevServicesContext devServicesContext;


	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}
	
	@BeforeAll
	void createTestObjects() {

	}

	ClosePaymentRequest getClosePaymentRequest(Outcome outcome) {

		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		closePaymentRequest.setOutcome(outcome.name());
		closePaymentRequest.setPaymentMethod("PAGOBANCOMAT");
		closePaymentRequest.setPaymentTimestamp("2022-11-12T08:53:55");

		return closePaymentRequest;
	}

	@Test
	void testClosePayment_200_node200_OK() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")

				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_OK)
				.and()
				.body(getClosePaymentRequest(Outcome.OK))
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

	}

	@Test
	void testClosePayment_200_node200_KO() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_KO)
				.and()
				.body(getClosePaymentRequest(Outcome.OK))
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

	}


	@ParameterizedTest
	@ValueSource(strings = {
			PaymentTestData.PAY_TID_NODE_400,
			PaymentTestData.PAY_TID_NODE_404})
	void testClosePayment_200_nodeError_KO(String paymentTransactionId) {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", paymentTransactionId)
				.and()
				.body(getClosePaymentRequest(Outcome.OK))
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
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", paymentTransactionId)
				.and()
				.body(getClosePaymentRequest(Outcome.OK))
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

	}


	@Test
	void testClosePayment_200_nodeTimeout() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_TIMEOUT)
				.and()
				.body(getClosePaymentRequest(Outcome.OK))
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
	     
	}


	@Test
	void testClosePaymentKO_200_nodeOK() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", PaymentTestData.ACQUIRER_ID_KNOWN,
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.pathParam("transactionId", PaymentTestData.PAY_TID_NODE_TIMEOUT)
				.and()
				.body(getClosePaymentRequest(Outcome.KO))
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());

	}
	
}
