package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTest {
	
	final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	@InjectMock
	@RestClient
    NodeRestService nodeRestService;
	
	@InjectMock
	PspConfRepository pspConfRepository;

	@InjectMock
	PaymentService paymentService;

	PspConfEntity pspConfEntity;

	ClosePaymentRequest closePaymentRequestOK;

	ClosePaymentRequest closePaymentRequestKO;

	@BeforeAll
	void createTestObjects() {

		// PSP configuration
		PspConfiguration pspInfo = new PspConfiguration();
		pspInfo.setPspId("AGID_01");
		pspInfo.setPspBroker("97735020584");
		pspInfo.setPspPassword("pwd_AgID");

		pspConfEntity = new PspConfEntity();
		pspConfEntity.acquirerId = "4585625";
		pspConfEntity.pspConfiguration = pspInfo;

		closePaymentRequestOK = new ClosePaymentRequest();
		closePaymentRequestOK.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		closePaymentRequestOK.setPaymentTokens(tokens);
		closePaymentRequestOK.setPaymentMethod("PAGOBANCOMAT");
		closePaymentRequestOK.setTransactionId("517a4216840E461fB011036A0fd134E1");
		closePaymentRequestOK.setTotalAmount(BigInteger.valueOf(234234));
		closePaymentRequestOK.setFee(BigInteger.valueOf(897));
		closePaymentRequestOK.setTimestampOp("2022-11-12T08:53:55");

		closePaymentRequestKO = new ClosePaymentRequest();
		closePaymentRequestKO.setOutcome(Outcome.KO.toString());
		closePaymentRequestKO.setPaymentTokens(tokens);
		closePaymentRequestKO.setPaymentMethod("PAGOBANCOMAT");
		closePaymentRequestKO.setTransactionId("517a4216840E461fB011036A0fd134E1");
		closePaymentRequestKO.setTotalAmount(BigInteger.valueOf(234234));
		closePaymentRequestKO.setFee(BigInteger.valueOf(897));
		closePaymentRequestKO.setTimestampOp("2022-11-12T08:53:55");


		// node close response OK


		// node close response KO


	}

	@Test
	void testClosePayment_200_node200_OK() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));
		
		
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
				.body(closePaymentRequestOK)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + closePaymentRequestOK.getTransactionId()));
	    Assertions.assertNotNull(response.getHeader("Retry-after"));
	    Assertions.assertNotNull(response.getHeader("Max-Retry"));

	}

	@Test
	void testClosePayment_200_node200_KO() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.KO.name());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


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
				.body(closePaymentRequestOK)
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
	@ValueSource(ints = {400, 404, 422})
	void testClosePayment_200_nodeError_KO(int statusCode) {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(() -> new ClientWebApplicationException(statusCode)));


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
				.body(closePaymentRequestOK)
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
	@ValueSource(ints = {408, 500})
	void testClosePayment_200_nodeError_OK(int status) {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(() -> new ClientWebApplicationException(status)));


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
				.body(closePaymentRequestOK)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + closePaymentRequestOK.getTransactionId()));
		Assertions.assertNotNull(response.getHeader("Retry-after"));
		Assertions.assertNotNull(response.getHeader("Max-Retry"));

	}


	@Test
	void testClosePayment_200_nodeTimeout() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(new TimeoutException()));
		
		
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
				.body(closePaymentRequestOK)
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
				response.getHeader("Location").endsWith("/" + closePaymentRequestOK.getTransactionId()));
		Assertions.assertNotNull(response.getHeader("Retry-after"));
		Assertions.assertNotNull(response.getHeader("Max-Retry"));
	     
	}


	@Test
	void testClosePaymentKO_200_nodeOK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


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
				.body(closePaymentRequestKO)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());

	}
	
}
