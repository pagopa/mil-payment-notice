package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferListPSPV2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferPSPV2;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.NodeForPspWrapper;

@QuarkusTest
@TestHTTPEndpoint(ActivatePaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivatePaymentResourceTest {
	
	private final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	private final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	private final static String QR_CODE				= "qrCode";
	private final static String PAX_CODE			= "paxCode";
	private final static String NOTICE_NUMBER		= "noticeNumber";

	
	@InjectMock
	private NodeForPspWrapper pnWrapper;
	
	@InjectMock
	private PNRepository pnRepository;

	@Test
	void testActivateNoticeByQrCode_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		ActivatePaymentNoticeV2Response activatePaymentNoticeV2Response = new ActivatePaymentNoticeV2Response();
		activatePaymentNoticeV2Response.setOutcome(StOutcome.OK);
		activatePaymentNoticeV2Response.setTotalAmount(new BigDecimal(129000));
		activatePaymentNoticeV2Response.setPaymentDescription("Descr");
		activatePaymentNoticeV2Response.setFiscalCodePA("98097686");
		activatePaymentNoticeV2Response.setCompanyName("company name");
		activatePaymentNoticeV2Response.setOfficeName("office name");
		activatePaymentNoticeV2Response.setPaymentToken("23509uvskvn");
		CtTransferListPSPV2 transferListV2 = new CtTransferListPSPV2();
		CtTransferPSPV2 transfer = new CtTransferPSPV2();
		transfer.setFiscalCodePA("09857rt8yho");
		
		transferListV2.getTransfer().add(transfer);
		activatePaymentNoticeV2Response.setTransferList(transferListV2);
		
		ActivatePaymentBody acivatePaymentBody = new ActivatePaymentBody();
		acivatePaymentBody.setAmount(12099);
		acivatePaymentBody.setIdempotencyKey("47583764545_Ahf479elk0");
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
		.thenReturn(Uni.createFrom().item(activatePaymentNoticeV2Response));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(acivatePaymentBody)
				.when()
				.patch("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
	     
	}
	
	@Test
	void testActivateNoticeByQrCodeKo_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		ActivatePaymentNoticeV2Response activatePaymentNoticeV2Response = new ActivatePaymentNoticeV2Response();
		activatePaymentNoticeV2Response.setOutcome(StOutcome.KO);
		activatePaymentNoticeV2Response.setTotalAmount(new BigDecimal(129000));
		activatePaymentNoticeV2Response.setPaymentDescription("Descr");
		activatePaymentNoticeV2Response.setFiscalCodePA("98097686");
		activatePaymentNoticeV2Response.setCompanyName("company name");
		activatePaymentNoticeV2Response.setOfficeName("office name");
		activatePaymentNoticeV2Response.setPaymentToken("23509uvskvn");
		CtTransferListPSPV2 transferListV2 = new CtTransferListPSPV2();
		CtTransferPSPV2 transfer = new CtTransferPSPV2();
		transfer.setFiscalCodePA("09857rt8yho");
		
		transferListV2.getTransfer().add(transfer);
		activatePaymentNoticeV2Response.setTransferList(transferListV2);
		
		ActivatePaymentBody acivatePaymentBody = new ActivatePaymentBody();
		acivatePaymentBody.setAmount(12099);
		acivatePaymentBody.setIdempotencyKey("47583764545_Ahf479elk0");
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
		.thenReturn(Uni.createFrom().item(activatePaymentNoticeV2Response));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(acivatePaymentBody)
				.when()
				.patch("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.KO.toString(), response.jsonPath().getString("outcome"));
	     
	}

	@Test
	void testActivateNoticeByQrCode_pspInfoNotFound() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		ActivatePaymentBody acivatePaymentBody = new ActivatePaymentBody();
		acivatePaymentBody.setAmount(12099);
		acivatePaymentBody.setIdempotencyKey("47583764545_Ahf479elk0");
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.pathParam(QR_CODE, "PAGOPA|002|100000000000000000|20000000000|9999")
				.body(acivatePaymentBody)
				.when()
				.patch("paymentNotices/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertEquals("{\"errors\":[\"" + ErrorCode.ERROR_READING_PSP_INFO + "\"]}", response.getBody().asString());
	     
	}
	
	@Test
	void testActivateNoticePaTaxCodeAndNoticeNumber_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		ActivatePaymentNoticeV2Response activatePaymentNoticeV2Response = new ActivatePaymentNoticeV2Response();
		activatePaymentNoticeV2Response.setOutcome(StOutcome.OK);
		activatePaymentNoticeV2Response.setTotalAmount(new BigDecimal(129000));
		activatePaymentNoticeV2Response.setPaymentDescription("Descr");
		activatePaymentNoticeV2Response.setFiscalCodePA("98097686");
		activatePaymentNoticeV2Response.setCompanyName("company name");
		activatePaymentNoticeV2Response.setOfficeName("office name");
		activatePaymentNoticeV2Response.setPaymentToken("23509uvskvn");
		CtTransferListPSPV2 transferListV2 = new CtTransferListPSPV2();
		CtTransferPSPV2 transfer = new CtTransferPSPV2();
		transfer.setFiscalCodePA("09857rt8yho");
		
		transferListV2.getTransfer().add(transfer);
		activatePaymentNoticeV2Response.setTransferList(transferListV2);
		
		ActivatePaymentBody acivatePaymentBody = new ActivatePaymentBody();
		acivatePaymentBody.setAmount(12099);
		acivatePaymentBody.setIdempotencyKey("47583764545_Ahf479elk0");
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		Mockito.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
		.thenReturn(Uni.createFrom().item(activatePaymentNoticeV2Response));
		
		Response response = given()
				.contentType(ContentType.JSON)
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
				.body(acivatePaymentBody)
				.when()
				.patch("paymentNotices/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.OK.toString(), response.jsonPath().getString("outcome"));
	     
	}
	
	@Test
	void testActivateNoticePaTaxCodeAndNoticeNumber_pspInfoNotFound() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		ActivatePaymentBody acivatePaymentBody = new ActivatePaymentBody();
		acivatePaymentBody.setAmount(12099);
		acivatePaymentBody.setIdempotencyKey("47583764545_Ahf479elk0");
		
		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		Response response = given()
				.contentType(ContentType.JSON)
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
				.body(acivatePaymentBody)
				.when()
				.patch("paymentNotices/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"" + ErrorCode.ERROR_READING_PSP_INFO + "\"]}", response.getBody().asString());
	     
	}
}
