package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.StAmountOptionPSP;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.VerifyPaymentNoticeResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;

@QuarkusTest
@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerifyPaymentNoticeResourceTest {
	
	final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	final static String QR_CODE				= "qrCode";
	final static String PA_TAX_CODE 		= "paTaxCode";
	final static String NOTICE_NUMBER		= "noticeNumber";

	@InjectMock
	NodeForPspWrapper pnWrapper;
	
	@InjectMock
	PspConfRepository pspConfRepository;

	VerifyPaymentNoticeRes verifyPaymentNoticeResOk;

	VerifyPaymentNoticeRes verifyPaymentNoticeResKo;

	PspConfEntity pspConfEntity;

	@BeforeAll
	void createTestObjects() {

		// PSP configuration
		PspConfiguration pspInfo = new PspConfiguration();
		pspInfo.setPspId("AGID_01");
		pspInfo.setPspBroker("97735020584");
		pspInfo.setPspPassword("pwd_AgID");

		pspConfEntity = new PspConfEntity();
		pspConfEntity.acquirerId = "4585625";
		pspConfEntity.pspConfiguration = pspInfo;

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
		ctFaultBean.setFaultCode("PPT_SINTASSI_EXTRAXSD");
		ctFaultBean.setFaultString("Errore di sintassi extra XSD.");
		ctFaultBean.setId("NodoDeiPagamentiSPC");
		ctFaultBean.setDescription("Errore validazione XML [Envelope/Body/verifyPaymentNoticeReq/qrCode/noticeNumber] - cvc-pattern-valid: il valore \"30205\" " +
				"non è valido come facet rispetto al pattern \"[0-9]{18}\" per il tipo 'stNoticeNumber'.");

		verifyPaymentNoticeResKo = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeResKo.setOutcome(StOutcome.KO);
		verifyPaymentNoticeResKo.setFault(ctFaultBean);

	}


	@Test
	void testVerifyByQrCode_400() {

		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|100000000000000000|20000000000|9999")
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
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(PA_TAX_CODE, "2000000")
				.pathParam(NOTICE_NUMBER, "10000000000")
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
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
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResOk));
		
		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|302051234567890125|77777777777|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
	    Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getAmount().multiply(new BigDecimal(100)).longValue(), response.jsonPath().getLong("amount"));
		Assertions.assertEquals("2021-07-31", response.jsonPath().getString("dueDate"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentList().getPaymentOptionDescription().get(0).getPaymentNote(), response.jsonPath().getString("note"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getPaymentDescription(), response.jsonPath().getString("description"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getCompanyName(), response.jsonPath().getString("company"));
		Assertions.assertEquals(verifyPaymentNoticeResOk.getOfficeName(), response.jsonPath().getString("office"));
	}
	
	@Test
	void testVerifyByQrCode_200_nodeKo() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));
		
		Mockito
				.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResKo));
		
		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|100000000000000000|20000000000|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals("WRONG_NOTICE_DATA", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}
	
	@Test
	void testVerifyByQrCode_500_pspInfoNotFound() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|100000000000000000|20000000000|9999")
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
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
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));
		
		Mockito
				.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResOk));
		
		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(PA_TAX_CODE, "20000000000")
				.pathParam(NOTICE_NUMBER, "100000000000000000")
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
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


	@Test
	void testVerifyByTaxCodeAndNoticeNumber_200_nodeKo() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
				.thenReturn(Uni.createFrom().item(verifyPaymentNoticeResKo));

		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(PA_TAX_CODE, "20000000000")
				.pathParam(NOTICE_NUMBER, "100000000000000000")
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));
		Assertions.assertEquals("WRONG_NOTICE_DATA", response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}
	
	@Test
	void testVerifyNoticePaTaxCodeAndNoticeNumber_pspInfoNotFound() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		Response response = given()
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(PA_TAX_CODE, "20000000000")
				.pathParam(NOTICE_NUMBER, "100000000000000000")
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}
	
}
