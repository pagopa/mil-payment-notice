package it.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import it.pagopa.swclient.mil.paymentnotice.bean.Payment;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionRepository;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.resource.UnitTestProfile;
import it.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import it.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestProfile(UnitTestProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ManagePaymentResultTest {

	static final Logger logger = LoggerFactory.getLogger(ManagePaymentResultTest.class);

	@InjectMock
	PaymentTransactionRepository paymentTransactionRepository;

	@Inject @Any
	InMemoryConnector connector;

	Map<String, String> commonHeaders;

	PaymentTransactionEntity paymentTransactionEntity;

	PaymentTransactionEntity paymentTransactionPresetEntity;

	ReceivePaymentStatusRequest paymentStatusOK;

	ReceivePaymentStatusRequest paymentStatusKO;

	String transactionId;

	String paymentDate;

	int receivedMessage = 0;

	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = PaymentTestData.getMilHeaders(true, true);

		transactionId = RandomStringUtils.random(32, true, true);

		paymentDate = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

		paymentTransactionEntity = PaymentTestData.getPaymentTransaction(transactionId,
				PaymentTransactionStatus.PENDING, commonHeaders, 3, null);

		paymentTransactionPresetEntity = PaymentTestData.getPaymentTransaction(transactionId,
				PaymentTransactionStatus.PENDING, commonHeaders, 3, PaymentTestData.getPreset());


		List<Payment> paymentList = new ArrayList<>();
		for (Notice notice : paymentTransactionEntity.paymentTransaction.getNotices()){
			Payment payment = new Payment();
			payment.setCompany(notice.getCompany());
			payment.setCreditorReferenceId("4839d50603fssfW5X");
			payment.setDebtor("Mario Rossi");
			payment.setDescription(notice.getDescription());
			payment.setFiscalCode(notice.getPaTaxCode());
			payment.setOffice(notice.getOffice());
			payment.setPaymentToken(notice.getPaymentToken());
			paymentList.add(payment);
		}

		paymentStatusOK = new ReceivePaymentStatusRequest();
		paymentStatusOK.setOutcome(Outcome.OK.name());
		paymentStatusOK.setPaymentDate(paymentDate);
		paymentStatusOK.setPayments(paymentList);

		paymentStatusKO = new ReceivePaymentStatusRequest();
		paymentStatusKO.setOutcome(Outcome.KO.name());
		paymentStatusKO.setPaymentDate(paymentDate);
		paymentStatusKO.setPayments(paymentList);

	}

	@AfterAll
	void cleanUp() {
		connector.sink("presets").clear();
	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPayments_200() {

		ReactivePanacheQuery<PaymentTransactionEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
		Mockito.when(reactivePanacheQuery.page(Mockito.any())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.withBatchSize(Mockito.anyInt())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.list()).thenReturn(Uni.createFrom().item(List.of(paymentTransactionEntity)));

		Mockito
				.when(paymentTransactionRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.any(Object[].class)))
				.thenReturn(reactivePanacheQuery);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.when()
				.get("/")
				.then()
				.extract()
				.response();


		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("transactions").size());
		Assertions.assertEquals(transactionId, response.jsonPath().getList("transactions",
				PaymentTransaction.class).get(0).getTransactionId());

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPayments_200_empty() {

		ReactivePanacheQuery<PaymentTransactionEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
		Mockito.when(reactivePanacheQuery.page(Mockito.any())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.withBatchSize(Mockito.anyInt())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.list()).thenReturn(Uni.createFrom().item(List.of()));

		Mockito
				.when(paymentTransactionRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.any(Object[].class)))
				.thenReturn(reactivePanacheQuery);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.when()
				.get("/")
				.then()
				.extract()
				.response();


		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(0, response.jsonPath().getList("transactions").size());

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPayments_500_db_errorRead() {

		ReactivePanacheQuery<PaymentTransactionEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
		Mockito.when(reactivePanacheQuery.page(Mockito.any())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.withBatchSize(Mockito.anyInt())).thenReturn(reactivePanacheQuery);
		Mockito.when(reactivePanacheQuery.list())
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));

		Mockito
				.when(paymentTransactionRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.any(Object[].class)))
				.thenReturn(reactivePanacheQuery);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.when()
				.get("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPaymentStatus_200() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		var paymentTransaction = paymentTransactionEntity.paymentTransaction;

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(transactionId, response.jsonPath().getString("transactionId"));
		Assertions.assertEquals(commonHeaders.get("AcquirerId"), response.jsonPath().getString("acquirerId"));
		Assertions.assertEquals(commonHeaders.get("Channel"), response.jsonPath().getString("channel"));
		Assertions.assertEquals(commonHeaders.get("MerchantId"), response.jsonPath().getString("merchantId"));
		Assertions.assertEquals(commonHeaders.get("TerminalId"), response.jsonPath().getString("terminalId"));
		Assertions.assertNotNull(response.jsonPath().getString("insertTimestamp"));
		Assertions.assertEquals(paymentTransaction.getTotalAmount(), response.jsonPath().getLong("totalAmount"));
		Assertions.assertEquals(paymentTransaction.getFee().longValue(), response.jsonPath().getLong("fee"));
		Assertions.assertEquals(paymentTransaction.getStatus(), response.jsonPath().getString("status"));


		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());
	}

	@ParameterizedTest
	@MethodSource("it.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPaymentStatus_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode)  {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
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

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPaymentStatus_404_transactionNotFound()  {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().nullItem());

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPaymentStatus_404_transactionMismatch()  {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Map<String, String> invalidClientHeaderMap = new HashMap<>(commonHeaders);
		invalidClientHeaderMap.put("MerchantId", "abdce");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidClientHeaderMap)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.body().asString());

	}


	@Test
	@TestSecurity(user = "testUser", roles = { "NoticePayer" })
	void testGetPaymentResult_500_db_errorRead() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.get("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentStatusOK_200() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.then(i-> Uni.createFrom().item(i.getArgument(0, PaymentTransactionEntity.class)));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", transactionId)
				.body(paymentStatusOK)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorTransactionEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		Mockito.verify(paymentTransactionRepository).update(captorTransactionEntity.capture());
		Assertions.assertEquals(PaymentTransactionStatus.CLOSED.name(), captorTransactionEntity.getValue().paymentTransaction.getStatus());
		Assertions.assertEquals(paymentDate, captorTransactionEntity.getValue().paymentTransaction.getPaymentDate());
		Assertions.assertNotNull(captorTransactionEntity.getValue().paymentTransaction.getCallbackTimestamp());
		for (Notice notice : captorTransactionEntity.getValue().paymentTransaction.getNotices()) {
			Assertions.assertEquals("4839d50603fssfW5X", notice.getCreditorReferenceId());
			Assertions.assertEquals("Mario Rossi", notice.getDebtor());
		}
	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentStatusOK_200_preset() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionPresetEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.then(i-> Uni.createFrom().item(i.getArgument(0, PaymentTransactionEntity.class)));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", transactionId)
				.body(paymentStatusOK)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check topic integration
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());

		receivedMessage++;
		InMemorySink<PaymentTransaction> presetsOut = connector.sink("presets");
		Awaitility.await().<List<? extends Message<PaymentTransaction>>>until(presetsOut::received, t -> t.size() == receivedMessage);

		PaymentTransaction message = presetsOut.received().get(receivedMessage-1).getPayload();
		logger.info("Topic message: {}", message);
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getTransactionId(), message.getTransactionId());
		Assertions.assertEquals(captorEntity.getValue().paymentTransaction.getStatus(), message.getStatus());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getPresetId(), message.getPreset().getPresetId());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getSubscriberId(), message.getPreset().getSubscriberId());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getPaTaxCode(), message.getPreset().getPaTaxCode());
	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentStatusKO_200() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.then(i-> Uni.createFrom().item(i.getArgument(0, PaymentTransactionEntity.class)));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", transactionId)
				.body(paymentStatusKO)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorTransactionEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		Mockito.verify(paymentTransactionRepository).update(captorTransactionEntity.capture());
		Assertions.assertEquals(PaymentTransactionStatus.ERROR_ON_RESULT.name(), captorTransactionEntity.getValue().paymentTransaction.getStatus());
		Assertions.assertEquals(paymentDate, captorTransactionEntity.getValue().paymentTransaction.getPaymentDate());
		Assertions.assertNotNull(captorTransactionEntity.getValue().paymentTransaction.getCallbackTimestamp());
		for (Notice notice : captorTransactionEntity.getValue().paymentTransaction.getNotices()) {
			Assertions.assertEquals("4839d50603fssfW5X", notice.getCreditorReferenceId());
			Assertions.assertEquals("Mario Rossi", notice.getDebtor());
		}
	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentStatusKO_200_preset() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionPresetEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.then(i-> Uni.createFrom().item(i.getArgument(0, PaymentTransactionEntity.class)));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", transactionId)
				.body(paymentStatusKO)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check topic integration
		ArgumentCaptor<PaymentTransactionEntity> captorEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);
		Mockito.verify(paymentTransactionRepository).update(captorEntity.capture());

		receivedMessage++;
		InMemorySink<PaymentTransaction> presetsOut = connector.sink("presets");
		Awaitility.await().<List<? extends Message<PaymentTransaction>>>until(presetsOut::received, t -> t.size() == receivedMessage);

		PaymentTransaction message = presetsOut.received().get(receivedMessage-1).getPayload();
		logger.info("Topic message: {}", message);
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getTransactionId(), message.getTransactionId());
		Assertions.assertEquals(captorEntity.getValue().paymentTransaction.getStatus(), message.getStatus());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getPresetId(), message.getPreset().getPresetId());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getSubscriberId(), message.getPreset().getSubscriberId());
		Assertions.assertEquals(paymentTransactionPresetEntity.paymentTransaction.getPreset().getPaTaxCode(), message.getPreset().getPaTaxCode());
	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentStatus_404() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().nullItem());

		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.pathParam("transactionId", transactionId)
				.body(paymentStatusOK)
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
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentResult_500_db_errorRead() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)));


		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.body(paymentStatusOK)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB));

	}

	@Test
	@TestSecurity(user = "testUser", roles = { "Nodo" })
	void testReceivePaymentResult_500_db_errorWrite() {

		Mockito
				.when(paymentTransactionRepository.findById(Mockito.anyString()))
				.thenReturn(Uni.createFrom().item(paymentTransactionEntity));

		Mockito
				.when(paymentTransactionRepository.update(Mockito.any(PaymentTransactionEntity.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(ExceptionType.DB_TIMEOUT_EXCEPTION)))
				.then(i-> Uni.createFrom().item(i.getArgument(0, PaymentTransactionEntity.class)));

		Response response = given()
				.contentType(ContentType.JSON)
				.and()
				.body(paymentStatusOK)
				.and()
				.pathParam("transactionId", transactionId)
				.when()
				.post("/{transactionId}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

		// check DB integration - read
		ArgumentCaptor<String> captorTransactionId = ArgumentCaptor.forClass(String.class);

		Mockito.verify(paymentTransactionRepository).findById(captorTransactionId.capture());
		Assertions.assertEquals(transactionId, captorTransactionId.getValue());

		// check DB integration - write
		ArgumentCaptor<PaymentTransactionEntity> captorTransactionEntity = ArgumentCaptor.forClass(PaymentTransactionEntity.class);

		Mockito.verify(paymentTransactionRepository, Mockito.times(2)).update(captorTransactionEntity.capture());
		Assertions.assertEquals(PaymentTransactionStatus.CLOSED.name(), captorTransactionEntity.getValue().paymentTransaction.getStatus());
		Assertions.assertEquals(paymentDate, captorTransactionEntity.getValue().paymentTransaction.getPaymentDate());
		Assertions.assertNotNull(captorTransactionEntity.getValue().paymentTransaction.getCallbackTimestamp());
		for (Notice notice : captorTransactionEntity.getValue().paymentTransaction.getNotices()) {
			Assertions.assertEquals("4839d50603fssfW5X", notice.getCreditorReferenceId());
			Assertions.assertEquals("Mario Rossi", notice.getDebtor());
		}

	}

}
