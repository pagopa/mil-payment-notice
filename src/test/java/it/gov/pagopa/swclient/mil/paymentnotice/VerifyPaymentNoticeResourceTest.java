package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.StAmountOptionPSP;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.QrCode;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.VerifyPaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.QrCodeParser;

@QuarkusTest
@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerifyPaymentNoticeResourceTest {

	@InjectMock
	NodeForPspWrapper nodeWrapper;

	@InjectMock
	@RestClient
	MilRestService milRestService;

	@Inject
	QrCodeParser qrCodeParser;

	VerifyPaymentNoticeRes verifyPaymentNoticeResOk;

	AcquirerConfiguration acquirerConfiguration;

	Map<String, String> validMilHeaders;

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

		// node verify response OK

		//		<nfp:verifyPaymentNoticeRes>
		//			<outcome>OK</outcome>
		//			<paymentList>
		//				<paymentOptionDescription>
		//					<amount>100.00</amount>
		//					<options>EQ</options>
		//					<dueDate>2021-07-31</dueDate>
		//					<paymentNote>pagamentoTest</paymentNote>
		//				</paymentOptionDescription>
		//			</paymentList>
		//			<paymentDescription>Pagamento di Test</paymentDescription>
		//			<fiscalCodePA>77777777777</fiscalCodePA>
		//			<companyName>companyName</companyName>
		//			<officeName>officeName</officeName>
		//		</nfp:verifyPaymentNoticeRes>

		XMLGregorianCalendar dueDate = null;
		try {
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			gregorianCalendar.setTime(formatter.parse("2021-07-31"));
			dueDate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
					gregorianCalendar.get(Calendar.YEAR),
					gregorianCalendar.get(Calendar.MONTH) + 1,
					gregorianCalendar.get(Calendar.DAY_OF_MONTH),
					DatatypeConstants.FIELD_UNDEFINED
			);
		}
		catch (ParseException | DatatypeConfigurationException ignored) {
		}

		CtPaymentOptionDescription paymentDescription = new CtPaymentOptionDescription();
		paymentDescription.setAmount(new BigDecimal("100.99"));
		paymentDescription.setOptions(StAmountOptionPSP.EQ);
		paymentDescription.setDueDate(dueDate);
		paymentDescription.setPaymentNote("paymentNote");

		CtPaymentOptionsDescriptionList paymentList = new CtPaymentOptionsDescriptionList();
		paymentList.getPaymentOptionDescription().add(paymentDescription);

		verifyPaymentNoticeResOk = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeResOk.setOutcome(StOutcome.OK);
		verifyPaymentNoticeResOk.setPaymentList(paymentList);
		verifyPaymentNoticeResOk.setPaymentDescription("Pagamento di Test");
		verifyPaymentNoticeResOk.setFiscalCodePA("77777777777");
		verifyPaymentNoticeResOk.setOfficeName("officeName");
		verifyPaymentNoticeResOk.setCompanyName("companyName");

	}

	private VerifyPaymentNoticeRes generateKoNodeResponse(String faultCode, String originalFaultCode) {

		// node verify response KO

		//		<nfp:verifyPaymentNoticeRes>
		//			<outcome>KO</outcome>
		//			<fault>
		//				<faultCode>PPT_SINTASSI_EXTRAXSD</faultCode>
		//				<faultString>Errore di sintassi extra XSD.</faultString>
		//				<id>NodoDeiPagamentiSPC</id>
		//				<description>Errore validazione XML [Envelope/Body/verifyPaymentNoticeReq/qrCode/noticeNumber] -
		//					cvc-pattern-valid: il valore &quot;30205&quot; non è valido come facet rispetto al pattern &quot;[0-9]{18}&quot; per il tipo 'stNoticeNumber'.
		//				</description>
		//			</fault>
		//		</nfp:verifyPaymentNoticeRes>

		CtFaultBean ctFaultBean = new CtFaultBean();
		ctFaultBean.setFaultCode(faultCode);
		ctFaultBean.setOriginalFaultCode(originalFaultCode);

		VerifyPaymentNoticeRes verifyPaymentNoticeRes = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeRes.setOutcome(StOutcome.KO);
		verifyPaymentNoticeRes.setFault(ctFaultBean);

		return verifyPaymentNoticeRes;
	}


	@Test
	void testVerifyByQrCode_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResOk));

		QrCode qrCode = qrCodeParser.b64UrlParse(encodedQrCode);

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
	    Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(
				verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getAmount().multiply(new BigDecimal(100)).longValue(),
				response.jsonPath().getLong("amount"));
		Assertions.assertEquals("2021-07-31", response.jsonPath().getString("dueDate"));
		Assertions.assertEquals(
				verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getPaymentNote(),
				response.jsonPath().getString("note"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentDescription(), response.jsonPath().getString("description"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getCompanyName(), response.jsonPath().getString("company"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getOfficeName(), response.jsonPath().getString("office"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getFiscalCodePA(), response.jsonPath().getString("paTaxCode"));
		Assertions.assertEquals(qrCode.getNoticeNumber(), response.jsonPath().getString("noticeNumber"));

		//check of milRestService clients
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);
		
		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"),captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"),captorAcquirerId.getValue());
		
		//check of nodeWrapper clients
		ArgumentCaptor<VerifyPaymentNoticeReq> captorVerifyPaymentNoticeReq  = ArgumentCaptor.forClass(VerifyPaymentNoticeReq.class);
		Mockito.verify(nodeWrapper).verifyPaymentNotice(captorVerifyPaymentNoticeReq.capture());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getBroker(),captorVerifyPaymentNoticeReq.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getChannel(),captorVerifyPaymentNoticeReq.getValue().getIdChannel());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPassword(),captorVerifyPaymentNoticeReq.getValue().getPassword());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPsp(),captorVerifyPaymentNoticeReq.getValue().getIdPSP());
		
		Assertions.assertEquals("00000000000",captorVerifyPaymentNoticeReq.getValue().getQrCode().getFiscalCode());
		Assertions.assertEquals("000000000000000000",captorVerifyPaymentNoticeReq.getValue().getQrCode().getNoticeNumber());
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
	void testVerifyByQrCode_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(milOutcome, response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideQrCodeValidationErrorCases")
	void testVerifyByQrCode_400_invalidPathParams(String invalidEncodedQrCode, String errorCode) {

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", invalidEncodedQrCode)
				.when()
				.get("/{qrCode}")
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
	void testVerifyByQrCode_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.headers(invalidHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.get("/{qrCode}")
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
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideNodeIntegrationErrorCases")
	void testVerifyByQrCode_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testVerifyByQrCode_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));
		
		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("qrCode", encodedQrCode)
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
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
	void testVerifyByTaxCodeAndNoticeNumber_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResOk));
		
		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(Outcome.OK.name(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getAmount().multiply(new BigDecimal(100)).longValue(), response.jsonPath().getLong("amount"));
		Assertions.assertEquals("2021-07-31", response.jsonPath().getString("dueDate"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getPaymentNote(), response.jsonPath().getString("note"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentDescription(), response.jsonPath().getString("description"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getCompanyName(), response.jsonPath().getString("company"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getOfficeName(), response.jsonPath().getString("office"));

		//check of milRestService clients
		ArgumentCaptor<String> captorRequestId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captorAcquirerId = ArgumentCaptor.forClass(String.class);
		
		Mockito.verify(milRestService).getPspConfiguration(captorRequestId.capture(),captorAcquirerId.capture());
		Assertions.assertEquals(validMilHeaders.get("RequestId"),captorRequestId.getValue());
		Assertions.assertEquals(validMilHeaders.get("AcquirerId"),captorAcquirerId.getValue());
		
		//check of nodeWrapper clients
		ArgumentCaptor<VerifyPaymentNoticeReq> captorVerifyPaymentNoticeReq  = ArgumentCaptor.forClass(VerifyPaymentNoticeReq.class);
		Mockito.verify(nodeWrapper).verifyPaymentNotice(captorVerifyPaymentNoticeReq.capture());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getBroker(),captorVerifyPaymentNoticeReq.getValue().getIdBrokerPSP());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getChannel(),captorVerifyPaymentNoticeReq.getValue().getIdChannel());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPassword(),captorVerifyPaymentNoticeReq.getValue().getPassword());
		Assertions.assertEquals(acquirerConfiguration.getPspConfigForVerifyAndActivate().getPsp(),captorVerifyPaymentNoticeReq.getValue().getIdPSP());
		
		Assertions.assertEquals("20000000000",captorVerifyPaymentNoticeReq.getValue().getQrCode().getFiscalCode());
		Assertions.assertEquals("100000000000000000",captorVerifyPaymentNoticeReq.getValue().getQrCode().getNoticeNumber());

	}

	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
	void testVerifyByTaxCodeAndNoticeNumber_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals(milOutcome, response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@ParameterizedTest
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#providePaTaxCodeNoticeNumberValidationErrorCases")
	void testVerifyByTaxCodeAndNoticeNumber_400_invalidPathParams(String paTaxCode, String noticeNumber, String errorCode) {

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", paTaxCode)
				.pathParam("noticeNumber", noticeNumber)
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
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
	void testVerifyByTaxCodeAndNoticeNumber_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode) {

		Response response = given()
				.headers(invalidHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
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
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideNodeIntegrationErrorCases")
	void testVerifyByTaxCodeAndNoticeNumber_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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
	@MethodSource("it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils#provideMilIntegrationErrorCases")
	void testVerifyByTaxCodeAndNoticeNumber_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));
		
		Response response = given()
				.headers(validMilHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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

}
