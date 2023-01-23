package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentV2Rsponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;

@Path("/")
public class ActivatePaymentResource extends PaymentResource {
	
	@ConfigProperty(name="activate.paymentnotice.expiration.time")
	private BigInteger paymentNoticeExpirationTime;
	

	@PATCH
	@Path("/paymentNotices/{qrCode}")
	public Uni<Response> activatePaymentNoticeByQrCode(@Valid @BeanParam CommonHeader headers, 
			@Pattern(regexp = PaymentNoticeConstants.QRCODE_REGEX, message = "[" + ErrorCode.QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			@PathParam(value = "qrCode") String qrCode,
			@Valid ActivatePaymentBody activatePaymentBody) {
		Log.debugf("ActivatePaymentNoticeByQrCode - Input parameters: %s, qrCode: %s, body: %s", headers, qrCode, activatePaymentBody);
		
		Log.debugf("Parsing qrCode");
		Tuple2<String,String> tuple = parseQrCode(qrCode);
		Log.debugf("QrCode parsed FiscalCode=%s NoticeNumber=%s",tuple.getItem2(),tuple.getItem1());
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(tuple.getItem2()); //paTaxCode
		ctQrCode.setNoticeNumber(tuple.getItem2()); //noticeNumber
		return manageFindPspInfoByAcquirerId(headers.getAcquirerId()).onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error retrieving pspInfo", ErrorCode.ERROR_READING_PSP_INFO);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_READING_PSP_INFO)))
					.build());
		}).chain(f -> manageRequestToNodo(ctQrCode, f, headers.getChannel(),activatePaymentBody));
		
	}
	
	@PATCH
	@Path("paymentNotices/{paTaxCode}/{noticeNumber}")
	public Uni<Response> activatePaymentNoticeByPaxCodeAndNoticeNumber(@Valid @BeanParam CommonHeader headers, 
			
			@Pattern(regexp = PaymentNoticeConstants.PAX_TAX_CODE_REGEX, message = "[" + ErrorCode.PAX_TAX_CODE_MUST_MATCH_REGEXP + "] paxTaxCode must match \"{regexp}\"")
			@PathParam(value = "paxTaxCode") String paxTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX, message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			@PathParam(value = "noticeNumber") String noticeNumber,
			
			@Valid ActivatePaymentBody activatePaymentBody) {
		Log.debugf("activatePaymentNoticeByPaxCodeAndNoticeNumber - Input parameters: %s, paxTaxCode: %s, noticeNumber, body: %s", headers, paxTaxCode, noticeNumber,activatePaymentBody);
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(paxTaxCode); 
		ctQrCode.setNoticeNumber(noticeNumber);
		return manageFindPspInfoByAcquirerId(headers.getAcquirerId()).onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error retrieving pspInfo", ErrorCode.ERROR_READING_PSP_INFO);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_READING_PSP_INFO)))
					.build());
		}).chain(f -> manageRequestToNodo(ctQrCode, f, headers.getChannel(),activatePaymentBody));
	}
	
	private Uni<Response> manageRequestToNodo(CtQrCode ctQrCode, 
											  PspInfo pspInfo, 
											  String channel,
											  ActivatePaymentBody activatePaymentBody) {
		
		ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request = new ActivatePaymentNoticeV2Request();
		activatePaymentNoticeV2Request.setIdPSP(pspInfo.getPspId());
		activatePaymentNoticeV2Request.setIdBrokerPSP(pspInfo.getPspBroker());
		activatePaymentNoticeV2Request.setIdChannel(channel);
		activatePaymentNoticeV2Request.setPassword(pspInfo.getPspPassword());
		activatePaymentNoticeV2Request.setIdempotencyKey(activatePaymentBody.getIdempotencyKey());
		activatePaymentNoticeV2Request.setQrCode(ctQrCode);
		activatePaymentNoticeV2Request.setExpirationTime(paymentNoticeExpirationTime);	
		activatePaymentNoticeV2Request.setAmount(new BigDecimal(activatePaymentBody.getAmount()));
		
		Uni<ActivatePaymentNoticeV2Response> response = wrapper.activatePaymentNoticeV2Async(activatePaymentNoticeV2Request);
		
		return response.onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error calling tokenizator service ", ErrorCode.ERROR_NODE);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_NODE)))
					.build());
		}).map(f -> {
			if (Outcome.OK.toString().equals(f.getOutcome().value())) {
				ActivatePaymentV2Rsponse activatePaymentV2Rsponse = buildResponseOk(f);
				Log.debugf("Response %s", activatePaymentV2Rsponse.toString());
				return Response.status(Status.OK).entity(activatePaymentV2Rsponse).build();
			} else {
				ActivatePaymentV2Rsponse activatePaymentV2Rsponse = buildResponseKo(f);
				Log.debugf("Response %s", activatePaymentV2Rsponse.toString());
				return Response.status(Status.OK).entity(activatePaymentV2Rsponse).build();
			}

		});
	}
	
	private ActivatePaymentV2Rsponse buildResponseOk(ActivatePaymentNoticeV2Response response) {
		ActivatePaymentV2Rsponse activatePaymentV2Rsponse = new ActivatePaymentV2Rsponse();
		activatePaymentV2Rsponse.setOutcome(response.getOutcome().value());
		activatePaymentV2Rsponse.setAmount(response.getTotalAmount());
		activatePaymentV2Rsponse.setPaTaxCode(response.getFiscalCodePA());
		activatePaymentV2Rsponse.setPaymentToken(response.getPaymentToken());
		List<Transfer> transfers = new ArrayList<>();
		response.getTransferList().getTransfer().forEach(t -> {
			Transfer transfer = new Transfer();
			transfer.setPaTaxCode(t.getFiscalCodePA());
			transfer.setCategory("CAT_");//Currently the node doesn't return the category for each transfer
			transfers.add(transfer);
		});
		activatePaymentV2Rsponse.setTransfers(transfers);
		
		return activatePaymentV2Rsponse;
	}
	private ActivatePaymentV2Rsponse buildResponseKo(ActivatePaymentNoticeV2Response response) {
		ActivatePaymentV2Rsponse activatePaymentV2Rsponse = new ActivatePaymentV2Rsponse();
		activatePaymentV2Rsponse.setOutcome(response.getOutcome().value());
		
		return activatePaymentV2Rsponse;
	}
}
