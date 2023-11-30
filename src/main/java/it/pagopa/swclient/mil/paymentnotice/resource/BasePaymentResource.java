package it.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.pagopa.swclient.mil.paymentnotice.client.AzureADRestClient;
import it.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.bean.PaymentTransactionOutcome;
import it.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.Preset;
import it.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AdditionalPaymentInformations;
import it.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.pagopa.swclient.mil.paymentnotice.utils.NodeApi;
import it.pagopa.swclient.mil.paymentnotice.utils.NodeErrorMapping;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;

public class BasePaymentResource {

	/**
	 * The configuration object containing the mapping between the errors returned by the node,
	 * and the error returned by the MIL APIs
	 */
	@Inject
	NodeErrorMapping nodeErrorMapping;

	@RestClient
	AzureADRestClient azureADRestClient;

	/**
	 * The configuration object containing the mapping between the payment methods passed in request to the MIL
	 * APIs and the payment methods accepted by the node
	 */
	@ConfigProperty(name = "node.paymentmethod.map")
	Map<String, String> nodePaymentMethodMap;


	/**
	 * The async wrapper for the Node SOAP client
	 */
	@Inject
	NodeForPspWrapper nodeWrapper;

	/**
	 * The reactive REST client for the MIL REST interfaces
	 */
	@Inject
	MilRestService milRestService;

	@ConfigProperty(name = "azure-auth-api.identity")
	String identity;

	public static final String VAULT = "https://storage.azure.com";

	private static final String BEARER = "Bearer ";


	/**
	 * Retrieves the PSP configuration by acquirer id, and emits it as a Uni
	 *
	 * @param acquirerId the id of the acquirer
	 * @param api a {@link NodeApi} used to choose which of the two PspConfiguration return
	 * @return the {@link Uni} emitting a {@link PspConfiguration}
	 */
	protected Uni<PspConfiguration> retrievePSPConfiguration(String acquirerId, NodeApi api) {
		Log.debugf("retrievePSPConfiguration - acquirerId: %s ", acquirerId);

		return azureADRestClient.getAccessToken(identity, VAULT)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] Error while calling Azure AD rest service", ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES);

					return new InternalServerErrorException(Response
							.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES)))
							.build());
				}).chain(token -> {
					Log.debugf("BasePaymentResource -> retrievePspConfiguration: Azure AD service returned a 200 status, response token: [%s]", token);

					if (token.getToken() == null) {
						return Uni.createFrom().failure(new InternalServerErrorException(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.AZUREAD_ACCESS_TOKEN_IS_NULL)))
								.build()));
					}

					return milRestService.getPspConfiguration(BEARER + token.getToken(), acquirerId)
							.onFailure().transform(t -> {
								if (t instanceof ClientWebApplicationException webEx && webEx.getResponse().getStatus() == 404) {
									Log.errorf(t, "[%s] Missing psp configuration for acquirerId", ErrorCode.UNKNOWN_ACQUIRER_ID);
									return new InternalServerErrorException(Response
											.status(Response.Status.INTERNAL_SERVER_ERROR)
											.entity(new Errors(List.of(ErrorCode.UNKNOWN_ACQUIRER_ID)))
											.build());
								} else {
									Log.errorf(t, "[%s] Error retrieving the psp configuration", ErrorCode.ERROR_CALLING_MIL_REST_SERVICES);
									return new InternalServerErrorException(Response
											.status(Response.Status.INTERNAL_SERVER_ERROR)
											.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)))
											.build());
								}
							})
							.map(acquirerConfiguration -> switch (api) {
								case ACTIVATE, VERIFY -> acquirerConfiguration.getPspConfigForVerifyAndActivate();
								case CLOSE -> acquirerConfiguration.getPspConfigForGetFeeAndClosePayment();
							});
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

	/**
	 * Creates a payment transaction to be stored in the DB from the data passed in request in the
	 * {@link PaymentResource#preClose(CommonHeader, PreCloseRequest)} and the notice data retrieved from cache
	 *
	 * @param headers the MIL headers passed in request to the preClose
	 * @param transactionId the transaction ID of the payment transaction
	 * @param fees the fees of the payment transaction as returned by GEC
	 * @param notices the list of notices retrieved from the cache
	 * @param outcome the outcome passed in request of the preClose, con be PRE_CLOSE or ABORT
	 * @return the {@link PaymentTransactionEntity} to be stored in the DB
	 */
	protected static PaymentTransactionEntity createPaymentTransactionEntity(CommonHeader headers,
																			 String transactionId,
																			 Long fees,
																			 List<Notice> notices,
																			 String outcome,
																			 Preset preset) {

		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setTransactionId(transactionId);
		paymentTransaction.setAcquirerId(headers.getAcquirerId());
		paymentTransaction.setChannel(headers.getChannel());
		paymentTransaction.setMerchantId(headers.getMerchantId());
		paymentTransaction.setTerminalId(headers.getTerminalId());
		paymentTransaction.setInsertTimestamp(getTimestamp());
		paymentTransaction.setNotices(notices);
		paymentTransaction.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));
		paymentTransaction.setFee(fees);
		paymentTransaction.setStatus(PaymentTransactionOutcome.PRE_CLOSE.name().equals(outcome) ?
				PaymentTransactionStatus.PRE_CLOSE.name() : PaymentTransactionStatus.ABORTED.name());

		paymentTransaction.setPreset(preset);
		PaymentTransactionEntity entity = new PaymentTransactionEntity();
		entity.transactionId = transactionId;
		entity.paymentTransaction = paymentTransaction;

		return entity;
	}

	/**
	 * Creates the request for the closePayment REST API of the node
	 *
	 * @param paymentMethod the payment method used for the e-money transaction
	 * @param paymentTimestamp the timestamp of the e-money transaction
	 * @param outcome the outcome of the e-money transaction
	 * @param paymentTransaction the object containing the data of the payment transaction, retrieved from the DB
	 * @param pspConfiguration the configuration of the PSP, retrieved from the MIL configuration API
	 * @return the {@link NodeClosePaymentRequest} to be sent to the node
	 */
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
		nodeClosePaymentRequest.setPaymentMethod(nodePaymentMethodMap.getOrDefault(paymentMethod, paymentMethod));
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

	/**
	 * Generates the current timestamp (UTC time) in the uuuu-MM-dd'T'HH:mm:ss format
	 * @return the timestamp
	 */
	protected static String getTimestamp() {
		return LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	/**
	 * Checks if transaction stored on DB was created by the client invoking the API
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paymentTransaction the payment transaction stored on the DB
	 * @return true if transaction was created by the caller, false otherwise
	 */
	protected boolean isTransactionLinkedToClient(CommonHeader headers, PaymentTransaction paymentTransaction) {
		return StringUtils.equals(headers.getAcquirerId(), paymentTransaction.getAcquirerId())
				&& StringUtils.equals(headers.getMerchantId(), paymentTransaction.getMerchantId())
				&& StringUtils.equals(headers.getChannel(), paymentTransaction.getChannel())
				&& StringUtils.equals(headers.getTerminalId(), paymentTransaction.getTerminalId());
	}

	/**
	 * Generates the deviceId to be passed as query param to the node in the close payment API
	 *
	 * @param commonHeader the object containing all the common headers used by the mil services
	 * @return the deviceId value
	 */
	protected String getDeviceId(CommonHeader commonHeader) {
		return StringUtils.join(List.of(commonHeader.getAcquirerId(), commonHeader.getTerminalId()), "|");
	}

}
