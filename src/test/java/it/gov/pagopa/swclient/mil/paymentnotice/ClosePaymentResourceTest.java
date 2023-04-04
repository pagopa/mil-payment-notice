package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentService;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTest {

	@InjectMock
	@RestClient
    NodeRestService nodeRestService;

	@InjectMock
	@RestClient
	MilRestService milRestService;

	@InjectMock
	PaymentService paymentService;

	ClosePaymentRequest closePaymentRequestOK;

	ClosePaymentRequest closePaymentRequestKO;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> commonHeaders;


	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = PaymentTestData.getMilHeaders(true, true);

		// acquirer PSP configuration
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		closePaymentRequestOK = PaymentTestData.getClosePaymentRequest(true);

		closePaymentRequestKO = PaymentTestData.getClosePaymentRequest(false);

	}


	@Test
	void testClosePayment_200_node200_OK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
	    Assertions.assertNotNull(response.getHeader("Retry-After"));
	    Assertions.assertNotNull(response.getHeader("Max-Retries"));

		//check of milRestService clients
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);
		
		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(commonHeaders.get("RequestId"),captorRequestId.getValue());
		Assertions.assertEquals(commonHeaders.get("AcquirerId"),captorAcquirerId.getValue());
		
		//check of nodeRestService clients
		ArgumentCaptor<NodeClosePaymentRequest> captorNodeClosePaymentRequest = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
		Mockito.verify(nodeRestService).closePayment(captorNodeClosePaymentRequest.capture());

		Assertions.assertEquals(closePaymentRequestOK.getOutcome(),captorNodeClosePaymentRequest.getValue().getOutcome());
		Assertions.assertEquals("CP",captorNodeClosePaymentRequest.getValue().getPaymentMethod());
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestOK.getTimestampOp()).atZone(ZoneId.of("UTC"));
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT),captorNodeClosePaymentRequest.getValue().getTimestampOperation());
		Assertions.assertEquals(closePaymentRequestOK.getTransactionId(),captorNodeClosePaymentRequest.getValue().getTransactionId());
		Assertions.assertEquals(new BigDecimal(closePaymentRequestOK.getTotalAmount(),2),captorNodeClosePaymentRequest.getValue().getTotalAmount());
		Assertions.assertEquals(new BigDecimal(closePaymentRequestOK.getFee(),2),captorNodeClosePaymentRequest.getValue().getFee());
		//for test list of one element
		Assertions.assertEquals(closePaymentRequestOK.getPaymentTokens().get(0),captorNodeClosePaymentRequest.getValue().getPaymentTokens().get(0));
		
	}

	@Test
	void testClosePayment_200_node200_KO() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.KO.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}


	@ParameterizedTest
	@ValueSource(ints = {400, 404})
	void testClosePayment_200_nodeError_KO(int statusCode) {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		javax.ws.rs.core.Response res = javax.ws.rs.core.Response
														.status(statusCode)
														.entity("")
														.build();
		
		Mockito
			.when(nodeRestService.closePayment(Mockito.any()))
			.thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(res))); 


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}


	@ParameterizedTest
	@ValueSource(ints = {408, 422, 500})
	void testClosePayment_200_nodeError_OK(int status) {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		javax.ws.rs.core.Response res = javax.ws.rs.core.Response
																.status(status)
																.entity("")
																.build();
		
		Mockito
			.when(nodeRestService.closePayment(Mockito.any()))
			.thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(res))); 


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testClosePayment_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.post("/")
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

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideCloseRequestValidationErrorCases")
	void testClosePayment_400_invalidRequest(ClosePaymentRequest closePaymentRequest, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequest)
				.when()
				.post("/")
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
	void testClosePayment_400_emptyRequest() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.CLOSE_REQUEST_MUST_NOT_BE_EMPTY));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@Test
	void testClosePayment_500_otherCases() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(Exception::new));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_NODE_REST_SERVICES));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}
	

	@Test
	void testClosePayment_200_nodeTimeout() {

		Mockito
				.when(paymentService.set(Mockito.any(String.class), Mockito.any()))
				.thenReturn(Uni.createFrom().voidItem());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(new TimeoutException()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));
	     
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testClosePayment_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}

	@Test
	void testClosePayment_500_redisKo() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentService.setIfNotExist(Mockito.any(String.class), Mockito.any(ReceivePaymentStatusRequest.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.REDIS_TIMEOUT_EXCEPTION)));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_DATA_INTO_REDIS));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}

	@Test
	void testClosePaymentKO_200_nodeOK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());
		
		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequestKO)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());
		

//		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
//		Mockito.verify(nodeRestService).closePayment(captor.capture());
//		Assertions.assertEquals("648fhg36s95jfg7DS",captor.getValue().getPaymentTokens().get(0));
//		Assertions.assertEquals(Outcome.KO.toString(), captor.getValue().getOutcome());
//		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getPsp(), captor.getValue().getIdPsp());
//		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getBroker(), captor.getValue().getIdBrokerPSP());
//		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getChannel(), captor.getValue().getIdChannel());
//		Assertions.assertEquals(PaymentMethod.PAGOBANCOMAT.name(), captor.getValue().getPaymentMethod());
//		Assertions.assertEquals("517a4216840E461fB011036A0fd134E1", captor.getValue().getTransactionId());
//		Assertions.assertEquals(BigDecimal.valueOf(234234).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getTotalAmount());
//		Assertions.assertEquals(BigDecimal.valueOf(897).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getFee());
//		
//		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestKO.getTimestampOp()).atZone(ZoneId.of("UTC"));
//		
//		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captor.getValue().getTimestampOperation());
//		Assertions.assertNotNull(captor.getValue().getAdditionalPaymentInformations());
		
	}


	@Test
	void testClosePaymentOK_200_nodeUnparsable() {

		Mockito
			.when(paymentService.setIfNotExist(Mockito.any(String.class), Mockito.any(ReceivePaymentStatusRequest.class)))
			.thenReturn(Uni.createFrom().item(Boolean.TRUE));
		
		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		JsonParser jsonParser = null;
		try {
			jsonParser = new JsonFactory().createParser("{}");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(new JsonParseException(jsonParser,""))));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
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
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

	}
	

	
}
