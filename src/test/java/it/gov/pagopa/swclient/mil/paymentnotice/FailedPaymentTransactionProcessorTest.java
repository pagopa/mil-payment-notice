/**
 * 
 */
package it.gov.pagopa.swclient.mil.paymentnotice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AdditionalPaymentInformations;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.FailedPaymentTransactionProcessor;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FailedPaymentTransactionProcessorTest {

	@Inject
	FailedPaymentTransactionProcessor failedPaymentTransactionProcessor;

	@InjectMock
	@RestClient
	NodeRestService nodeRestService;

	AcquirerConfiguration acquirerConfiguration;
	
	ClosePaymentRequest closePaymentRequestOK;
	
	ClosePaymentRequest closePaymentRequestKO;
	
	@BeforeAll
	void createTestObjects() {

		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		closePaymentRequestOK = PaymentTestData.getClosePaymentRequest(true);

		closePaymentRequestKO = PaymentTestData.getClosePaymentRequest(false);

	}
	
	@Test
	void consume_200_OK() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());
		
		Mockito
		.when(nodeRestService.closePayment(Mockito.any()))
		.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequestOK,
				acquirerConfiguration.getPspConfigForGetFeeAndClosePayment());
		
		failedPaymentTransactionProcessor.consume(nodeClosePaymentRequest);

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals(closePaymentRequestOK.getPaymentTokens().get(0), captor.getValue().getPaymentTokens().get(0));
		Assertions.assertEquals(Outcome.OK.toString(), captor.getValue().getOutcome());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getPsp(), captor.getValue().getIdPsp());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getBroker(), captor.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getChannel(), captor.getValue().getIdChannel());
		Assertions.assertEquals("CP", captor.getValue().getPaymentMethod());
		Assertions.assertEquals(closePaymentRequestOK.getTransactionId(), captor.getValue().getTransactionId());
		Assertions.assertEquals(closePaymentRequestOK.getTotalAmount(), captor.getValue().getTotalAmount().multiply(new BigDecimal(100)).toBigInteger());
		Assertions.assertEquals(closePaymentRequestOK.getFee(), captor.getValue().getFee().multiply(new BigDecimal(100)).toBigInteger());
		
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestOK.getTimestampOp()).atZone(ZoneId.of("UTC"));
		
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captor.getValue().getTimestampOperation());
		Assertions.assertNotNull(captor.getValue().getAdditionalPaymentInformations());
		
	}
	
	@Test
	void consume_200_KO() {

		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.KO.name());
		
		Mockito
				.when(nodeRestService.closePayment(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));

		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequestKO,
				acquirerConfiguration.getPspConfigForGetFeeAndClosePayment());
		
		failedPaymentTransactionProcessor.consume(nodeClosePaymentRequest);

		Awaitility.await().atLeast(15, TimeUnit.SECONDS);

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals(closePaymentRequestKO.getPaymentTokens().get(0), captor.getValue().getPaymentTokens().get(0));
		Assertions.assertEquals(Outcome.KO.toString(), captor.getValue().getOutcome());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getPsp(), captor.getValue().getIdPsp());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getBroker(), captor.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getChannel(), captor.getValue().getIdChannel());
		Assertions.assertEquals("CP", captor.getValue().getPaymentMethod());
		Assertions.assertEquals(closePaymentRequestKO.getTransactionId(), captor.getValue().getTransactionId());
		Assertions.assertEquals(closePaymentRequestKO.getTotalAmount(), captor.getValue().getTotalAmount().multiply(new BigDecimal(100)).toBigInteger());
		Assertions.assertEquals(closePaymentRequestKO.getFee(), captor.getValue().getFee().multiply(new BigDecimal(100)).toBigInteger());
		
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequestKO.getTimestampOp()).atZone(ZoneId.of("UTC"));
		
		Assertions.assertEquals(timestampOperation.format(DateTimeFormatter.ISO_INSTANT), captor.getValue().getTimestampOperation());
		Assertions.assertNotNull(captor.getValue().getAdditionalPaymentInformations());
		
	}

	private NodeClosePaymentRequest createNodeClosePaymentRequest(ClosePaymentRequest closePaymentRequest, PspConfiguration pspConfiguration) {

		NodeClosePaymentRequest nodeClosePaymentRequest =
				new NodeClosePaymentRequest();

		nodeClosePaymentRequest.setPaymentTokens(closePaymentRequest.getPaymentTokens());
		nodeClosePaymentRequest.setOutcome(closePaymentRequest.getOutcome());
		nodeClosePaymentRequest.setIdPsp(pspConfiguration.getPsp());
		nodeClosePaymentRequest.setIdBrokerPSP(pspConfiguration.getBroker());
		nodeClosePaymentRequest.setIdChannel(pspConfiguration.getChannel());

		// remapping payment method based on property file
		Optional<String> optPaymentMethodMapping = ConfigProvider.getConfig()
				.getOptionalValue("node.paymentmethod.map." + closePaymentRequest.getPaymentMethod(), String.class);
		nodeClosePaymentRequest.setPaymentMethod(optPaymentMethodMapping.orElse(closePaymentRequest.getPaymentMethod()));

		nodeClosePaymentRequest.setTransactionId(closePaymentRequest.getTransactionId());
		// conversion from euro cents to euro
		nodeClosePaymentRequest.setTotalAmount(new BigDecimal(closePaymentRequest.getTotalAmount(), 2));
		nodeClosePaymentRequest.setFee(new BigDecimal(closePaymentRequest.getFee(), 2));
		// transform the date from LocalDateTime to ZonedDateTime as requested by the closePayment on the node
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequest.getTimestampOp()).atZone(ZoneId.of("UTC"));
		nodeClosePaymentRequest.setTimestampOperation(timestampOperation.format(DateTimeFormatter.ISO_INSTANT));

		nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

		return nodeClosePaymentRequest;
	}
}
