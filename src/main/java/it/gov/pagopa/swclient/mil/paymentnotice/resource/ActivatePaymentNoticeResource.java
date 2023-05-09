package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentNoticeService;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeApi;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.QrCode;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.QrCodeParser;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;

@Path("/paymentNotices")
public class ActivatePaymentNoticeResource extends BasePaymentResource {

	@Inject
	QrCodeParser qrCodeParser;

	@Inject
	PaymentNoticeService paymentNoticeService;

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
	 * @param b64UrlQrCode the base64url encoded qr-code
	 * @param activatePaymentNoticeRequest an {@link ActivatePaymentNoticeRequest} containing the amount and the idempotency key
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	@PATCH
	@Path("/{qrCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> activateByQrCode(
			@Valid @BeanParam CommonHeader headers,

			@Pattern(regexp = PaymentNoticeConstants.ENCODED_QRCODE_REGEX,
					message = "[" + ErrorCode.ENCODED_QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			@PathParam(value = "qrCode") String b64UrlQrCode,

			@Valid
			@NotNull(message = "[" + ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
			ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		Log.debugf("activateByQrCode - Input parameters: %s, b64UrlQrCode: %s, request: %s", headers, b64UrlQrCode, activatePaymentNoticeRequest);

		// parse qr-code to retrieve the notice number and the PA tax code
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(b64UrlQrCode);
		Log.debugf("Decoded qrCode: %s", parsedQrCode);

		return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.ACTIVATE).
				chain(pspConf -> callNodeActivatePaymentNotice(parsedQrCode.getPaTaxCode(), parsedQrCode.getNoticeNumber(),
						pspConf, activatePaymentNoticeRequest));

	}


	/**
	 * Activate a payment notice by its number and the tax code of the company
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param activatePaymentNoticeRequest an {@link ActivatePaymentNoticeRequest} containing the amount and the idempotency key
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	@PATCH
	@Path("/{paTaxCode}/{noticeNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> activateByTaxCodeAndNoticeNumber(
			@Valid @BeanParam CommonHeader headers,
			
			@Pattern(regexp = PaymentNoticeConstants.PA_TAX_CODE_REGEX,
					message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
			@PathParam(value = "paTaxCode") String paTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX,
					message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			@PathParam(value = "noticeNumber") String noticeNumber,

			@Valid
			@NotNull(message = "[" + ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
			ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		Log.debugf("activateByTaxCodeAndNoticeNumber - Input parameters: %s, paTaxCode: %s, noticeNumber, body: %s",
				headers, paTaxCode, noticeNumber, activatePaymentNoticeRequest);

		return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.VERIFY).
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
		nodeActivateRequest.setIdPSP(pspConfiguration.getPsp());
		nodeActivateRequest.setIdBrokerPSP(pspConfiguration.getBroker());
		nodeActivateRequest.setIdChannel(pspConfiguration.getChannel());
		nodeActivateRequest.setPassword(pspConfiguration.getPassword());

		nodeActivateRequest.setIdempotencyKey(activatePaymentNoticeRequest.getIdempotencyKey());

		nodeActivateRequest.setQrCode(ctQrCode);

		nodeActivateRequest.setExpirationTime(paymentNoticeExpirationTime);
		// conversion from euro cents to euro
		nodeActivateRequest.setAmount(BigDecimal.valueOf(activatePaymentNoticeRequest.getAmount(), 2));

		return nodeWrapper.activatePaymentNoticeV2Async(nodeActivateRequest)
				.onFailure().transform(t-> {
					Log.errorf(t, "[%s] Error calling the node activatePaymentNoticeV2 service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)))
							.build());
				})
				.chain(nodeResponse -> storeNoticeData(noticeNumber, nodeResponse));
	}

	/**
	 * Store the payment notice data in the redis cache
	 *
	 * @param noticeNumber the identifier of the payment notice
	 * @param activateResponse the response of the activatePaymentNoticeV2Async
	 * @return a {@link Uni} emitting Void
	 */
	private Uni<Response> storeNoticeData(String noticeNumber, ActivatePaymentNoticeV2Response activateResponse) {

		Uni<ActivatePaymentNoticeV2Response> response;
		if (Outcome.OK.name().equals(activateResponse.getOutcome().value())) {
			Notice notice = new Notice();
			notice.setPaymentToken(activateResponse.getPaymentToken());
			notice.setPaTaxCode(activateResponse.getFiscalCodePA());
			notice.setNoticeNumber(noticeNumber);
			notice.setAmount(activateResponse.getTotalAmount().scaleByPowerOfTen(2).longValue());
			notice.setDescription(activateResponse.getPaymentDescription());
			notice.setCompany(activateResponse.getCompanyName());
			notice.setOffice(activateResponse.getOfficeName());

			response = paymentNoticeService.set(activateResponse.getPaymentToken(), notice)
					.onFailure().transform(t-> {
						Log.errorf(t, "[%s] Error while storing payment data in cache", ErrorCode.ERROR_STORING_DATA_INTO_REDIS);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_INTO_REDIS)))
								.build());
					})
					.replaceWith(activateResponse);

		}
		else response = Uni.createFrom().item(activateResponse);

		return response
				.map(nodeResponse -> {
					ActivatePaymentNoticeResponse activatePaymentNoticeResponse;
					if (Outcome.OK.name().equals(nodeResponse.getOutcome().value())) {
						activatePaymentNoticeResponse = buildResponseOk(nodeResponse);
					} else {
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
