package it.pagopa.swclient.mil.paymentnotice.client;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.QueryParam;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

import java.util.Optional;

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
	@ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "{determineHeaderValue}", required=false)
	@ClientQueryParam(name="clientId", value = "${node-rest-client.client-id}")
	@WithSpan(kind = SpanKind.CLIENT)
	Uni<NodeClosePaymentResponse> closePayment(@QueryParam("deviceId") String deviceId, NodeClosePaymentRequest nodeClosePaymentRequest);

	default String determineHeaderValue(String headerName) {
         if ("Ocp-Apim-Subscription-Key".equals(headerName)) {
			 Optional<String> optSubscriptionKey = ConfigProvider.getConfig().getOptionalValue("node-rest-client.apim-subscription-key", String.class);
			 if (optSubscriptionKey.isPresent() && StringUtils.isNotBlank(optSubscriptionKey.get())) {
				 return StringUtils.trim(optSubscriptionKey.get());
			 }
			 throw new UnsupportedOperationException("Blank property");
         }
         throw new UnsupportedOperationException("Unknown header name");
     }

}
