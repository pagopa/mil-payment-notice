package it.gov.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferListPSPV2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferPSPV2;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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

	ActivatePaymentNoticeV2Response nodeActivateResponseKo;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> commonHeaders;


	@BeforeAll
	void createTestObjects() {

		// common headers
		commonHeaders = new HashMap<>();
		commonHeaders.put("RequestId", UUID.randomUUID().toString());
		commonHeaders.put("Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay");
		commonHeaders.put("AcquirerId", "4585625");
		commonHeaders.put("Channel", "ATM");
		commonHeaders.put("TerminalId", "0aB9wXyZ");
		commonHeaders.put("SessionId", UUID.randomUUID().toString());


		// acquirer PSP configuration
		acquirerConfiguration = new AcquirerConfiguration();

		PspConfiguration pspConfiguration = new PspConfiguration();
		pspConfiguration.setPsp("AGID_01");
		pspConfiguration.setBroker("97735020584");
		pspConfiguration.setChannel("97735020584_07");
		pspConfiguration.setPassword("PLACEHOLDER");

		acquirerConfiguration.setPspConfigForVerifyAndActivate(pspConfiguration);
		acquirerConfiguration.setPspConfigForGetFeeAndClosePayment(pspConfiguration);

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


		// node activate response KO

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
		ctFaultBean.setFaultCode("PPT_SINTASSI_EXTRAXSD");
		ctFaultBean.setFaultString("Errore di sintassi extra XSD.");
		ctFaultBean.setId("NodoDeiPagamentiSPC");
		ctFaultBean.setDescription(
				"Errore validazione XML [Envelope/Body/verifyPaymentNoticeReq/qrCode/noticeNumber] - cvc-pattern-valid: il valore \"30205\" " +
				"non è valido come facet rispetto al pattern \"[0-9]{18}\" per il tipo 'stNoticeNumber'."
		);

		nodeActivateResponseKo = new ActivatePaymentNoticeV2Response();
		nodeActivateResponseKo.setOutcome(StOutcome.KO);
		nodeActivateResponseKo.setFault(ctFaultBean);

	}

	private static Stream<Arguments> provideNodeIntegrationErrorCases() {
		return Stream.of(
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)
		);
	}

	private static Stream<Arguments> provideMilIntegrationErrorCases() {
		return Stream.of(
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_404, ErrorCode.UNKNOWN_ACQUIRER_ID),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)
		);
	}


	@Test
	void testActivateByQrCode_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseOk));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
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
	     
	}
	
	@Test
	void testActivateByQrCode_200_nodeKo() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseKo));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");


		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals("WRONG_NOTICE_DATA", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}


	@ParameterizedTest
	@MethodSource("provideNodeIntegrationErrorCases")
	void testActivateByQrCode_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

	}

	@ParameterizedTest
	@MethodSource("provideMilIntegrationErrorCases")
	void testActivateByQrCode__500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(activateRequest)
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
	     
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_200_nodeKo() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseKo));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(activateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals("WRONG_NOTICE_DATA", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

	}

	@ParameterizedTest
	@MethodSource("provideNodeIntegrationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(activateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	}

	@ParameterizedTest
	@MethodSource("provideMilIntegrationErrorCases")
	void testActivateByTaxCodeAndNoticeNumber_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.body(activateRequest)
				.when()
				.patch("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}
}
