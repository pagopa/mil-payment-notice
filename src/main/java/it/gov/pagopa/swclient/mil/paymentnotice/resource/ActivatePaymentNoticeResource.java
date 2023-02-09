package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.gov.pagopa.swclient.mil.paymentnotice.utils.QrCode;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;

@Path("/paymentNotices")
public class ActivatePaymentNoticeResource extends BasePaymentResource {

	/**
	 * The expiration time of the payment token passed to the node
	 */
	@ConfigProperty(name="paymentnotice.activatepayment.expiration-time")
	BigInteger paymentNoticeExpirationTime;


	/**
	 * Activate a payment notice by its qr-code.
	 * The qr code contains, encoded, the tax code of the company and the payment notice number
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param qrCode the qr-code
	 * @param activatePaymentNoticeRequest an {@link ActivatePaymentNoticeRequest}
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	@PATCH
	@Path("/{qrCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> activateByQrCode(@Valid @BeanParam CommonHeader headers,
			@Pattern(regexp = PaymentNoticeConstants.QRCODE_REGEX, message = "[" + ErrorCode.QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			@PathParam(value = "qrCode") String qrCode,
			@Valid ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		Log.debugf("activateByQrCode - Input parameters: %s, qrCode: %s, request: %s", headers, qrCode, activatePaymentNoticeRequest);

		// parse qr-code to retrieve the notice number and the PA tax code
		QrCode parsedQrCode = QrCode.parse(qrCode);

		return retrievePSPConfiguration(headers.getAcquirerId()).
				chain(pspConf -> callNodeActivatePaymentNotice(parsedQrCode.getPaTaxCode(), parsedQrCode.getNoticeNumber(),
						pspConf, activatePaymentNoticeRequest));

	}


	/**
	 * Activate a payment notice by its number and the tax code of the company
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	@PATCH
	@Path("/{paTaxCode}/{noticeNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> activateByTaxCodeAndNoticeNumber(@Valid @BeanParam CommonHeader headers,
			
			@Pattern(regexp = PaymentNoticeConstants.PA_TAX_CODE_REGEX, message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
			@PathParam(value = "paTaxCode") String paTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX, message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			@PathParam(value = "noticeNumber") String noticeNumber,
			
			@Valid ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		Log.debugf("activateByTaxCodeAndNoticeNumber - Input parameters: %s, paTaxCode: %s, noticeNumber, body: %s",
				headers, paTaxCode, noticeNumber, activatePaymentNoticeRequest);

		return retrievePSPConfiguration(headers.getAcquirerId()).
				chain(pspConf -> callNodeActivatePaymentNotice(paTaxCode, noticeNumber, pspConf, activatePaymentNoticeRequest));
	}


	/**
	 * Branch of the activatePaymentNotice Uni that retrieves the payment notice detail from the node
	 *
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @param activatePaymentNoticeRequest the object received in request of the activatePaymentNotice
	 * @return a {@link Uni} emitting an {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	private Uni<Response> callNodeActivatePaymentNotice(String paTaxCode, String noticeNumber, PspConfiguration pspConfiguration,
														ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(paTaxCode);
		ctQrCode.setNoticeNumber(noticeNumber);

		ActivatePaymentNoticeV2Request nodeActivateRequest = new ActivatePaymentNoticeV2Request();
		nodeActivateRequest.setIdPSP(pspConfiguration.getPspId());
		nodeActivateRequest.setIdBrokerPSP(pspConfiguration.getPspBroker());
		nodeActivateRequest.setIdChannel(pspConfiguration.getIdChannel());
		nodeActivateRequest.setPassword(pspConfiguration.getPspPassword());

		nodeActivateRequest.setIdempotencyKey(activatePaymentNoticeRequest.getIdempotencyKey());

		nodeActivateRequest.setQrCode(ctQrCode);

		nodeActivateRequest.setExpirationTime(paymentNoticeExpirationTime);
		// conversion from euro cents to euro
		nodeActivateRequest.setAmount(new BigDecimal(activatePaymentNoticeRequest.getAmount()).divide(new BigDecimal(100), RoundingMode.HALF_DOWN));

		return nodeWrapper.activatePaymentNoticeV2Async(nodeActivateRequest)
				.onFailure().transform(t-> {
					Log.errorf(t, "[%s] Error calling the node activatePaymentNoticeV2 service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)))
							.build());
				})
				.map(nodeResponse -> {
					ActivatePaymentNoticeResponse activatePaymentNoticeResponse;
					if (Outcome.OK.toString().equals(nodeResponse.getOutcome().value())) {
						 activatePaymentNoticeResponse = buildResponseOk(nodeResponse);
					}
					else {
						 activatePaymentNoticeResponse = buildResponseKo(nodeResponse);
					}
					Log.debugf("verifyPaymentNotice response %s", activatePaymentNoticeResponse.toString());
					return Response.status(Status.OK).entity(activatePaymentNoticeResponse).build();
		});
	}

	/**
	 * Builds the OK response of the activatePayment API based on the response from the node
	 *
	 * @param response the {@link ActivatePaymentNoticeV2Response} from the node
	 * @return the {@link ActivatePaymentNoticeResponse} to be returned by the API
	 */
	private ActivatePaymentNoticeResponse buildResponseOk(ActivatePaymentNoticeV2Response response) {
		ActivatePaymentNoticeResponse activateResponse = new ActivatePaymentNoticeResponse();
		activateResponse.setOutcome(response.getOutcome().value());
		// conversion from euro cents to euro
		activateResponse.setAmount(response.getTotalAmount().multiply(new BigDecimal(100)).toBigInteger());
		activateResponse.setPaTaxCode(response.getFiscalCodePA());
		activateResponse.setPaymentToken(response.getPaymentToken());
		List<Transfer> transfers = new ArrayList<>();
		response.getTransferList().getTransfer().forEach(t -> {
			Transfer transfer = new Transfer();
			transfer.setPaTaxCode(t.getFiscalCodePA());
			transfer.setCategory(StringUtils.EMPTY); // Currently the node doesn't return the category, will return empty string
			transfers.add(transfer);
		});
		activateResponse.setTransfers(transfers);
		
		return activateResponse;
	}

	/**
	 * Builds the KO response of the activatePayment API based on the response from the node
	 *
	 * @param response the {@link ActivatePaymentNoticeV2Response} from the node
	 * @return the {@link ActivatePaymentNoticeResponse} to be returned by the API
	 */
	private ActivatePaymentNoticeResponse buildResponseKo(ActivatePaymentNoticeV2Response response) {

		ActivatePaymentNoticeResponse activatePaymentNoticeResponse = new ActivatePaymentNoticeResponse();
		activatePaymentNoticeResponse.setOutcome(
				remapNodeFaultToOutcome(
						response.getFault().getFaultCode(),
						response.getFault().getOriginalFaultCode()
				));
		
		return activatePaymentNoticeResponse;
	}
}
