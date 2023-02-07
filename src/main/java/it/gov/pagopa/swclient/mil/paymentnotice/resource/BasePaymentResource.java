package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeErrorMapping;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasePaymentResource {

	/**
	 * The configuration object cotaining the mapping between
	 */
	@Inject
	NodeErrorMapping nodeErrorMapping;

	@Inject
	NodeForPspWrapper nodeWrapper;
	
    @Inject
	PspConfRepository pspConfRepository;


	/**
	 * Retrieves the PSP configuration from the database by acquirer id, and emits it as a Uni
	 *
	 * @param acquirerId the id of the acquirer
	 * @return the {@link Uni} emitting a {@link PspConfiguration}
	 */
	protected Uni<PspConfiguration> retrievePSPConfiguration(String acquirerId) {
		Log.debugf("retrievePSPConfiguration - acquirerId: %s ", acquirerId);

		return pspConfRepository.findByIdOptional(acquirerId)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] Error retrieving data from the db", ErrorCode.ERROR_RETRIEVING_DATA_FROM_MONGO);
					return new InternalServerErrorException(Response
							.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_MONGO)))
							.build());
				})
				.onItem().transform(o -> o.orElseThrow(NotFoundException::new))
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] Error retrieving data from the db", ErrorCode.UNKNOWN_ACQUIRER_ID);
					return new InternalServerErrorException(Response
							.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.UNKNOWN_ACQUIRER_ID)))
							.build());
				})
				.map(t -> t.pspConfiguration);

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
						.collect(Collectors.joining(",")));
		if (outcomeErrorId == null) {
			Log.errorf("Could not find configured mapping for faultCode %s originalFaultCode %s, defaulting to UNEXPECTED_ERROR",
					faultCode, originalFaultCode);
			outcomeErrorId = 0;
		}
		return nodeErrorMapping.outcomes().get(outcomeErrorId);

	}

}
