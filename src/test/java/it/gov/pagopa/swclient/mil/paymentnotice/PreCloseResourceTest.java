package it.gov.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentNoticeService;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreCloseResourceTest {

	@InjectMock
	@RestClient
	MilRestService milRestService;

	@InjectMock
	PaymentTransactionRepository paymentTransactionRepository;

	@InjectMock
	PaymentNoticeService paymentNoticeService;

	PreCloseRequest preCloseRequest;

	PreCloseRequest abortRequest;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> validMilHeaders;

	int tokens = 3;

	@BeforeAll
	void createTestObjects() {

		// common headers
		validMilHeaders = PaymentTestData.getMilHeaders(true, true);

		// acquirer PSP configuration
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		preCloseRequest = PaymentTestData.getPreCloseRequest(true, tokens);

		abortRequest = PaymentTestData.getPreCloseRequest(false, tokens);

	}

	@Test
	void testPreClose_201() {

		final Map<String, Notice> noticeMap = getNoticeMap(preCloseRequest.getPaymentTokens(),
				preCloseRequest.getPaymentTokens().size());

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(noticeMap));

		Mockito
				.when(paymentTransactionRepository.persist(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().item(new PaymentTransactionEntity()));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertTrue(response.getHeader("Location") != null &&
				response.getHeader("Location").endsWith("/payments/" + preCloseRequest.getTransactionId()));


		// check redis integration
		ArgumentCaptor<List<String>> captorPaymentToken = ArgumentCaptor.forClass(List.class);

		Mockito.verify(paymentNoticeService).mget(captorPaymentToken.capture());
		List<String> capturedPaymentTokens = captorPaymentToken.getValue();
		Assertions.assertEquals(tokens, capturedPaymentTokens.stream().distinct().filter(noticeMap::containsKey).toList().size());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		Mockito.verify(paymentTransactionRepository).persist(captorEntity.capture());
		Assertions.assertEquals(preCloseRequest.getTransactionId(), captorEntity.getValue().transactionId);
		Assertions.assertEquals(preCloseRequest.getTransactionId(), captorEntity.getValue().paymentTransaction.getTransactionId());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"), captorEntity.getValue().paymentTransaction.getAcquirerId());
		Assertions.assertEquals(validMilHeaders.get("Channel"), captorEntity.getValue().paymentTransaction.getChannel());
		Assertions.assertEquals(validMilHeaders.get("MerchantId"), captorEntity.getValue().paymentTransaction.getMerchantId());
		Assertions.assertEquals(validMilHeaders.get("TerminalId"), captorEntity.getValue().paymentTransaction.getTerminalId());
		Assertions.assertNotNull(captorEntity.getValue().paymentTransaction.getInsertTimestamp());
		Assertions.assertEquals(preCloseRequest.getFee(), captorEntity.getValue().paymentTransaction.getFee());
		Assertions.assertEquals(noticeMap.values().stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L),
				captorEntity.getValue().paymentTransaction.getTotalAmount());
		Assertions.assertEquals(PaymentTransactionStatus.PRE_CLOSE.name(), captorEntity.getValue().paymentTransaction.getStatus());

		Assertions.assertEquals(noticeMap.size(), captorEntity.getValue().paymentTransaction.getNotices().size());

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testPreClose_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.body(abortRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#providePreCloseRequestValidationErrorCases")
	void testPreClose_400_invalidRequest(PreCloseRequest preCloseRequest, String errorCode) {

		if (ErrorCode.ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE.equals(errorCode)) {

			final Map<String, Notice> noticeMap = getNoticeMap(preCloseRequest.getPaymentTokens(),
					preCloseRequest.getPaymentTokens().size());

			Mockito
					.when(paymentNoticeService.mget(Mockito.any()))
					.thenReturn(Uni.createFrom().item(noticeMap));
		}

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	}

	@Test
	void testPreClose_400_emptyRequest() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.PRE_CLOSE_REQUEST_MUST_NOT_BE_EMPTY));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	}

	@Test
	void testPreClose_400_noticesNotFound() {

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(getNoticeMap(preCloseRequest.getPaymentTokens(),
						preCloseRequest.getPaymentTokens().size()-1)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.CACHED_NOTICE_NOT_FOUND));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}

	@Test
	void testPreClose_409_transactionAlreadyExists() {

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(getNoticeMap(preCloseRequest.getPaymentTokens(),
						preCloseRequest.getPaymentTokens().size())));

		Mockito
				.when(paymentTransactionRepository.persist(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_DUPLICATED_KEY)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(409, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@Test
	void testPreClose_500_redisErrorRead() {

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.REDIS_TIMEOUT_EXCEPTION)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}

	@Test
	void testPreClose_500_dbErrorWrite() {

		final Map<String, Notice> noticeMap = getNoticeMap(preCloseRequest.getPaymentTokens(),
				preCloseRequest.getPaymentTokens().size());

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(noticeMap));

		Mockito
				.when(paymentTransactionRepository.persist(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(preCloseRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_DATA_IN_DB));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}

	@Test
	void testAbort_201() {

		final Map<String, Notice> noticeMap = getNoticeMap(abortRequest.getPaymentTokens(),
				abortRequest.getPaymentTokens().size());

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(noticeMap));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(abortRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));

		// check milRestService client integration
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"), captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"), captorAcquirerId.getValue());

		// check redis integration
		ArgumentCaptor<List<String>> captorPaymentToken = ArgumentCaptor.forClass(List.class);

		Mockito.verify(paymentNoticeService).mget(captorPaymentToken.capture());
		final List<String> capturedPaymentTokens = captorPaymentToken.getValue();
		Assertions.assertEquals(noticeMap.size(), capturedPaymentTokens.stream().distinct().filter(noticeMap::containsKey).toList().size());

	}


	@Test
	void testAbort_201_noNotices() {

		final Map<String, Notice> noticeMap = getNoticeMap(abortRequest.getPaymentTokens(), 0);

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(paymentNoticeService.mget(Mockito.any()))
				.thenReturn(Uni.createFrom().item(noticeMap));


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(abortRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));

		// check milRestService client integration
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"), captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"), captorAcquirerId.getValue());

		// check redis integration
		ArgumentCaptor<List<String>> captorPaymentToken = ArgumentCaptor.forClass(List.class);

		Mockito.verify(paymentNoticeService).mget(captorPaymentToken.capture());
		final List<String> capturedPaymentTokens = captorPaymentToken.getValue();
		Assertions.assertEquals(noticeMap.size(), capturedPaymentTokens.stream().distinct().filter(noticeMap::containsKey).toList().size());

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testAbort_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.body(abortRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));

	}

	private Map<String, Notice> getNoticeMap(List<String> paymentTokens, int size) {
		Map<String, Notice> noticeMap = new HashMap<>();
		int i = 1;
		for (String paymentToken : paymentTokens) {
			if (i > size) {
				noticeMap.put(paymentToken, null);
			}
			else {
				Notice notice = PaymentTestData.getNotice(paymentToken);
				noticeMap.put(paymentToken, notice);
			}
			i++;
		}
		return noticeMap;
	}
}
