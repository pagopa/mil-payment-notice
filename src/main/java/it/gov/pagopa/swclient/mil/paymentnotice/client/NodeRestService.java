package it.gov.pagopa.swclient.mil.paymentnotice.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient(configKey = "node-rest-api")
public interface NodeRestService {
	
	@POST
	@Path("/closepayment")
    Uni<NodeClosePaymentResponse> closePayment(NodeClosePaymentRequest nodeClosePaymentRequest);

}
