/**
 * 
 */
package it.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AdditionalPaymentInformations;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.resource.AsyncClosePaymentProcessor;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsyncClosePaymentProcessorTest {

	@Inject
    AsyncClosePaymentProcessor asyncClosePaymentProcessor;

	@InjectMock
	@RestClient
	NodeRestService nodeRestService;

	AcquirerConfiguration acquirerConfiguration;

	ClosePaymentRequest closePaymentRequestKO;

	Map<String, String> commonHeaders;

	PaymentTransactionEntity paymentTransactionEntity;

	String transactionId;

	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = PaymentTestData.getMilHeaders(true, true);

		// acquirer PSP configuration
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		closePaymentRequestKO = PaymentTestData.getClosePaymentRequest(false);

		transactionId = RandomStringUtils.random(32, true, true);

		paymentTransactionEntity = PaymentTestData.getPaymentTransaction(transactionId,
				PaymentTransactionStatus.PENDING, commonHeaders, 3);

	}
	
	@Test
	void consume_nodeOK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());
		
		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest("CP",
				closePaymentRequestKO.getPaymentTimestamp(), Outcome.KO, paymentTransactionEntity.paymentTransaction,
				acquirerConfiguration.getPspConfigForGetFeeAndClosePayment());
		
		asyncClosePaymentProcessor.processClosePayment(nodeClosePaymentRequest);

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);

		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals(Outcome.KO.name(), captor.getValue().getOutcome());
		Assertions.assertEquals("CP", captor.getValue().getPaymentMethod());
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestKO.getPaymentTimestamp()).atZone(ZoneId.of("UTC"));
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captor.getValue().getTimestampOperation());
		Assertions.assertEquals(transactionId, captor.getValue().getTransactionId());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getTotalAmount(), 2), captor.getValue().getTotalAmount());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getFee(), 2), captor.getValue().getFee());

		List<String> paymentTokens = captor.getValue().getPaymentTokens();
		Assertions.assertEquals(paymentTransactionEntity.paymentTransaction.getNotices().size(),
				paymentTransactionEntity.paymentTransaction.getNotices().stream()
						.map(Notice::getPaymentToken)
						.filter(paymentTokens::contains).distinct().toList().size());
		
	}
	
	@Test
	void consume_nodeKO() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.KO.name());
		
		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest("CP",
				closePaymentRequestKO.getPaymentTimestamp(), Outcome.KO, paymentTransactionEntity.paymentTransaction,
				acquirerConfiguration.getPspConfigForGetFeeAndClosePayment());
		
		asyncClosePaymentProcessor.processClosePayment(nodeClosePaymentRequest);

		Awaitility.await().atLeast(15, TimeUnit.SECONDS);

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);

		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals(Outcome.KO.name(), captor.getValue().getOutcome());
		Assertions.assertEquals("CP", captor.getValue().getPaymentMethod());
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestKO.getPaymentTimestamp()).atZone(ZoneId.of("UTC"));
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captor.getValue().getTimestampOperation());
		Assertions.assertEquals(transactionId, captor.getValue().getTransactionId());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getTotalAmount(), 2), captor.getValue().getTotalAmount());
		Assertions.assertEquals(BigDecimal.valueOf(paymentTransactionEntity.paymentTransaction.getFee(), 2), captor.getValue().getFee());

		List<String> paymentTokens = captor.getValue().getPaymentTokens();
		Assertions.assertEquals(paymentTransactionEntity.paymentTransaction.getNotices().size(),
				paymentTransactionEntity.paymentTransaction.getNotices().stream()
						.map(Notice::getPaymentToken)
						.filter(paymentTokens::contains).distinct().toList().size());
		
	}

	protected NodeClosePaymentRequest createNodeClosePaymentRequest(String paymentMethod,
																	String paymentTimestamp,
																	Outcome outcome,
																	PaymentTransaction paymentTransaction,
																	PspConfiguration pspConfiguration) {

		NodeClosePaymentRequest nodeClosePaymentRequest = new NodeClosePaymentRequest();

		nodeClosePaymentRequest.setPaymentTokens(paymentTransaction.getNotices().stream().map(Notice::getPaymentToken).toList());
		nodeClosePaymentRequest.setOutcome(outcome.name());
		nodeClosePaymentRequest.setIdPsp(pspConfiguration.getPsp());
		nodeClosePaymentRequest.setIdBrokerPSP(pspConfiguration.getBroker());
		nodeClosePaymentRequest.setIdChannel(pspConfiguration.getChannel());
		// remapping payment method based on property file
		nodeClosePaymentRequest.setPaymentMethod(paymentMethod);
		nodeClosePaymentRequest.setTransactionId(paymentTransaction.getTransactionId());
		// conversion from euro cents to euro
		nodeClosePaymentRequest.setTotalAmount(BigDecimal.valueOf(paymentTransaction.getTotalAmount(), 2));
		nodeClosePaymentRequest.setFee(BigDecimal.valueOf(Objects.requireNonNullElse(paymentTransaction.getFee(), 0L), 2));
		// transform the date from LocalDateTime to ZonedDateTime as requested by the closePayment on the node
		ZonedDateTime timestampOperation = LocalDateTime.parse(paymentTimestamp).atZone(ZoneId.of("UTC"));
		nodeClosePaymentRequest.setTimestampOperation(timestampOperation.format(DateTimeFormatter.ISO_INSTANT));

		nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

		return nodeClosePaymentRequest;
	}

}
