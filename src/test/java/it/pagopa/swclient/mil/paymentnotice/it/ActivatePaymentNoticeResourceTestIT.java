package it.pagopa.swclient.mil.paymentnotice.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.net.impl.TrustAllTrustManager;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferListPSPV2;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtTransferPSPV2;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.pagopa.swclient.mil.paymentnotice.util.Role;
import it.pagopa.swclient.mil.paymentnotice.bean.Transfer;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.it.resource.InjectTokenGenerator;
import it.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentNoticeResource;
import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.pagopa.swclient.mil.paymentnotice.util.TokenGenerator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivatePaymentNoticeResourceTestIT implements DevServicesContext.ContextAware {

    static final Logger logger = LoggerFactory.getLogger(VerifyPaymentNoticeResourceTestIT.class);

    @InjectTokenGenerator
    TokenGenerator tokenGenerator;

    ActivatePaymentNoticeV2Response nodeActivateResponseOk;

    ActivatePaymentNoticeV2Response nodeActivateResponseKo;

    DevServicesContext devServicesContext;

    JedisPool jedisPool;

    @Override
    public void setIntegrationTestContext(DevServicesContext devServicesContext) {
        this.devServicesContext = devServicesContext;
    }

    @BeforeAll
    void createTestObjects() {

        logger.info("devServicesContext " + devServicesContext);

        // initializing redis client
        String redisExposedPort = devServicesContext.devServicesProperties().get("test.redis.exposed-port");
        String password = devServicesContext.devServicesProperties().get("test.redis.password");
        boolean tlsEnabled = BooleanUtils.toBoolean(devServicesContext.devServicesProperties().get("test.redis.tls"));
        String redisURI = "redis" + (tlsEnabled ? "s" : "") + "://:" + password + "@127.0.0.1:" + redisExposedPort;
        if (tlsEnabled) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{TrustAllTrustManager.INSTANCE}, null);
                jedisPool = new JedisPool(redisURI, sslContext.getSocketFactory(), null, (hostname, session) -> true);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                logger.error("Error while initializing sslContext for jedis", e);
            }
        } else {
            jedisPool = new JedisPool(redisURI);
        }


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
        // category in not present in wsdl

        CtTransferPSPV2 transfer2 = new CtTransferPSPV2();
        transfer2.setIdTransfer(1);
        transfer2.setTransferAmount(new BigDecimal("1.01"));
        transfer2.setFiscalCodePA("77777777777");
        transfer2.setIBAN("IT30N0103076271000001823603");
        transfer2.setRemittanceInformation("TARI Comune EC_TE");
        // category in not present in wsdl

        CtTransferListPSPV2 transferList = new CtTransferListPSPV2();
        transferList.getTransfer().add(0, transfer1);
        transferList.getTransfer().add(1, transfer2);

        nodeActivateResponseOk = new ActivatePaymentNoticeV2Response();
        nodeActivateResponseOk.setOutcome(StOutcome.OK);
        nodeActivateResponseOk.setTotalAmount(new BigDecimal("100.99"));
        nodeActivateResponseOk.setPaymentDescription("TARI 2021");
        nodeActivateResponseOk.setFiscalCodePA("77777777777");
        nodeActivateResponseOk.setCompanyName("company PA");
        nodeActivateResponseOk.setOfficeName("office PA");
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

    @AfterAll
    void destroyTestObjects() {
        try {
            jedisPool.destroy();
        } catch (Exception e) {
            logger.error("Error while destroying Jedis pool", e);
        }
    }

    String generateB64UrlEncodedQrCode(String paTaxCode) {
        String qrCode = "PAGOPA|002|302051234567890125|" + paTaxCode + "|9999";
        byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode(qrCode.getBytes(StandardCharsets.UTF_8));
        logger.debug(new String(bytes, StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    void testActivateByQrCode_200_nodeOk() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("qrCode", generateB64UrlEncodedQrCode("77777777777"))
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
        Assertions.assertNotNull(response.jsonPath().getString("paymentToken")); // payment token is randomly generated from wiremock
        Assertions.assertNotNull(response.jsonPath().getJsonObject("transfers"));
        Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(0).getFiscalCodePA(),
                response.jsonPath().getList("transfers", Transfer.class).get(0).getPaTaxCode());
        Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(0).getCategory());
        Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(1).getFiscalCodePA(),
                response.jsonPath().getList("transfers", Transfer.class).get(1).getPaTaxCode());
        Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(1).getCategory());

        try (Jedis jedis = jedisPool.getResource()) {
            String sCachedNotice = jedis.get(response.jsonPath().getString("paymentToken"));
            Assertions.assertNotNull(sCachedNotice);
            Notice cachedNotice = new ObjectMapper().readValue(sCachedNotice, Notice.class);
            Assertions.assertEquals(response.jsonPath().getString("paymentToken"), cachedNotice.getPaymentToken());
            Assertions.assertEquals(nodeActivateResponseOk.getFiscalCodePA(), cachedNotice.getPaTaxCode());
            Assertions.assertEquals("302051234567890125", cachedNotice.getNoticeNumber());
            Assertions.assertEquals(nodeActivateResponseOk.getTotalAmount().scaleByPowerOfTen(2).longValue(), cachedNotice.getAmount());
            Assertions.assertEquals(nodeActivateResponseOk.getPaymentDescription(), cachedNotice.getDescription());
            Assertions.assertEquals(nodeActivateResponseOk.getCompanyName(), cachedNotice.getCompany());
            Assertions.assertEquals(nodeActivateResponseOk.getOfficeName(), cachedNotice.getOffice());
            Assertions.assertNull(cachedNotice.getCreditorReferenceId());
            Assertions.assertNull(cachedNotice.getDebtor());
            logger.debug("Cached notice: {}", cachedNotice);

        } catch (JsonProcessingException e) {
            logger.error("Error while reading notice from redis", e);
        }
    }

    @Test
    void testActivateByQrCode_200_nodeKo() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");


        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("qrCode", generateB64UrlEncodedQrCode("20000000000"))
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
    @ValueSource(strings = {"88888888888", "99999999999", "66666666666"})
    void testActivateByQrCode_500_nodeError(String paTaxCode) {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("qrCode", generateB64UrlEncodedQrCode(paTaxCode))
                .body(activateRequest)
                .when()
                .patch("/{qrCode}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
        Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
        Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
        Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

    }

    @Test
    void testActivateByQrCode_500_pspInfoNotFound() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, false))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("qrCode", generateB64UrlEncodedQrCode("77777777777"))
                .body(activateRequest)
                .when()
                .patch("/{qrCode}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
        Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
        Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
        Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

    }

    @Test
    void testActivateByTaxCodeAndNoticeNumber_200_nodeOk() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("paTaxCode", "77777777777")
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
        Assertions.assertNotNull(response.jsonPath().getString("paymentToken"));
        Assertions.assertNotNull(response.jsonPath().getJsonObject("transfers"));
        Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(0).getFiscalCodePA(),
                response.jsonPath().getList("transfers", Transfer.class).get(0).getPaTaxCode());
        Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(0).getCategory());
        Assertions.assertEquals(nodeActivateResponseOk.getTransferList().getTransfer().get(1).getFiscalCodePA(),
                response.jsonPath().getList("transfers", Transfer.class).get(1).getPaTaxCode());
        Assertions.assertEquals(StringUtils.EMPTY, response.jsonPath().getList("transfers", Transfer.class).get(1).getCategory());

        try (Jedis jedis = jedisPool.getResource()) {
            String sCachedNotice = jedis.get(response.jsonPath().getString("paymentToken"));
            Assertions.assertNotNull(sCachedNotice);
            Notice cachedNotice = new ObjectMapper().readValue(sCachedNotice, Notice.class);
            Assertions.assertEquals(response.jsonPath().getString("paymentToken"), cachedNotice.getPaymentToken());
            Assertions.assertEquals(nodeActivateResponseOk.getFiscalCodePA(), cachedNotice.getPaTaxCode());
            Assertions.assertEquals("100000000000000000", cachedNotice.getNoticeNumber());
            Assertions.assertEquals(nodeActivateResponseOk.getTotalAmount().scaleByPowerOfTen(2).longValue(), cachedNotice.getAmount());
            Assertions.assertEquals(nodeActivateResponseOk.getPaymentDescription(), cachedNotice.getDescription());
            Assertions.assertEquals(nodeActivateResponseOk.getCompanyName(), cachedNotice.getCompany());
            Assertions.assertEquals(nodeActivateResponseOk.getOfficeName(), cachedNotice.getOffice());
            Assertions.assertNull(cachedNotice.getCreditorReferenceId());
            Assertions.assertNull(cachedNotice.getDebtor());
            logger.debug("Cached notice: {}", cachedNotice);

        } catch (JsonProcessingException e) {
            logger.error("Error while reading notice from redis", e);
        }

    }

    @Test
    void testActivateByTaxCodeAndNoticeNumber_200_nodeKo() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
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
    @ValueSource(strings = {"88888888888", "99999999999", "66666666666"})
    void testActivateByTaxCodeAndNoticeNumber_200_nodeError(String paTaxCode) {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, true))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
                .and()
                .pathParam("paTaxCode", paTaxCode)
                .pathParam("noticeNumber", "100000000000000000")
                .body(activateRequest)
                .when()
                .patch("/{paTaxCode}/{noticeNumber}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
        Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
        Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
        Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));


    }

    @Test
    void testActivateByTaxCodeAndNoticeNumber_500_pspInfoNotFound() {

        ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
        activateRequest.setAmount(10099L);
        activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(PaymentTestData.getMilHeaders(true, false))
                .and()
                .auth()
                .oauth2(tokenGenerator.getToken(Role.NOTICE_PAYER))
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
        Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.UNKNOWN_ACQUIRER_ID));
        Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
        Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
        Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));

    }
}
