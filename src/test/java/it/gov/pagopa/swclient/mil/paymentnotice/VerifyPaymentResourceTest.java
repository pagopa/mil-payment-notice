package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Assertions;
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
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.NodeForPspWrapper;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;

@QuarkusTest
@TestHTTPEndpoint(PaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerifyPaymentResourceTest {
	
	private final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	private final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	private final static String QR_CODE				= "qrCode";
	private final static String PAX_CODE			= "paxCode";
	private final static String NOTICE_NUMBER		= "noticeNumber";
	private final static String PAX_CODE_VALUE		= "20000000000";
	private final static String NOTICE_NUMBER_VALUE	= "100000000000000000";
	
	@InjectMock
	private NodeForPspWrapper pnWrapper;
	
	@InjectMock
	private PNRepository pnRepository;
	
	@Test
	void testVerifyNoticeByQrCode_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(PAX_CODE_VALUE);
		ctQrCode.setNoticeNumber(NOTICE_NUMBER_VALUE);
		
		VerifyPaymentNoticeRes verifyPaymentNoticeRes = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeRes.setOutcome(StOutcome.OK);
		verifyPaymentNoticeRes.setPaymentDescription("Payment description");
		verifyPaymentNoticeRes.setOfficeName("Office");
		verifyPaymentNoticeRes.setCompanyName("company");
		
		CtPaymentOptionsDescriptionList paymentList = new CtPaymentOptionsDescriptionList();
		CtPaymentOptionDescription paymentDescription = new CtPaymentOptionDescription();
		paymentDescription.setAmount(new BigDecimal(1000));
		
		GregorianCalendar c = new GregorianCalendar();
		String date = "2023-11-12";
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");  
		c.setTime(formatter.parse(date));
		XMLGregorianCalendar gregDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		
		paymentDescription.setDueDate(gregDate);
		paymentDescription.setPaymentNote("note");
		
		paymentList.getPaymentOptionDescription().add(paymentDescription);
		
		verifyPaymentNoticeRes.setPaymentList(paymentList);
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
		.thenReturn(Uni.createFrom().item(verifyPaymentNoticeRes));
		
		Response response = given()
//				.contentType(ContentType.JSON)
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
				.get("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
	}
	
	@Test
	void testVerifyNoticeByQrCodeOutcomeKo_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(PAX_CODE_VALUE);
		ctQrCode.setNoticeNumber(NOTICE_NUMBER_VALUE);
		
		VerifyPaymentNoticeRes verifyPaymentNoticeRes = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeRes.setOutcome(StOutcome.KO);
		verifyPaymentNoticeRes.setPaymentDescription("Payment description");
		verifyPaymentNoticeRes.setOfficeName("Office");
		verifyPaymentNoticeRes.setCompanyName("company");
		
		CtPaymentOptionsDescriptionList paymentList = new CtPaymentOptionsDescriptionList();
		CtPaymentOptionDescription paymentDescription = new CtPaymentOptionDescription();
		paymentDescription.setAmount(new BigDecimal(1000));
		
		GregorianCalendar c = new GregorianCalendar();
		String date = "2023-11-12";
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");  
		c.setTime(formatter.parse(date));
		XMLGregorianCalendar gregDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		
		paymentDescription.setDueDate(gregDate);
		paymentDescription.setPaymentNote("note");
		
		paymentList.getPaymentOptionDescription().add(paymentDescription);
		
		verifyPaymentNoticeRes.setPaymentList(paymentList);
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
		.thenReturn(Uni.createFrom().item(verifyPaymentNoticeRes));
		
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
				.get("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.KO.toString(), response.jsonPath().getString("outcome"));
	}
	
	@Test
	void testVerifyNoticeByQrCode_pspInfoNotFound() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
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
				.get("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals("{\"errors\":[\"" + ErrorCode.ERROR_READING_PSP_INFO + "\"]}", response.getBody().asString());
	}

	@Test
	void testVerifyNoticePaTaxCodeAndNoticeNumber_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(PAX_CODE_VALUE);
		ctQrCode.setNoticeNumber(NOTICE_NUMBER_VALUE);
		
		VerifyPaymentNoticeRes verifyPaymentNoticeRes = new VerifyPaymentNoticeRes();
		verifyPaymentNoticeRes.setOutcome(StOutcome.OK);
		verifyPaymentNoticeRes.setPaymentDescription("Payment description");
		verifyPaymentNoticeRes.setOfficeName("Office");
		verifyPaymentNoticeRes.setCompanyName("company");
		
		CtPaymentOptionsDescriptionList paymentList = new CtPaymentOptionsDescriptionList();
		CtPaymentOptionDescription paymentDescription = new CtPaymentOptionDescription();
		paymentDescription.setAmount(new BigDecimal(1000));
		
		GregorianCalendar c = new GregorianCalendar();
		String date = "2023-11-12";
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");  
		c.setTime(formatter.parse(date));
		XMLGregorianCalendar gregDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		
		paymentDescription.setDueDate(gregDate);
		paymentDescription.setPaymentNote("note");
		
		paymentList.getPaymentOptionDescription().add(paymentDescription);
		
		verifyPaymentNoticeRes.setPaymentList(paymentList);
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.verifyPaymentNotice(Mockito.any()))
		.thenReturn(Uni.createFrom().item(verifyPaymentNoticeRes));
		
		Response response = given()
//				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(PAX_CODE, "20000000000")
				.pathParam(NOTICE_NUMBER, "100000000000000000")
				.when()
				.get("paymentNotices/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
	}
	
	@Test
	void testVerifyNoticePaTaxCodeAndNoticeNumber_pspInfoNotFound() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
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
				.pathParam(PAX_CODE, "20000000000")
				.pathParam(NOTICE_NUMBER, "100000000000000000")
				.when()
				.get("paymentNotices/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals("{\"errors\":[\"" + ErrorCode.ERROR_READING_PSP_INFO + "\"]}", response.getBody().asString());
	}
	
}
