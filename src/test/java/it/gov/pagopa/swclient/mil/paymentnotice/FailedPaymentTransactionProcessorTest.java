/**
 * 
 */
package it.gov.pagopa.swclient.mil.paymentnotice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentMethod;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AdditionalPaymentInformations;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.FailedPaymentTransactionProcessor;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;

@QuarkusTest
class FailedPaymentTransactionProcessorTest {

	@Inject
	FailedPaymentTransactionProcessor failedPaymentTransactionProcessor;
	
	AcquirerConfiguration acquirerConfiguration;
	
	ClosePaymentRequest closePaymentRequestOK;
	
	ClosePaymentRequest closePaymentRequestKO;
	
	private CountDownLatch lock = new CountDownLatch(1);
	
	@InjectMock
	@RestClient
    NodeRestService nodeRestService;
	
	@Test
	void consume_200_OK() {
		NodeClosePaymentResponse nodeClosePaymentResponse = new NodeClosePaymentResponse();
		nodeClosePaymentResponse.setOutcome(Outcome.OK.name());
		
		Mockito
		.when(nodeRestService.closePayment(Mockito.any()))
		.thenReturn(Uni.createFrom().item(nodeClosePaymentResponse));
		
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();
		
		closePaymentRequestOK = PaymentTestData.getClosePaymentRequest(true);
		
		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequestOK,acquirerConfiguration.getPspConfigForVerifyAndActivate());
		
		failedPaymentTransactionProcessor.consume(nodeClosePaymentRequest);

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals("648fhg36s95jfg7DS",captor.getValue().getPaymentTokens().get(0));
		Assertions.assertEquals(Outcome.OK.toString(), captor.getValue().getOutcome());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getPsp(), captor.getValue().getIdPsp());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getBroker(), captor.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getChannel(), captor.getValue().getIdChannel());
		Assertions.assertEquals(PaymentMethod.PAGOBANCOMAT.name(), captor.getValue().getPaymentMethod());
		Assertions.assertEquals("517a4216840E461fB011036A0fd134E1", captor.getValue().getTransactionId());
		Assertions.assertEquals(BigDecimal.valueOf(234234).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getTotalAmount());
		Assertions.assertEquals(BigDecimal.valueOf(897).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getFee());
		
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
		
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();
		closePaymentRequestKO = PaymentTestData.getClosePaymentRequest(false);
		
		NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequestKO,acquirerConfiguration.getPspConfigForVerifyAndActivate());
		
		failedPaymentTransactionProcessor.consume(nodeClosePaymentRequest);
		
		try {
			lock.await(10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ArgumentCaptor<NodeClosePaymentRequest> captor = ArgumentCaptor.forClass(NodeClosePaymentRequest.class);
		Mockito.verify(nodeRestService).closePayment(captor.capture());
		Assertions.assertEquals("648fhg36s95jfg7DS",captor.getValue().getPaymentTokens().get(0));
		Assertions.assertEquals(Outcome.KO.toString(), captor.getValue().getOutcome());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getPsp(), captor.getValue().getIdPsp());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getBroker(), captor.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForGetFeeAndClosePayment().getChannel(), captor.getValue().getIdChannel());
		Assertions.assertEquals(PaymentMethod.PAGOBANCOMAT.name(), captor.getValue().getPaymentMethod());
		Assertions.assertEquals("517a4216840E461fB011036A0fd134E1", captor.getValue().getTransactionId());
		Assertions.assertEquals(BigDecimal.valueOf(234234).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getTotalAmount());
		Assertions.assertEquals(BigDecimal.valueOf(897).divide(new BigDecimal(100), RoundingMode.HALF_DOWN), captor.getValue().getFee());
		
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
		nodeClosePaymentRequest.setPaymentMethod(closePaymentRequest.getPaymentMethod());
		nodeClosePaymentRequest.setTransactionId(closePaymentRequest.getTransactionId());
		// conversion from euro cents to euro
		nodeClosePaymentRequest.setTotalAmount(new BigDecimal(closePaymentRequest.getTotalAmount()).divide(new BigDecimal(100), RoundingMode.HALF_DOWN));
		nodeClosePaymentRequest.setFee(new BigDecimal(closePaymentRequest.getFee()).divide(new BigDecimal(100), RoundingMode.HALF_DOWN));
		// transform the date from LocalDateTime to ZonedDateTime as requested by the closePayment on the node
		ZonedDateTime timestampOperation = LocalDateTime.parse(closePaymentRequest.getTimestampOp()).atZone(ZoneId.of("UTC"));
		nodeClosePaymentRequest.setTimestampOperation(timestampOperation.format(DateTimeFormatter.ISO_INSTANT));

		nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

		return nodeClosePaymentRequest;
	}
}
