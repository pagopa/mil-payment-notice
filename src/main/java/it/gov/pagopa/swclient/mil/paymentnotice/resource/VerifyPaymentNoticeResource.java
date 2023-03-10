package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.VerifyPaymentNoticeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeForPspLoggingUtil;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeApi;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.QrCode;

@Path("/paymentNotices")
public class VerifyPaymentNoticeResource extends BasePaymentResource {

	/**
	 * Retrieve the data of a payment notice by its qr-code.
	 * The qr code contains, encoded, the tax code of the company and the payment notice number
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param qrCode the qr-code
	 * @return a {@link VerifyPaymentNoticeResponse} containing the data of the payment notice retrieved from the node
	 */
	@GET
	@Path("/{qrCode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> verifyByQrCode(@Valid @BeanParam CommonHeader headers,
			@Pattern(regexp = PaymentNoticeConstants.QRCODE_REGEX, message = "[" + ErrorCode.QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			@PathParam(value = "qrCode") String qrCode) {
		
		Log.debugf("verifyPaymentNoticeByQrCode - Input parameters: %s, qrCode: %s", headers, qrCode);

		// parse qr-code to retrieve the notice number and the PA tax code
		QrCode parsedQrCode = QrCode.parse(qrCode);

		return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.VERIFY).
				chain(pspConf -> callNodeVerifyPaymentNotice(parsedQrCode.getPaTaxCode(), parsedQrCode.getNoticeNumber(), pspConf));

	}


	/**
	 * Retrieve the data of a payment notice by its number and the tax code of the company
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @return a {@link VerifyPaymentNoticeResponse} containing the data of the payment notice retrieved from the node
	 */
	@GET
	@Path("/{paTaxCode}/{noticeNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> verifyByTaxCodeAndNoticeNumber(@Valid @BeanParam CommonHeader headers,
			
			@Pattern(regexp = PaymentNoticeConstants.PA_TAX_CODE_REGEX, message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
			@PathParam(value = "paTaxCode") String paTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX, message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			@PathParam(value = "noticeNumber") String noticeNumber) {
		
		Log.debugf("verifyByTaxCodeAndNoticeNumber - Input parameters: %s, paTaxCode: %s, noticeNumber", headers, paTaxCode, noticeNumber);

		return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.VERIFY).
				chain(pspConf -> callNodeVerifyPaymentNotice(paTaxCode, noticeNumber, pspConf));

	}


	/**
	 * Branch of the verifyPaymentNotice Uni that retrieves the payment notice detail from the node
	 *
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @return an {@link Uni} emitting a {@link VerifyPaymentNoticeResponse} containing the data retrieved from the node
	 */
	private Uni<Response> callNodeVerifyPaymentNotice(String paTaxCode, String noticeNumber, PspConfiguration pspConfiguration) {

		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(paTaxCode);
		ctQrCode.setNoticeNumber(noticeNumber);

		VerifyPaymentNoticeReq verifyPaymentNoticeReq = new VerifyPaymentNoticeReq();

		verifyPaymentNoticeReq.setIdPSP(pspConfiguration.getPsp());
		verifyPaymentNoticeReq.setIdBrokerPSP(pspConfiguration.getBroker());
		verifyPaymentNoticeReq.setIdChannel(pspConfiguration.getChannel());
		verifyPaymentNoticeReq.setPassword(pspConfiguration.getPassword());

		verifyPaymentNoticeReq.setQrCode(ctQrCode);

		return nodeWrapper.verifyPaymentNotice(verifyPaymentNoticeReq)
				.onFailure().transform(t -> {
					Log.errorf(t, "[%s] Error calling the node verifyPaymentNotice service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)))
							.build());
				})
				.map(nodeResponse -> {
					VerifyPaymentNoticeResponse verifyPaymentNoticeResponse;
					if (StOutcome.OK.name().equals(nodeResponse.getOutcome().name())) {
						Log.debugf("Node verifyPaymentNotice responded with outcome OK, %s",
								NodeForPspLoggingUtil.toString(nodeResponse));
						verifyPaymentNoticeResponse = buildResponseOk(nodeResponse);
					}
					else {
						Log.debugf("Node verifyPaymentNotice responded with outcome KO, %s",
								NodeForPspLoggingUtil.toString(nodeResponse.getFault()));
						verifyPaymentNoticeResponse = buildResponseKo(nodeResponse);
					}
					Log.debugf("verifyPaymentNotice response %s", verifyPaymentNoticeResponse.toString());
					return Response.status(Status.OK).entity(verifyPaymentNoticeResponse).build();
				});
	}


	/**
	 * Builds the OK response of the verifyPayment API based on the response from the node
	 *
	 * @param response the {@link VerifyPaymentNoticeRes} from the node
	 * @return the OK {@link VerifyPaymentNoticeResponse} to be returned by the API
	 */
	private VerifyPaymentNoticeResponse buildResponseOk(VerifyPaymentNoticeRes response) {
		VerifyPaymentNoticeResponse verifyResponse = new VerifyPaymentNoticeResponse();
		verifyResponse.setOutcome(response.getOutcome().value());
		verifyResponse.setDescription(response.getPaymentDescription());
		verifyResponse.setCompany(response.getCompanyName());
		verifyResponse.setOffice(response.getOfficeName());
		// only the first element of the payment list is returned
		if (response.getPaymentList().getPaymentOptionDescription() != null) {
			CtPaymentOptionDescription paymentOptionDescription = response.getPaymentList().getPaymentOptionDescription().get(0);
			Log.debugf("Node verifyPaymentNotice responded with , %s",
					NodeForPspLoggingUtil.toString(paymentOptionDescription));
			// conversion from euro to euro cents
			verifyResponse.setAmount(paymentOptionDescription.getAmount().multiply(new BigDecimal(100)).toBigInteger());
			verifyResponse.setDueDate(paymentOptionDescription.getDueDate().toString());
			verifyResponse.setNote(paymentOptionDescription.getPaymentNote());
		}
		return verifyResponse;
	}


	/**
	 * Builds the KO response of the verifyPayment API based on the response from the node
	 *
	 * @param response the {@link VerifyPaymentNoticeRes} from the node
	 * @return the KO {@link VerifyPaymentNoticeResponse} to be returned by the API
	 */
	private VerifyPaymentNoticeResponse buildResponseKo(VerifyPaymentNoticeRes response) {
		VerifyPaymentNoticeResponse verifyResponse = new VerifyPaymentNoticeResponse();
		verifyResponse.setOutcome(
				remapNodeFaultToOutcome(
						response.getFault().getFaultCode(),
						response.getFault().getOriginalFaultCode()
				));
		return verifyResponse;
	}

}
