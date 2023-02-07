package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonParseException;
import io.vertx.core.eventbus.EventBus;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.VerifyPaymentNoticeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AdditionalPaymentInformations;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@Path("/payments")
public class PaymentResource extends BasePaymentResource {

	/**
	 * The reactive REDIS client
	 */
	@Inject
	PaymentService paymentService;

	/**
	 * The reactive REST client for the node interfaces
	 */
	@RestClient
	NodeRestService nodeRestService;

	/**
	 * The Vert.X bus used to asynchronously process the closePayment request in case of an KO outcome
	 * of the e-payment transaction
	 */
	@Inject
	EventBus bus;

	/**
	 * The value of the Max-Retry header to be sent in response to the closePayment API
	 */
	@ConfigProperty(name="paymentnotice.closepayment.max-retry", defaultValue = "3")
	int closePaymentMaxRetry;

	/**
	 * The value of the Retry-After header to be sent in response to the closePayment API
	 */
	@ConfigProperty(name="paymentnotice.closepayment.retry-after", defaultValue = "30")
	int closePaymentRetryAfter;


	/**
	 * Closes a set of payment notices previously activated calling the
	 * {@link ActivatePaymentNoticeResource#activateByQrCode(CommonHeader, String, ActivatePaymentNoticeRequest) activateByQrCode}
	 * or {@link ActivatePaymentNoticeResource#activateByTaxCodeAndNoticeNumber(CommonHeader, String, String, ActivatePaymentNoticeRequest) activateByTaxCodeAndNoticeNumber}
     *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param closePaymentRequest a {@link ClosePaymentRequest} instance containing the payment tokens and the outcome of the common e-payment transaction
	 * @return a {@link ClosePaymentResponse} instance containing the outcome of taking charge of the closing of the payment notices. The HTTP response contains
	 * a Location header with the URL to invoke to retrieve the final status of the closing operation
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> closePayment(@Valid @BeanParam CommonHeader headers, @Valid ClosePaymentRequest closePaymentRequest) {

		Log.debugf("closePayment - Input parameters: %s, body: %s", headers, closePaymentRequest);
		
		return retrievePSPConfiguration(headers.getAcquirerId())
				.chain(pspConf -> {
					if (Outcome.OK.name().equals(closePaymentRequest.getOutcome())) {
						return paymentService.setIfNotExist(closePaymentRequest.getTransactionId(), new ReceivePaymentStatusRequest()).
								onFailure().transform(t -> {
									Log.errorf(t, "[%s] REDIS error while saving data", ErrorCode.ERROR_STORING_DATA_INTO_REDIS);
									return new InternalServerErrorException(Response
											.status(Status.INTERNAL_SERVER_ERROR)
											.entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_INTO_REDIS)))
											.build());
								})
								.chain(() -> callNodeClosePaymentOutcomeOk(closePaymentRequest, pspConf));
					}
					else {
						NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequest, pspConf);
						// we are using the non-mutiny variant of the event bus because this is a fire-and-forget scenario
						bus.<String>request("failedPaymentTransaction", nodeClosePaymentRequest);
						Log.debugf("closePayment - response status %s", Status.ACCEPTED);
						return Uni.createFrom().item(Response.status(Status.ACCEPTED).build());
					}
				});
		
	}

	/**
	 * Retrieves the status of a close payment operation by its transaction id
     *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param transactionId the id of the payment transaction as returned by the {@link #closePayment(CommonHeader, ClosePaymentRequest) closePayment} method
	 * @return a {@link ReceivePaymentStatusRequest} instance containing the status of the close payment operation and the details of the payment notices
	 */
	@GET
	@Path("/{transactionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> getPaymentStatus(@Valid @BeanParam CommonHeader headers,
										  @Pattern(regexp = "^[a-zA-Z0-9]{1,255}$",
												  message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
										  String transactionId) {

		Log.debugf("getPaymentStatus - %s, transactionId: %s", headers, transactionId);
		
		return paymentService.get(transactionId)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] REDIS error while retrieving data", ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS)))
							.build());
				})
				.onItem().ifNull().failWith(() ->{
					Log.errorf("[%s] REDIS transactionId not found", ErrorCode.UNKNOWN_PAYMENT_TRANSACTION);
					return new NotFoundException(Response
							.status(Status.NOT_FOUND)
							.entity(new Errors(List.of(ErrorCode.UNKNOWN_PAYMENT_TRANSACTION)))
							.build());
				})
				.map(res -> {
					if (res.getOutcome() == null) {
						Log.debugf("getPaymentStatus - transaction not found on redis");
						return Response.status(Status.NOT_FOUND).build();
					}
					else {
						Log.debugf("getPaymentStatus - response %s", res);
						return Response.status(Status.OK).entity(res).build();
					}
				});
	}

	/**
	 * Stores the status of a close payment operation by its transactionId
     *
	 * @param transactionId the id of the payment transaction as returned by the {@link #closePayment(CommonHeader, ClosePaymentRequest) closePayment} method
	 * @param receivePaymentStatusRequest a {@link ReceivePaymentStatusRequest} instance containing the status of the close payment operation and the details of the payment notices
	 * @return a {@link ReceivePaymentStatusResponse} instance containing the outcome of the store operation
	 */
	@POST
	@Path("/{transactionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> receivePaymentStatus( @Pattern(regexp = "^[a-zA-Z0-9]{1,255}$", message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
											   String transactionId,
											   @Valid ReceivePaymentStatusRequest receivePaymentStatusRequest) {

		Log.debugf("receivePaymentStatus - Input path params: transactionId %s, body: %s", transactionId, receivePaymentStatusRequest);
		
		return paymentService.get(transactionId)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] REDIS error retrieving payment result in cache", ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS)))
							.build());
				})
				.onItem().ifNull().failWith(() -> {
					// if no transaction is found on redis return PAYMENT_NOT_FOUND as outcome
					ReceivePaymentStatusResponse receiveResponse = new ReceivePaymentStatusResponse();
					receiveResponse.setOutcome("PAYMENT_NOT_FOUND");
					return new NotFoundException(Response.status(Status.NOT_FOUND).entity(receiveResponse).build());
				})
				.chain(n -> paymentService.set(transactionId, receivePaymentStatusRequest)
						.onFailure().transform(t -> {
							Log.errorf(t, "[%s] REDIS error saving payment result in cache", ErrorCode.ERROR_STORING_DATA_INTO_REDIS);
							return new InternalServerErrorException(Response
									.status(Status.INTERNAL_SERVER_ERROR)
									.entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_INTO_REDIS)))
									.build());
						})
						.map(res -> {
							ReceivePaymentStatusResponse receiveResponse = new ReceivePaymentStatusResponse();
							receiveResponse.setOutcome(Outcome.OK.toString());
							Log.debugf("receivePaymentStatus - response %s", receiveResponse);
							return Response.status(Status.OK).entity(receiveResponse).build();
						})
				);
	}

	/**
	 * Branch of the closePayment Uni that calls the node to close a set of previously activated payment notices
     *
	 * @param closePaymentRequest the object received in request of the closePayment
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @return a {@link Uni} emitting a {@link VerifyPaymentNoticeResponse} containing the data retrieved from the node
	 */
	private Uni<Response> callNodeClosePaymentOutcomeOk(ClosePaymentRequest closePaymentRequest,
											   PspConfiguration pspConfiguration) {

		NodeClosePaymentRequest nodeClosePaymentRequest =
				createNodeClosePaymentRequest(closePaymentRequest, pspConfiguration);

		Log.debugf("Calling the node closePayment service for OK outcome");
		return nodeRestService.closePayment(nodeClosePaymentRequest)
			.onItemOrFailure()
			.transform((closePayRes, error) -> {
				boolean outcomeOk = false;
				if (error != null) {
					Log.errorf(error, "Error calling the node closePayment service");
				 	if (error instanceof ClientWebApplicationException webEx) {
						outcomeOk = validateClosePaymentError(webEx);
					}
					else if (error instanceof TimeoutException) {
						Log.debug("Node closePayment went in timeout, responding with outcome OK");
						outcomeOk = true; // for a timeout we return outcome ok
					}
					else {
						// in any other case we return 500
				    	return Response
								.status(Status.INTERNAL_SERVER_ERROR)
				    			.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_REST_SERVICES)))
								.build();
					}
			    }
				else {
					Log.debugf("Node closePayment service responded %s", closePayRes);
					if (Outcome.OK.name().equals(closePayRes.getOutcome())) {
						outcomeOk = true;
					}
			    }

				// returning response
				ClosePaymentResponse closePaymentResponse = new ClosePaymentResponse();
				Response.ResponseBuilder responseBuilder = Response.status(Status.OK);
				if (outcomeOk) {
					closePaymentResponse.setOutcome(Outcome.OK);
					responseBuilder
							.location(URI.create("/payment/" + closePaymentRequest.getTransactionId()))
							.header("Retry-After", closePaymentRetryAfter)
							.header("Max-Retry", closePaymentMaxRetry);
				}
				else {
					closePaymentResponse.setOutcome(Outcome.KO);
				}

				Log.debugf("closePayment - response %s", closePaymentResponse);
				return responseBuilder.entity(closePaymentResponse).build();

			});

	}

	/**
	 * Checks if a ClientWebApplicationException (wrapping a status != 2xx) or an unparsable response
	 * returned from the REST client connecting to the node closePayment REST API should be mapped to an OK outcome
     *
	 * @param webEx the exception returned by the rest client
	 * @return true if the error maps to an OK outcome, false otherwise
	 */
	private boolean validateClosePaymentError(ClientWebApplicationException webEx) {

		boolean outcomeOk = false;

		// un unparsable response is wrapped in a ClientWebApplicationException exception
		// with 404 status, so we need to distinguish it from the real 404 case
		if (ExceptionUtils.indexOfThrowable(webEx, JsonParseException.class) != -1) {
			Log.debug("Node closePayment returned an unparsable response, responding with outcome OK");
			outcomeOk = true;
		} else {
			int nodeResponseStatus = webEx.getResponse().getStatus();
			switch (nodeResponseStatus) {
				case 400, 404, 422 ->
					// for these three statuses we return outcome ko
					Log.debugf("Node closePayment returned a %s status response, responding with outcome KO", nodeResponseStatus);
				default -> {
					// for any other status we return outcome ok
					Log.debugf("Node closePayment returned a %s status response, responding with outcome OK", nodeResponseStatus);
					outcomeOk = true;
				}
			}
		}
		return outcomeOk;
	}


	/**
	 * Creates the request for the closePayment REST API of the node
	 *
	 * @param closePaymentRequest the {@link ClosePaymentRequest} received in request by the MIL
	 * @param pspConfiguration the @{@link PspConfiguration} retrieved from the DB
	 * @return
	 */
	private NodeClosePaymentRequest createNodeClosePaymentRequest(ClosePaymentRequest closePaymentRequest, PspConfiguration pspConfiguration) {

		NodeClosePaymentRequest nodeClosePaymentRequest =
				new NodeClosePaymentRequest();

		nodeClosePaymentRequest.setPaymentTokens(closePaymentRequest.getPaymentTokens());
		nodeClosePaymentRequest.setOutcome(closePaymentRequest.getOutcome());
		nodeClosePaymentRequest.setIdPsp(pspConfiguration.getPspId());
		nodeClosePaymentRequest.setIdBrokerPSP(pspConfiguration.getPspBroker());
		nodeClosePaymentRequest.setIdChannel(pspConfiguration.getIdChannel());
		nodeClosePaymentRequest.setPaymentMethod(closePaymentRequest.getPaymentMethod());
		nodeClosePaymentRequest.setTransactionId(closePaymentRequest.getTransactionId());
		nodeClosePaymentRequest.setTotalAmount(new BigDecimal(closePaymentRequest.getTotalAmount()).divide(new BigDecimal(100)));
		nodeClosePaymentRequest.setFee(new BigDecimal(closePaymentRequest.getFee()).divide(new BigDecimal(100)));
		nodeClosePaymentRequest.setTimestampOperation(closePaymentRequest.getTimestampOp()); // FIXME transform in 2022-02-22T14:41:58.811+01:00

		nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

		return nodeClosePaymentRequest;
	}

}
