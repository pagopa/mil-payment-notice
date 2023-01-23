package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.BodyToNodeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentRequestBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentsPathParams;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodoService;
import it.gov.pagopa.swclient.mil.paymentnotice.exception.NodeExceptionManageKo;
import it.gov.pagopa.swclient.mil.paymentnotice.exception.NodeExceptionManageOk;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.RedisPaymentService;

@Path("/")
public class ClosePaymentResource extends PaymentResource {
	
	@ConfigProperty(name="activate.paymentnotice.expiration.time")
	private BigInteger paymentNoticeExpirationTime;
	
	@ConfigProperty(name="paymentnotice.retry.after")
	private int retryAfter;
	
	@ConfigProperty(name="paymentnotice.max.retry")
	private int maxRetry;
	
	@Inject
	private RedisPaymentService redisPaymentService;
	
	@RestClient
	private NodoService nodoService;

	@POST
	@Path("/payment")
	public Uni<Response> closePaymentNotice(@Valid @BeanParam CommonHeader headers, 
			@Valid PaymentRequestBody paymentRequestBody) {
		Log.debugf("closePaymentNotice - Input parameters: %s, body: %s", headers, paymentRequestBody);
		
		return manageFindPspInfoByAcquirerId(headers.getAcquirerId()).onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error retrieving pspInfo", ErrorCode.ERROR_READING_PSP_INFO);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_READING_PSP_INFO)))
					.build());
		}).chain(f -> manageRequestToNodo(paymentRequestBody,f, headers));
		
	}
	
	@GET
	@Path("/payments/{transactionId}")
	public Uni<Response> retrievePaymentsResultByTransactionId(@Valid @BeanParam CommonHeader headers, @Valid PaymentsPathParams paymentPathParams) {
		Log.debugf("retrievePaymentsResultByTransactionId - Input parameters: %s, path params: %s", headers, paymentPathParams);
		
		return retrievePaymentBodyFromCacheByTransactionId(paymentPathParams.getTransactionId())
				.map(f -> Response.status(Status.OK).entity(f).build());
	}
	
	
	@POST
	@Path("/payments/{transactionId}")
	public Uni<Response> setPaymentsResultByTransactionId( @Valid PaymentsPathParams paymentPathParams, @Valid PaymentBody paymentBody) {
		Log.debugf("setPaymentsResultByTransactionId - Input path params: %s, body: %s", paymentPathParams, paymentBody);
		
		return redisPaymentService.set(paymentPathParams.getTransactionId(), paymentBody)
			.onFailure().transform(t -> {
				Log.errorf(t, "[%s] REDIS error saving session in cache", ErrorCode.REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT);
				return new InternalServerErrorException(Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(new Errors(List.of(ErrorCode.REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT)))
						.build());
			}).map(f -> {
				BodyToNodeResponse bodyResponse = new BodyToNodeResponse();
				bodyResponse.setOutcome(Outcome.OK.toString());
				return Response.status(Status.OK).entity(bodyResponse).build();
			});
	}
	
	private Uni<Response> manageRequestToNodo(PaymentRequestBody paymentRequestBody, PspInfo pspInfo, CommonHeader headers) {
		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		
		closePaymentRequest.setPaymentTokens(paymentRequestBody.getPaymentTokens());
		closePaymentRequest.setOutcome(Outcome.OK.toString()); 	
		closePaymentRequest.setIdPsp(pspInfo.getPspId());
		closePaymentRequest.setPspBroker(pspInfo.getPspBroker());
		closePaymentRequest.setIdChannel(headers.getChannel());
		closePaymentRequest.setPaymentMethod(paymentRequestBody.getPaymentMethod());
		closePaymentRequest.setTransactionId(paymentRequestBody.getTransactionId());
		closePaymentRequest.setTotalAmount(String.valueOf(paymentRequestBody.getTotalAmount()));
		closePaymentRequest.setFee(String.valueOf(paymentRequestBody.getFee()));
		closePaymentRequest.setTimestampOperation(paymentRequestBody.getTimestampOp());

		Log.debugf("Calling nodo service");
		return nodoService.closePayment(closePaymentRequest)
			.onItemOrFailure()
			.transform((success,error) -> {
				if(error != null) {
					if (error instanceof NodeExceptionManageKo) {
						
						return Response.status(Status.OK).entity(new ClosePaymentResponse(Outcome.KO.toString())).build();
					} 
					else if (error instanceof  NodeExceptionManageOk || 
							error instanceof TimeoutException) {
						return Response.status(Status.OK)
								.header("Location", "/payment/"+ paymentRequestBody.getTransactionId())
				    			.header("Retry-After", retryAfter)
				    			.header("Max-Retry", maxRetry).build();
					}else {
				    	return Response.status(Status.INTERNAL_SERVER_ERROR)
				    			.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SERVICE))).build();
					}
			    } else {
			    	return Response.status(Status.OK)
			    			.header("Location", "/payment/"+ paymentRequestBody.getTransactionId())
			    			.header("Retry-After", retryAfter)
			    			.header("Max-Retry", maxRetry).build();
			    }
			});

	}
	
	private Uni<PaymentBody> retrievePaymentBodyFromCacheByTransactionId(String transactionId) {
		return redisPaymentService.get(transactionId)
			.onFailure().transform(t -> {
				Log.errorf(t, "[%s] REDIS error while retrieving body by transactionId", ErrorCode.REDIS_ERROR_WHILE_RETRIEVING_PAYMENT_RESULT);
				return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.REDIS_ERROR_WHILE_RETRIEVING_PAYMENT_RESULT)))
					.build());
			})
			.onItem().ifNull().failWith(() ->{
					Log.errorf("[%s] REDIS transactionId not found", ErrorCode.REDIS_ERROR_PAYMENT_RESULT_WITH_TRANSACTION_ID_NOT_FOUND);
					return new NotFoundException(Response
						.status(Status.NOT_FOUND)
						.entity(new Errors(List.of(ErrorCode.REDIS_ERROR_PAYMENT_RESULT_WITH_TRANSACTION_ID_NOT_FOUND)))
						.build());
			});
	}
}
