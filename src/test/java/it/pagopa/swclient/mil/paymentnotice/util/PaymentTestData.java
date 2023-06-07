package it.pagopa.swclient.mil.paymentnotice.util;

import it.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.PaymentMethod;
import it.pagopa.swclient.mil.paymentnotice.bean.PaymentTransactionOutcome;
import it.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.pagopa.swclient.mil.paymentnotice.bean.Preset;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import it.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public static PreCloseRequest getPreCloseRequest(boolean isPreClose, int tokens, boolean isPreset) {
        PreCloseRequest preCloseRequest = new PreCloseRequest();
        if (isPreClose) {
            preCloseRequest.setOutcome(PaymentTransactionOutcome.PRE_CLOSE.name());
            preCloseRequest.setTransactionId(RandomStringUtils.random(32, true, true));
            preCloseRequest.setTotalAmount(AMOUNT*tokens);
            preCloseRequest.setFee(100L);
        }
        else {
            preCloseRequest.setOutcome(PaymentTransactionOutcome.ABORT.name());
        }

        // payment tokens are always present in preclose
        List<String> paymentTokens = new ArrayList<>(tokens);
        for (int i = 0; i < tokens; i++) {
            paymentTokens.add(RandomStringUtils.random(32, true, true));
        }
        preCloseRequest.setPaymentTokens(paymentTokens);

        // preset is optional
        if (isPreset) {
            preCloseRequest.setPreset(getPreset());
        }

        return preCloseRequest;
    }

    public static ClosePaymentRequest getClosePaymentRequest(boolean isOk) {
        ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
        closePaymentRequest.setOutcome(isOk ? PaymentTransactionOutcome.CLOSE.name() :
                PaymentTransactionOutcome.ERROR_ON_PAYMENT.name());
        closePaymentRequest.setPaymentMethod(PaymentMethod.PAGOBANCOMAT.name());
        closePaymentRequest.setPaymentTimestamp("2022-11-12T08:53:55");
        return closePaymentRequest;
    }

    public static Notice getNotice(String paymentToken) {
        Notice notice = new Notice();
        notice.setPaymentToken(paymentToken);
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber(NOTICE_NUMBER);
        notice.setAmount(AMOUNT);
        notice.setDescription("Test payment notice");
        notice.setCompany("Test company");
        notice.setOffice("Test office");
        return notice;
    }

    public static Preset getPreset() {
        String presetId = UUID.randomUUID().toString();
        String subscriberId = org.apache.commons.lang3.RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom()).toLowerCase();
        return getPreset(presetId, subscriberId);
    }

    public static Preset getPreset(String presetId, String subscriberId) {
        Preset preset = new Preset();
        preset.setPresetId(presetId);
        preset.setPaTaxCode(PA_TAX_CODE);
        preset.setSubscriberId(subscriberId);
        return preset;
    }

    public static PaymentTransactionEntity getPaymentTransaction(String transactionId,
                                                                 PaymentTransactionStatus status,
                                                                 Map<String, String> headers,
                                                                 int tokens,
                                                                 Preset preset) {

        if (status == PaymentTransactionStatus.ABORTED) throw new IllegalArgumentException();

        String timestamp = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

        var paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionId(transactionId);
        paymentTransaction.setAcquirerId(headers.get("AcquirerId"));
        paymentTransaction.setChannel(headers.get("Channel"));
        paymentTransaction.setMerchantId(headers.get("MerchantId"));
        paymentTransaction.setTerminalId(headers.get("TerminalId"));
        paymentTransaction.setInsertTimestamp(timestamp);

        List<Notice> notices = new ArrayList<>();
        for (int i = 0; i < tokens; i++) {
            notices.add(getNotice(RandomStringUtils.random(32, true, true)));
        }

        paymentTransaction.setNotices(notices);
        paymentTransaction.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));

        paymentTransaction.setFee(100L);
        paymentTransaction.setStatus(status.name());

        switch (status) {
            case PRE_CLOSE -> {}
            case PENDING,ERROR_ON_PAYMENT, ERROR_ON_CLOSE -> {
                paymentTransaction.setPaymentMethod("PAGOBANCOMAT");
                paymentTransaction.setPaymentTimestamp(timestamp);
                paymentTransaction.setCloseTimestamp(timestamp);
            }
            case CLOSED, ERROR_ON_RESULT -> {
                paymentTransaction.setPaymentMethod("PAGOBANCOMAT");
                paymentTransaction.setPaymentTimestamp(timestamp);
                paymentTransaction.setCloseTimestamp(timestamp);
                paymentTransaction.setPaymentDate(timestamp);
                paymentTransaction.setCallbackTimestamp(timestamp);
                notices.forEach(n -> {
                    n.setDebtor("Mario Rossi");
                    n.setCreditorReferenceId("abcde");
                });
            }
        }

        paymentTransaction.setPreset(preset);

        var paymentTransactionEntity = new PaymentTransactionEntity();
        paymentTransactionEntity.transactionId = transactionId;
        paymentTransactionEntity.paymentTransaction = paymentTransaction;

        return paymentTransactionEntity;

    }

    public static final String PA_TAX_CODE = "77777777777";
    public static final String NOTICE_NUMBER = "000000000000000000";
    public static final long AMOUNT = 9999;

    /**
     * Example taken from <a href="https://docs.pagopa.it/avviso-pagamento/struttura/specifiche-tecniche/dati-per-il-pagamento/codice-qr">QR Code specification</a>
     */
    public static final String QR_CODE = "PAGOPA|002|"+NOTICE_NUMBER+"|"+PA_TAX_CODE+"|"+AMOUNT;

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
