package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeErrorMapping;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeApi;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasePaymentResource {

	/**
	 * The configuration object containing the mapping between
	 */
	@Inject
	NodeErrorMapping nodeErrorMapping;

	/**
	 * The async wrapper for the Node SOAP client
	 */
	@Inject
	NodeForPspWrapper nodeWrapper;

	/**
	 * The reactive REST client for the MIL REST interfaces
	 */
	@RestClient
	MilRestService milRestService;


	/**
	 * Retrieves the PSP configuration by acquirer id, and emits it as a Uni
	 *
	 * @param acquirerId the id of the acquirer
	 * @return the {@link Uni} emitting a {@link PspConfiguration}
	 */
	protected Uni<PspConfiguration> retrievePSPConfiguration(String requestId, String acquirerId, NodeApi api) {
		Log.debugf("retrievePSPConfiguration - requestId: %s acquirerId: %s ", requestId, acquirerId);

		return milRestService.getPspConfiguration(requestId, acquirerId)
				.onFailure().transform(t -> {
					if (t instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {
						Log.errorf(t, "[%s] Missing psp configuration for acquirerId", ErrorCode.UNKNOWN_ACQUIRER_ID);
						return new InternalServerErrorException(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.UNKNOWN_ACQUIRER_ID)))
								.build());
					}
					else {
						Log.errorf(t, "[%s] Error retrieving the psp configuration", ErrorCode.ERROR_CALLING_MIL_REST_SERVICES);
						return new InternalServerErrorException(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)))
								.build());
					}
				})
				.map(a -> switch (api) {
					case ACTIVATE, VERIFY -> a.getPspConfigForVerifyAndActivate();
					case CLOSE -> a.getPspConfigForGetFeeAndClosePayment();
				});
	}


	/**
	 * Remaps the fault error from the node to an outcome based on the mapping in the property
	 *
	 * @param faultCode the fault code returned by the node
	 * @param originalFaultCode the original fault code returned by the node (could be empty)
	 * @return the mapped outcome
	 */
	protected String remapNodeFaultToOutcome(String faultCode, String originalFaultCode) {

		Integer outcomeErrorId = nodeErrorMapping.map().
				get(Stream.of(faultCode, originalFaultCode)
						.filter(s -> s != null && !s.isEmpty())
						.collect(Collectors.joining("-")));
		if (outcomeErrorId == null) {
			Log.errorf("Could not find configured mapping for faultCode %s originalFaultCode %s, defaulting to UNEXPECTED_ERROR",
					faultCode, originalFaultCode);
			outcomeErrorId = 0;
		}
		return nodeErrorMapping.outcomes().get(outcomeErrorId);

	}

}
