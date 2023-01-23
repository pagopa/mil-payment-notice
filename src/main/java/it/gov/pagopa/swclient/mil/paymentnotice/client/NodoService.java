package it.gov.pagopa.swclient.mil.paymentnotice.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.exception.NodeResponseExceptionMapper;

@RegisterRestClient(configKey = "pmwallet-api")
@RegisterProvider(NodeResponseExceptionMapper.class)
public interface NodoService {
	
	@POST
	@Path("/closepayment")
    Uni<ClosePaymentResponse> closePayment(ClosePaymentRequest closePaymentRequest);

}
