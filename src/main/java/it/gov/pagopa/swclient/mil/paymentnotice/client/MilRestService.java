package it.gov.pagopa.swclient.mil.paymentnotice.client;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Reactive rest client for the REST APIs exposed by the MIL APIM
 */
@RegisterRestClient(configKey = "mil-rest-api")
public interface MilRestService {

	/**
	 * Retrieves the psp configuration
	 * @param requestId the id of the request
	 * @param acquirerId the acquirer id passed in request
	 * @return the psp configuration for the acquirer id
	 */
	@GET
	@Path("/mil-acquirer-conf/confs/{acquirerId}/psp")
	@ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${mil-rest-client.apim-subscription-key}", required = false)
	@ClientHeaderParam(name = "Version", value = "${mil-rest-client.mil-acquirer-conf.version}", required = false)
    Uni<AcquirerConfiguration> getPspConfiguration(@HeaderParam(value = "RequestId") String requestId, @PathParam(value = "acquirerId") String acquirerId);

}
