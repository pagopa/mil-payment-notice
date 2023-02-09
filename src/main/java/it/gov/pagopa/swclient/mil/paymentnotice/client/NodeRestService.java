package it.gov.pagopa.swclient.mil.paymentnotice.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

/**
 * Reactive rest client for the REST APIs exposed by the node
 */
@RegisterRestClient(configKey = "node-rest-api")
public interface NodeRestService {

	/**
	 * Client of the closePayment API exposed by the node
	 * @param nodeClosePaymentRequest the request to the node
	 * @return the response from the node
	 */
	@POST
	@Path("/closepayment")
	@ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${node-rest-client.apim-subscription-key}")
    Uni<NodeClosePaymentResponse> closePayment(NodeClosePaymentRequest nodeClosePaymentRequest);

}
