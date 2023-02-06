package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;

import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PspConfRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeForPspWrapper;

@QuarkusTest
@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivatePaymentNoticeResourceTest {
	
	final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	final static String QR_CODE				= "qrCode";
	final static String PAX_CODE			= "paxCode";
	final static String NOTICE_NUMBER		= "noticeNumber";

	
	@InjectMock
	NodeForPspWrapper pnWrapper;
	
	@InjectMock
	PspConfRepository pspConfRepository;

	PspConfEntity pspConfEntity;

	ActivatePaymentNoticeV2Response nodeActivateResponseOk;

	ActivatePaymentNoticeV2Response nodeActivateResponseKo;

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
		transfer1.setTransferAmount(new BigDecimal(99.98));
		transfer1.setFiscalCodePA("77777777777");
		transfer1.setIBAN("IT30N0103076271000001823603");
		transfer1.setRemittanceInformation("TARI Comune EC_TE");
		// TODO: missing category in wsdl

		CtTransferPSPV2 transfer2 = new CtTransferPSPV2();
		transfer2.setIdTransfer(1);
		transfer2.setTransferAmount(new BigDecimal(1.01));
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
		ctFaultBean.setDescription(
				"Errore validazione XML [Envelope/Body/verifyPaymentNoticeReq/qrCode/noticeNumber] - cvc-pattern-valid: il valore \"30205\" " +
				"non è valido come facet rispetto al pattern \"[0-9]{18}\" per il tipo 'stNoticeNumber'."
		);

		nodeActivateResponseKo = new ActivatePaymentNoticeV2Response();
		nodeActivateResponseKo.setOutcome(StOutcome.KO);
		nodeActivateResponseKo.setFault(ctFaultBean);

	}


	@Test
	void testActivateByQrCode_200_nodeOk() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseOk));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

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
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseKo));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");


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

	@Test
	void testActivateByQrCode_500_pspInfoNotFound() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.empty()));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
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
				.body(activateRequest)
				.when()
				.patch("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}
	
	@Test
	void testActivateByTaxCodeAndNoticeNumber_200_nodeOk() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));
		
		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseOk));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
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
				.body(activateRequest)
				.when()
				.patch("/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
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
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.of(pspConfEntity)));

		Mockito
				.when(pnWrapper.activatePaymentNoticeV2Async(Mockito.any()))
				.thenReturn(Uni.createFrom().item(nodeActivateResponseKo));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

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
				.body(activateRequest)
				.when()
				.patch("/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
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
	
	@Test
	void testActivateByTaxCodeAndNoticeNumber_500_pspInfoNotFound() {

		Mockito
				.when(pspConfRepository.findByIdOptional(Mockito.any(String.class)))
				.thenReturn(Uni.createFrom().item(Optional.empty()));

		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

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
				.body(activateRequest)
				.when()
				.patch("/{"+ PAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	     
	}
}
