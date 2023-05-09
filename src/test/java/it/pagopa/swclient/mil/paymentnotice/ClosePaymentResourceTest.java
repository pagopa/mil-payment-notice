package it.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionRepository;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.restassured.RestAssured.given;

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
    PaymentTransactionRepository paymentTransactionRepository;

	ClosePaymentRequest closePaymentRequestOK;

	ClosePaymentRequest closePaymentRequestKO;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> commonHeaders;

	PaymentTransactionEntity paymentTransactionEntity;

	String transactionId;

	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = PaymentTestData.getMilHeaders(true, true);

		// acquirer PSP configuration
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		closePaymentRequestOK = PaymentTestData.getClosePaymentRequest(true);

		closePaymentRequestKO = PaymentTestData.getClosePaymentRequest(false);

		transactionId = RandomStringUtils.random(32, true, true);

		paymentTransactionEntity = PaymentTestData.getPaymentTransaction(transactionId,
				PaymentTransactionStatus.PENDING, commonHeaders, 3);

	}


	@Test
	void testClosePayment_200_node200_OK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

        Mockito
                .when(paymentTransactionRepository.findById(Mockito.any(String.class)))
                .thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + transactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check milRestService client integration
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(commonHeaders.get("RequestId"), captorRequestId.getValue());
		Assertions.assertEquals(commonHeaders.get("AcquirerId"), captorAcquirerId.getValue());

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check node close rest service
		ArgumentCaptor<NodeClosePaymentRequest> captorNodeClosePaymentRequest = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);

		Mockito.verify(nodeRestService).closePayment(captorNodeClosePaymentRequest.capture());
		Assertions.assertEquals(Outcome.OK.name(), captorNodeClosePaymentRequest.getValue().getOutcome());
		Assertions.assertEquals("CP", captorNodeClosePaymentRequest.getValue().getPaymentMethod());
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestOK.getPaymentTimestamp()).atZone(ZoneId.of("UTC"));
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captorNodeClosePaymentRequest.getValue().getTimestampOperation());
		Assertions.assertEquals(transactionId, captorNodeClosePaymentRequest.getValue().getTransactionId());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getTotalAmount(), 2),
				captorNodeClosePaymentRequest.getValue().getTotalAmount());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getFee(), 2),
				captorNodeClosePaymentRequest.getValue().getFee());

		List<String> paymentTokens = captorNodeClosePaymentRequest.getValue().getPaymentTokens();
		Assertions.assertEquals(paymentTransactionEntity.paymentTransaction.getNotices().size(),
				paymentTransactionEntity.paymentTransaction.getNotices().stream()
						.map(Notice::getPaymentToken)
						.filter(paymentTokens::contains).distinct().toList().size());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.PENDING);

	}

	@Test
	void testClosePayment_200_node200_KO() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.KO.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
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

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.ERROR_ON_CLOSE);


	}


	@ParameterizedTest
	@ValueSource(ints = {400, 404})
	void testClosePayment_200_nodeError_KO(int statusCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getExceptionWithEntity(statusCode)));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
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

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.ERROR_ON_CLOSE);

	}


	@ParameterizedTest
	@ValueSource(ints = {408, 422, 500})
	void testClosePayment_200_nodeError_OK(int statusCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getExceptionWithEntity(statusCode)));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + transactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.PENDING);

	}

	@Test
	void testClosePayment_200_nodeUnparsable() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.UNPARSABLE_EXCEPTION)));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
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
				response.getHeader("Location").endsWith("/" + transactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.PENDING);

	}

	@Test
	void testClosePayment_200_nodeTimeout() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(new TimeoutException()));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
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
				response.getHeader("Location").endsWith("/" + transactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.PENDING);

	}

	@ParameterizedTest
	@MethodSource("it.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testClosePayment_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));
	}

	@Test
	void testClosePayment_400_invalidPathParam()  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.body(closePaymentRequestOK)
				.and()
				.pathParam("transactionId", "abc_")
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}

	@ParameterizedTest
	@MethodSource("it.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideCloseRequestValidationErrorCases")
	void testClosePayment_400_invalidRequest(ClosePaymentRequest closePaymentRequest, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequest)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));
	}

	@Test
	void testClosePayment_400_emptyRequest() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.CLOSE_REQUEST_MUST_NOT_BE_EMPTY));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));
	}

	@Test
	void testClosePayment_404_transactionNotFound() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().nullItem());


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@ParameterizedTest
	@MethodSource("it.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testClosePayment_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
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
	void testClosePayment_500_otherCases() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(Exception::new));

//		Mockito
//				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
//				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
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

		// TODO check if correct to update transaction?

//		// check DB integration - write
//		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);
//
//		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
//		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
//		validateDBUpdate(captorEntity, paymentTransaction, PaymentTransactionStatus.ERROR_ON_CLOSE);

	}


	@Test
	void testClosePayment_500_db_errorRead() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-After"));
		Assertions.assertNull(response.getHeader("Max-Retries"));

	}


	@Test
	void testClosePayment_500_db_errorWrite() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestOK)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/" + transactionId));
		Assertions.assertNotNull(response.getHeader("Retry-After"));
		Assertions.assertNotNull(response.getHeader("Max-Retries"));

	}

	@Test
	void testClosePaymentKO_200_nodeOK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestKO)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());


		// check milRestService client integration
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(commonHeaders.get("RequestId"), captorRequestId.getValue());
		Assertions.assertEquals(commonHeaders.get("AcquirerId"), captorAcquirerId.getValue());

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());
		validateDBUpdate(captorEntity.getValue(), paymentTransaction, PaymentTransactionStatus.ERROR_ON_PAYMENT);

	}

	@Test
	void testClosePaymentKO_200_dbError() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(nodeRestService.closePayment(Mockito.any(NodeClosePaymentRequest.class)))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.and()
				.body(closePaymentRequestKO)
				.when()
				.patch("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(202, response.statusCode());


		// check milRestService client integration
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(commonHeaders.get("RequestId"), captorRequestId.getValue());
		Assertions.assertEquals(commonHeaders.get("AcquirerId"), captorAcquirerId.getValue());

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
		Mockito.verify(paymentTransactionRepository, Mockito.times(2)).update(captorEntity.capture());
		for (PaymentTransactionEntity capturedEntity : captorEntity.getAllValues()) {
			validateDBUpdate(capturedEntity, paymentTransaction, PaymentTransactionStatus.ERROR_ON_PAYMENT);
		}

	}


	private void validateDBUpdate(PaymentTransactionEntity capturedEntity, PaymentTransaction paymentTransaction,
								  PaymentTransactionStatus transactionStatus) {

		Assertions.assertEquals(paymentTransactionEntity.transactionId, capturedEntity.transactionId);
		Assertions.assertEquals(paymentTransaction.getTransactionId(), capturedEntity.paymentTransaction.getTransactionId());
		Assertions.assertEquals(paymentTransaction.getAcquirerId(), capturedEntity.paymentTransaction.getAcquirerId());
		Assertions.assertEquals(paymentTransaction.getChannel(), capturedEntity.paymentTransaction.getChannel());
		Assertions.assertEquals(paymentTransaction.getMerchantId(), capturedEntity.paymentTransaction.getMerchantId());
		Assertions.assertEquals(paymentTransaction.getTerminalId(), capturedEntity.paymentTransaction.getTerminalId());
		Assertions.assertEquals(paymentTransaction.getInsertTimestamp(), capturedEntity.paymentTransaction.getInsertTimestamp());
		Assertions.assertEquals(paymentTransaction.getFee(), capturedEntity.paymentTransaction.getFee());
		Assertions.assertEquals(paymentTransaction.getTotalAmount(), capturedEntity.paymentTransaction.getTotalAmount());

		Assertions.assertEquals(transactionStatus.name(), capturedEntity.paymentTransaction.getStatus());
		Assertions.assertEquals(closePaymentRequestOK.getPaymentMethod(), capturedEntity.paymentTransaction.getPaymentMethod());
		Assertions.assertEquals(closePaymentRequestOK.getPaymentTimestamp(), capturedEntity.paymentTransaction.getPaymentTimestamp());
		Assertions.assertNotNull(capturedEntity.paymentTransaction.getCloseTimestamp());

		Assertions.assertEquals(paymentTransaction.getNotices().size(), capturedEntity.paymentTransaction.getNotices().size());
	}


}
