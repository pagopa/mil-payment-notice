package it.gov.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferListPSPV2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferPSPV2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivatePaymentNoticeResourceTest {

	@InjectMock
	NodeForPspWrapper pnWrapper;

	@InjectMock
	@RestClient
	MilRestService milRestService;

	ActivatePaymentNoticeV2Response nodeActivateResponseOk;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> validMilHeaders;

	ActivatePaymentNoticeRequest validActivateRequest;

	String encodedQrCode;

	@BeforeAll
	void createTestObjects() {

		// encoded valid qr-code
		byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode(PaymentTestData.QR_CODE.getBytes(StandardCharsets.UTF_8));
		encodedQrCode = new String(bytes, StandardCharsets.UTF_8);

		// valid mil headers
		validMilHeaders = PaymentTestData.getMilHeaders(true, true);

		// acquirer PSP configuration
		acquirerConfiguration = PaymentTestData.getAcquirerConfiguration();

		// valid activate request
		validActivateRequest = PaymentTestData.getActivatePaymentRequest();

		// node activate response OK

		//		<nfp:activatePaymentNoticeV2Response>
		//			<outcome>OK</outcome>
		//			<totalAmount>100.00</totalAmount>
		//			<paymentDescription>TARI 2021</paymentDescription>
		//			<fiscalCodePA>77777777777</fiscalCodePA>
		//			<companyName>company PA</companyName>
		//			<officeName>office PA</officeName>
		//			<paymentToken>3a254e00347d4f29a3607a35d780faac</paymentToken>
		//			<transferList>
		//				<transfer>
		//					<idTransfer>1</idTransfer>
		//					<transferAmount>100.00</transferAmount>
		//					<fiscalCodePA>77777777777</fiscalCodePA>
		//					<IBAN>IT30N0103076271000001823603</IBAN>
		//					<remittanceInformation>TARI Comune EC_TE</remittanceInformation>
		//				</transfer>
		//			</transferList>
		//			<creditorReferenceId>02051234567890124</creditorReferenceId>
		//		</nfp:activatePaymentNoticeV2Response>

		CtTransferPSPV2 transfer1 = new CtTransferPSPV2();
		transfer1.setIdTransfer(1);
		transfer1.setTransferAmount(new BigDecimal("99.98"));
		transfer1.setFiscalCodePA("77777777777");
		transfer1.setIBAN("IT30N0103076271000001823603");
		transfer1.setRemittanceInformation("TARI Comune EC_TE");
		// TODO: missing category in wsdl

		CtTransferPSPV2 transfer2 = new CtTransferPSPV2();
		transfer2.setIdTransfer(1);
		transfer2.setTransferAmount(new BigDecimal("1.01"));
		transfer2.setFiscalCodePA("77777777777");
		transfer2.setIBAN("IT30N0103076271000001823603");
		transfer2.setRemittanceInformation("TARI Comune EC_TE");
		// TODO: missing category in wsdl

		CtTransferListPSPV2 transferList = new CtTransferListPSPV2();
		transferList.getTransfer().add(0, transfer1);
		transferList.getTransfer().add(1, transfer2);

		nodeActivateResponseOk = new ActivatePaymentNoticeV2Response();
		nodeActivateResponseOk.setOutcome(StOutcome.OK);
		nodeActivateResponseOk.setTotalAmount(new BigDecimal("100.99"));
		nodeActivateResponseOk.setPaymentDescription("TARI 2021");
		nodeActivateResponseOk.setFiscalCodePA("77777777777");
		nodeActivateResponseOk.setCompanyName("company PA");
		nodeActivateResponseOk.setOfficeName("officeName");
		nodeActivateResponseOk.setPaymentToken("3a254e00347d4f29a3607a35d780faac");
		nodeActivateResponseOk.setTransferList(transferList);
		nodeActivateResponseOk.setCreditorReferenceId("02051234567890124");

	}

	private ActivatePaymentNoticeV2Response generateKoNodeResponse(String faultCode, String originalFaultCode) {

		// node verify response KO

		//		<nfp:activatePaymentNoticeV2Response>
		//			<outcome>KO</outcome>
		//			<fault>
		//				<faultCode>PPT_SINTASSI_EXTRAXSD</faultCode>
		//				<faultString>Errore di sintassi extra XSD.</faultString>
		//				<id>NodoDeiPagamentiSPC</id>
		//				<description>Errore validazione XML [Envelope/Body/verifyPaymentNoticeReq/qrCode/noticeNumber] -
		//					cvc-pattern-valid: il valore &quot;30205&quot; non è valido come facet rispetto al pattern &quot;[0-9]{18}&quot; per il tipo 'stNoticeNumber'.
		//				</description>
		//			</fault>
		//		</nfp:activatePaymentNoticeV2Response>

		CtFaultBean ctFaultBean = new CtFaultBean();
		ctFaultBean.setFaultCode(faultCode);
		ctFaultBean.setOriginalFaultCode(originalFaultCode);

		ActivatePaymentNoticeV2Response activatePaymentNoticeV2Response = new ActivatePaymentNoticeV2Response();
		activatePaymentNoticeV2Response.setOutcome(StOutcome.KO);
		activatePaymentNoticeV2Response.setFault(ctFaultBean);

		return activatePaymentNoticeV2Response;
	}


	@Test
	void testActivateByQrCode_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseOk));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(nodeActivateResponseOk.getTotalAmount().multiply(new BigDecimal(100)).longValue(),
				response.jsonPath().getLong("amount"));
		Assertions.assertEquals(nodeActivateResponseOk.getFiscalCodePA(), response.jsonPath().getString("paTaxCode"));
		Assertions.assertEquals(nodeActivateResponseOk.getPaymentToken(), response.jsonPath().getString("paymentToken"));
		Assertions.assertNotNull(response.jsonPath().getJsonObject("transfers"));
		Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(0).getFiscalCodePA(),
				response.jsonPath().getList("transfers", Transfer.class).get(0).getPaTaxCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(0).getCategory());
		Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(1).getFiscalCodePA(),
				response.jsonPath().getList("transfers", Transfer.class).get(1).getPaTaxCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(1).getCategory());

		//check of milRestService clients
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);
		
		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"),captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"),captorAcquirerId.getValue());
		
		//check of pnWrapper clients
		ArgumentCaptor<ActivatePaymentNoticeV2Request> captorActivatePaymentNoticeV2Request  = ArgumentCaptor.forClass(ActivatePaymentNoticeV2Request.class);
		Mockito.verify(pnWrapper).activatePaymentNoticeV2Async(captorActivatePaymentNoticeV2Request.capture());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getBroker(),captorActivatePaymentNoticeV2Request.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getChannel(),captorActivatePaymentNoticeV2Request.getValue().getIdChannel());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPassword(),captorActivatePaymentNoticeV2Request.getValue().getPassword());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPsp(),captorActivatePaymentNoticeV2Request.getValue().getIdPSP());
		Assertions.assertEquals(validActivateRequest.getIdempotencyKey(),captorActivatePaymentNoticeV2Request.getValue().getIdempotencyKey());
		
		Assertions.assertEquals("00000000000",captorActivatePaymentNoticeV2Request.getValue().getQrCode().getFiscalCode());
		Assertions.assertEquals("000000000000000000",captorActivatePaymentNoticeV2Request.getValue().getQrCode().getNoticeNumber());
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
	void testActivateByQrCode_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(milOutcome, response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideQrCodeValidationErrorCases")
	void testActivateByQrCode_400_invalidPathParams(String invalidEncodedQrCode, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", invalidEncodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testActivateByQrCode_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideActivateRequestValidationErrorCases")
	void testActivateByQrCode_400_invalidRequest(ActivatePaymentNoticeRequest activateRequest, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@Test
	void testActivateByQrCode_400_emptyRequest() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideNodeIntegrationErrorCases")
	void testActivateByQrCode_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testActivateByQrCode_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.body(validActivateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}
	
	@Test
	void testActivateByTaxCodeAndNoticeNumber_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseOk));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(nodeActivateResponseOk.getTotalAmount().multiply(new BigDecimal(100)).longValue(), response.jsonPath().getLong("amount"));
		Assertions.assertEquals(nodeActivateResponseOk.getFiscalCodePA(), response.jsonPath().getString("paTaxCode"));
		Assertions.assertEquals(nodeActivateResponseOk.getPaymentToken(), response.jsonPath().getString("paymentToken"));
		Assertions.assertNotNull(response.jsonPath().getJsonObject("transfers"));
		Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(0).getFiscalCodePA(),
				response.jsonPath().getList("transfers", Transfer.class).get(0).getPaTaxCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(0).getCategory());
		Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(1).getFiscalCodePA(),
				response.jsonPath().getList("transfers", Transfer.class).get(1).getPaTaxCode());
		Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(1).getCategory());

		//check of milRestService clients
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);
		
		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"),captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"),captorAcquirerId.getValue());
		
		//check of pnWrapper clients
		ArgumentCaptor<ActivatePaymentNoticeV2Request> captorActivatePaymentNoticeV2Request  = ArgumentCaptor.forClass(ActivatePaymentNoticeV2Request.class);
		Mockito.verify(pnWrapper).activatePaymentNoticeV2Async(captorActivatePaymentNoticeV2Request.capture());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getBroker(),captorActivatePaymentNoticeV2Request.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getChannel(),captorActivatePaymentNoticeV2Request.getValue().getIdChannel());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPassword(),captorActivatePaymentNoticeV2Request.getValue().getPassword());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPsp(),captorActivatePaymentNoticeV2Request.getValue().getIdPSP());
		Assertions.assertEquals(validActivateRequest.getIdempotencyKey(),captorActivatePaymentNoticeV2Request.getValue().getIdempotencyKey());
		
		Assertions.assertEquals("20000000000",captorActivatePaymentNoticeV2Request.getValue().getQrCode().getFiscalCode());
		Assertions.assertEquals("100000000000000000",captorActivatePaymentNoticeV2Request.getValue().getQrCode().getNoticeNumber());

	}

	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
	void testActivateByTaxCodeAndNoticeNumber_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(milOutcome, response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#providePaTaxCodeNoticeNumberValidationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_400_invalidPathParams(String paTaxCode, String noticeNumber, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", paTaxCode)
				.pathParam("noticeNumber", noticeNumber)
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideHeaderValidationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideActivateRequestValidationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_400_invalidRequest(ActivatePaymentNoticeRequest activateRequest, String errorCode) {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(activateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_400_emptyRequest() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideNodeIntegrationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(validActivateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}
}
