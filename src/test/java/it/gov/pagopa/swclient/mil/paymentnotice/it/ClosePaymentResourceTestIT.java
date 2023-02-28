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
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTestIT implements DevServicesContext.ContextAware {

	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";

	PspConfEntity pspConfEntity;

	DevServicesContext devServicesContext;


	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}
	
	@BeforeAll
	void createTestObjects() {

		// PSP configuration
		PspConfiguration pspInfo = new PspConfiguration();
		pspInfo.setPspId("AGID_01");
		pspInfo.setPspBroker("97735020584");
		pspInfo.setPspPassword("pwd_AgID");

		pspConfEntity = new PspConfEntity();
		pspConfEntity.acquirerId = PaymentTestData.ACQUIRER_ID_KNOWN;
		pspConfEntity.pspConfiguration = pspInfo;

	}

	ClosePaymentRequest getClosePaymentRequest(Outcome outcome, String transactionId) {

		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");

		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		closePaymentRequest.setOutcome(outcome.name());
		closePaymentRequest.setPaymentTokens(tokens);
		closePaymentRequest.setPaymentMethod("PAGOBANCOMAT");
		closePaymentRequest.setTransactionId(transactionId);
		closePaymentRequest.setTotalAmount(BigInteger.valueOf(234234));
		closePaymentRequest.setFee(BigInteger.valueOf(897));
		closePaymentRequest.setTimestampOp("2022-11-12T08:53:55");

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
				.body(getClosePaymentRequest(Outcome.OK, PaymentTestData.PAY_TID_NODE_OK))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + PaymentTestData.PAY_TID_NODE_OK));
	    Assertions.assertNotNull(response.getHeader("Retry-after"));
	    Assertions.assertNotNull(response.getHeader("Max-Retry"));

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
				.body(getClosePaymentRequest(Outcome.OK, PaymentTestData.PAY_TID_NODE_KO))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.KO.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-after"));
		Assertions.assertNull(response.getHeader("Max-Retry"));

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
				.body(getClosePaymentRequest(Outcome.OK, paymentTransactionId))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.KO.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-after"));
		Assertions.assertNull(response.getHeader("Max-Retry"));

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
				.body(getClosePaymentRequest(Outcome.OK, paymentTransactionId))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + paymentTransactionId));
		Assertions.assertNotNull(response.getHeader("Retry-after"));
		Assertions.assertNotNull(response.getHeader("Max-Retry"));

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
				.body(getClosePaymentRequest(Outcome.OK, PaymentTestData.PAY_TID_NODE_TIMEOUT))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNotNull(response.getHeader("Location"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + PaymentTestData.PAY_TID_NODE_TIMEOUT));
		Assertions.assertNotNull(response.getHeader("Retry-after"));
		Assertions.assertNotNull(response.getHeader("Max-Retry"));
	     
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
				.body(getClosePaymentRequest(Outcome.KO, PaymentTestData.PAY_TID_NODE_KO))
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());

	}
	
}
