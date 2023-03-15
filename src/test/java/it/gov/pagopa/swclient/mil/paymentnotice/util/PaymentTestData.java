package it.gov.pagopa.swclient.mil.paymentnotice.util;

import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentMethod;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PaymentTestData {

    public static Map<String, String> getMilHeaders(boolean isPos, boolean isKnownAcquirer) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        headerMap.put("AcquirerId", isKnownAcquirer ? PaymentTestData.ACQUIRER_ID_KNOWN : PaymentTestData.ACQUIRER_ID_NOT_KNOWN);
        headerMap.put("Channel", isPos ? "POS" : "ATM");
        headerMap.put("TerminalId", "0aB9wXyZ");
        if (isPos) headerMap.put("MerchantId", "28405fHfk73x88D");
        headerMap.put("SessionId", UUID.randomUUID().toString());
        return headerMap;
    }

    public static AcquirerConfiguration getAcquirerConfiguration() {
        AcquirerConfiguration acquirerConfiguration = new AcquirerConfiguration();

        PspConfiguration pspConfiguration = new PspConfiguration();
        pspConfiguration.setPsp("AGID_01");
        pspConfiguration.setBroker("97735020584");
        pspConfiguration.setChannel("97735020584_07");
        pspConfiguration.setPassword("PLACEHOLDER");

        acquirerConfiguration.setPspConfigForVerifyAndActivate(pspConfiguration);
        acquirerConfiguration.setPspConfigForGetFeeAndClosePayment(pspConfiguration);

        return acquirerConfiguration;
    }

    public static ActivatePaymentNoticeRequest getActivatePaymentRequest() {
        ActivatePaymentNoticeRequest activatePaymentNoticeRequest = new ActivatePaymentNoticeRequest();
        activatePaymentNoticeRequest.setIdempotencyKey("77777777777_abcDEF1238");
        activatePaymentNoticeRequest.setAmount(10000L);
        return activatePaymentNoticeRequest;
    }

    public static ClosePaymentRequest getClosePaymentRequest(boolean isOk) {
        ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
        closePaymentRequest.setOutcome(isOk ? Outcome.OK.name() : Outcome.KO.name());
        closePaymentRequest.setPaymentTokens(List.of("648fhg36s95jfg7DS"));
        closePaymentRequest.setPaymentMethod(PaymentMethod.PAGOBANCOMAT.name());
        closePaymentRequest.setTransactionId("517a4216840E461fB011036A0fd134E1");
        closePaymentRequest.setTotalAmount(BigInteger.valueOf(234234));
        closePaymentRequest.setFee(BigInteger.valueOf(897));
        closePaymentRequest.setTimestampOp("2022-11-12T08:53:55");
        return closePaymentRequest;
    }

    /**
     * Example taken from <a href="https://docs.pagopa.it/avviso-pagamento/struttura/specifiche-tecniche/dati-per-il-pagamento/codice-qr">QR Code specification</a>
     */
    public static final String QR_CODE = "PAGOPA|002|000000000000000000|00000000000|9999";

    // ACQUIRER ID
    public static final String ACQUIRER_ID_KNOWN = "4585625";
    public static final String ACQUIRER_ID_NOT_KNOWN = "4585626";

    // CLOSE PAYMENT TRANSACTION ID
    public static final String PAY_TID_NODE_OK = "8b19db3262384cde9ced78cb0f059c5f";
    public static final String PAY_TID_NODE_KO = "27de01c5c4b24a48802a59696c2bef20";
    public static final String PAY_TID_NODE_400 = "0af4576bd3654abb83713ae84b32ce50";
    public static final String PAY_TID_NODE_404 = "724a08e550094699880498a71a65cd47";
    public static final String PAY_TID_NODE_408 = "50a4853f77694cfe91386961a3ff0646";
    public static final String PAY_TID_NODE_422 = "b1ec45e154fb48c494129f74d97ae66e";
    public static final String PAY_TID_NODE_500 = "519769bcafad45d8be9c569002499e96";
    public static final String PAY_TID_NODE_TIMEOUT = "968b64b284dc48a08eb948d8777bf9e6";
    public static final String PAY_TID_NODE_UNPARSABLE = "2720236097a54d2799a41671b0585747";


    private PaymentTestData() {
    }

}
