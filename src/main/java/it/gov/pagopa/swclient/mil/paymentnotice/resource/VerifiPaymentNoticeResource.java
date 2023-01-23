package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.VerifyNoticeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;

@Path("/")
public class VerifiPaymentNoticeResource extends PaymentResource {
	@GET
	@Path("paymentNotices/{qrCode}")
	public Uni<Response> verifyPaymentNoticeByQrCode(@Valid @BeanParam CommonHeader headers, 
			@Pattern(regexp = PaymentNoticeConstants.QRCODE_REGEX, message = "[" + ErrorCode.QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			@PathParam(value = "qrCode") String qrCode) {
		
		Log.debugf("verifyPaymentNoticeByQrCode - Input parameters: %s, qrCode: %s", headers, qrCode);
		
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
		}).chain(f -> manageRequestToNodo(ctQrCode, f, headers.getChannel()));
		
	}
	
	@GET
	@Path("paymentNotices/{paTaxCode}/{noticeNumber}")
	public Uni<Response> verifyPaymetNoticeByPaxCodeAndNoticeNumber(@Valid @BeanParam CommonHeader headers, 
			
			@Pattern(regexp = PaymentNoticeConstants.PAX_TAX_CODE_REGEX, message = "[" + ErrorCode.PAX_TAX_CODE_MUST_MATCH_REGEXP + "] paxTaxCode must match \"{regexp}\"")
			@PathParam(value = "paxTaxCode") String paxTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX, message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			@PathParam(value = "noticeNumber") String noticeNumber) {
		
		Log.debugf("verifyPaymetNoticeByPaxCodeAndNoticeNumber - Input parameters: %s, paxTaxCode: %s, noticeNumber", headers, paxTaxCode, noticeNumber);
		
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(paxTaxCode); //paTaxCode
		ctQrCode.setNoticeNumber(noticeNumber); //noticeNumber
		
		return manageFindPspInfoByAcquirerId(headers.getAcquirerId()).onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error retrieving pspInfo", ErrorCode.ERROR_READING_PSP_INFO);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_READING_PSP_INFO)))
					.build());
		}).chain(f -> manageRequestToNodo(ctQrCode, f, headers.getChannel()));
		
	}
	
	private Uni<Response> manageRequestToNodo(CtQrCode ctQrCode, PspInfo pspInfo, String channel) {
		
		VerifyPaymentNoticeReq verifyPaymentNoticeReq = new VerifyPaymentNoticeReq();
		verifyPaymentNoticeReq.setIdPSP(pspInfo.getPspId());
		verifyPaymentNoticeReq.setIdBrokerPSP(pspInfo.getPspBroker());
		verifyPaymentNoticeReq.setIdChannel(channel);
		verifyPaymentNoticeReq.setPassword(pspInfo.getPspPassword());
		verifyPaymentNoticeReq.setQrCode(ctQrCode);
		
		Uni<VerifyPaymentNoticeRes> response = wrapper.verifyPaymentNotice(verifyPaymentNoticeReq);
		
		return response.onFailure().transform(t-> 
		{
			Log.errorf(t, "[%s] Error calling tokenizator service ", ErrorCode.ERROR_NODE);
			return new InternalServerErrorException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new Errors(List.of(ErrorCode.ERROR_NODE)))
					.build());
		}).map(f -> {
			if (Outcome.OK.toString().equals(f.getOutcome().value())) {
				VerifyNoticeResponse verifyNoticeResponse = buildResponseOk(f);
				Log.debugf("Response %s", verifyNoticeResponse.toString());
				return Response.status(Status.OK).entity(verifyNoticeResponse).build();
			} else {
				VerifyNoticeResponse verifyNoticeResponse = buildResponseKo(f);
				Log.debugf("Response %s", verifyNoticeResponse.toString());
				return Response.status(Status.OK).entity(verifyNoticeResponse).build();
			}

		});
	}

	private VerifyNoticeResponse buildResponseOk(VerifyPaymentNoticeRes response) {
		VerifyNoticeResponse verifyNoticeResponse = new VerifyNoticeResponse();
		verifyNoticeResponse.setOutcome(response.getOutcome().value());
		verifyNoticeResponse.setDescription(response.getPaymentDescription());
		verifyNoticeResponse.setCompany(response.getCompanyName());
		verifyNoticeResponse.setOffice(response.getOfficeName());
		//if a list is returned, the first one is got
		if (response.getPaymentList().getPaymentOptionDescription() != null) {
			CtPaymentOptionDescription paymentOptionDesctiption = response.getPaymentList().getPaymentOptionDescription().get(0);
			verifyNoticeResponse.setAmount(paymentOptionDesctiption.getAmount());
			verifyNoticeResponse.setDueDate(paymentOptionDesctiption.getDueDate().toString());
			verifyNoticeResponse.setNote(paymentOptionDesctiption.getPaymentNote());
		}
		return verifyNoticeResponse;
	}
	private VerifyNoticeResponse buildResponseKo(VerifyPaymentNoticeRes response) {
		VerifyNoticeResponse verifyNoticeResponse = new VerifyNoticeResponse();
		verifyNoticeResponse.setOutcome(response.getOutcome().value());
		
		return verifyNoticeResponse;
	}
}
