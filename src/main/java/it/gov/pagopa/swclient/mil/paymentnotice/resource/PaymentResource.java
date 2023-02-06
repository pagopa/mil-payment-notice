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

	@Inject
	PaymentService paymentService;

	@RestClient
	NodeRestService nodeRestService;

	@Inject
	EventBus bus;

	@ConfigProperty(name="paymentnotice.closepayment.max-retry", defaultValue = "3")
	int closePaymentMaxRetry;

	@ConfigProperty(name="paymentnotice.closepayment.retry-after", defaultValue = "30")
	int closePaymentRetryAfter;

	/**
	 * Closes a set of payment notices previously activated calling the
	 * {@link ActivatePaymentNoticeResource#activateByQrCode(CommonHeader, String, ActivatePaymentNoticeRequest) activateByQrCode}
	 * or {@link ActivatePaymentNoticeResource#activateByTaxCodeAndNoticeNumber(CommonHeader, String, String, ActivatePaymentNoticeRequest) activateByTaxCodeAndNoticeNumber}
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
						return callNodeClosePaymentOutcomeOk(closePaymentRequest, pspConf, headers.getChannel());
					}
					else {
						NodeClosePaymentRequest nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequest, pspConf, headers.getChannel());
						// we are using the non-mutiny variant of the event bus because this is a fire-and-forget scenario
						bus.<String>request("failedPaymentTransaction", nodeClosePaymentRequest);
						Log.debugf("closePayment - response status %s", Status.ACCEPTED);
						return Uni.createFrom().item(Response.status(Status.ACCEPTED).build());
					}
				});
		
	}

	/**
	 * Retrieves the status of a close payment operation by its transaction id
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
					Log.debugf("getPaymentStatus - response %s", res);
					return Response.status(Status.OK).entity(res).build();
				});
	}

	/**
	 * Stores the status of a close payment operation by its transactionId
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
		
		return paymentService.set(transactionId, receivePaymentStatusRequest)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] REDIS error saving session in cache", ErrorCode.ERROR_STORING_DATA_INTO_REDIS);
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
				});
	}

	/**
	 * Branch of the closePayment Uni that call the node to close a set of previously activated payment notices
	 * @param closePaymentRequest the object received in request of the closePayment
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @param channel the channel from which the request to verify the payment notice was done
	 * @return an @{@link Uni} emitting a @{@link VerifyPaymentNoticeResponse} containing the data retrieved from the node
	 */
	private Uni<Response> callNodeClosePaymentOutcomeOk(ClosePaymentRequest closePaymentRequest,
											   PspConfiguration pspConfiguration, String channel) {

		NodeClosePaymentRequest nodeClosePaymentRequest =
				createNodeClosePaymentRequest(closePaymentRequest, pspConfiguration, channel);

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

	private NodeClosePaymentRequest createNodeClosePaymentRequest(ClosePaymentRequest closePaymentRequest, PspConfiguration pspConfiguration, String channel) {

		NodeClosePaymentRequest nodeClosePaymentRequest =
				new NodeClosePaymentRequest();

		nodeClosePaymentRequest.setPaymentTokens(closePaymentRequest.getPaymentTokens());
		nodeClosePaymentRequest.setOutcome(closePaymentRequest.getOutcome());
		nodeClosePaymentRequest.setIdPsp(pspConfiguration.getPspId());
		nodeClosePaymentRequest.setPspBroker(pspConfiguration.getPspBroker());
		nodeClosePaymentRequest.setIdChannel(channel);
		nodeClosePaymentRequest.setPaymentMethod(closePaymentRequest.getPaymentMethod());
		nodeClosePaymentRequest.setTransactionId(closePaymentRequest.getTransactionId());
		nodeClosePaymentRequest.setTotalAmount(new BigDecimal(closePaymentRequest.getTotalAmount()).divide(new BigDecimal(100)));
		nodeClosePaymentRequest.setFee(new BigDecimal(closePaymentRequest.getFee()).divide(new BigDecimal(100)));
		nodeClosePaymentRequest.setTimestampOperation(closePaymentRequest.getTimestampOp());

		nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

		return nodeClosePaymentRequest;
	}

}
