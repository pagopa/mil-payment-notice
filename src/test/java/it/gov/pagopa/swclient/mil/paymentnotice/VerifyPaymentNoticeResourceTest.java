package it.gov.pagopa.swclient.mil.paymentnotice;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.StAmountOptionPSP;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.client.MilRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.VerifyPaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.util.ExceptionType;
import it.gov.pagopa.swclient.mil.paymentnotice.util.TestUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerifyPaymentNoticeResourceTest {

	@InjectMock
	NodeForPspWrapper nodeWrapper;

	@InjectMock
	@RestClient
	MilRestService milRestService;

	VerifyPaymentNoticeRes verifyPaymentNoticeResOk;

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

	private Stream<Arguments> provideNodeIntegrationErrorCases() {
		return Stream.of(
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
				Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)
		);
	}

	private Stream<Arguments> provideMilIntegrationErrorCases() {
		return Stream.of(
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_404, ErrorCode.UNKNOWN_ACQUIRER_ID),
				Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
				Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)
		);
	}

	@Test
	void testVerifyByQrCode_400() {

		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|100000000000000000|20000000000|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.QRCODE_MUST_MATCH_REGEXP));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}

	@Test
	void testVerifyByTaxCodeAndNoticeNumber_400() {

		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "2000000")
				.pathParam("noticeNumber", "10000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP));
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}


	@Test
	void testVerifyByQrCode_200_nodeOk() {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResOk));
		
		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|302051234567890125|77777777777|9999")
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
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mopping.csv", numLinesToSkip = 1)
	void testVerifyByQrCode_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));
		
		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));
		
		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
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
	@MethodSource("provideNodeIntegrationErrorCases")
	void testVerifyByQrCode_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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
	@MethodSource("provideMilIntegrationErrorCases")
	void testVerifyByQrCode_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));
		
		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("qrCode", "PAGOPA|002|100000000000000000|20000000000|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
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
				.headers(commonHeaders)
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

	}


	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mopping.csv", numLinesToSkip = 1)
	void testVerifyByTaxCodeAndNoticeNumber_200_nodeKo(String faultCode, String originalFaultCode, String milOutcome) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(generateKoNodeResponse(faultCode, originalFaultCode)));

		Response response = given()
				.headers(commonHeaders)
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
	@MethodSource("provideNodeIntegrationErrorCases")
	void testVerifyByTaxCodeAndNoticeNumber_500_nodeError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(acquirerConfiguration));

		Mockito
				.when(nodeWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));

		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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
	@MethodSource("provideMilIntegrationErrorCases")
	void testVerifyNoticePaTaxCodeAndNoticeNumber_500_milError(ExceptionType exceptionType, String errorCode) {

		Mockito
				.when(milRestService.getPspConfiguration(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().failure(TestUtils.getException(exceptionType)));
		
		Response response = given()
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", "20000000000")
				.pathParam("noticeNumber", "100000000000000000")
				.when()
				.get("/{paTaxCode}/{noticeNumber}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
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
