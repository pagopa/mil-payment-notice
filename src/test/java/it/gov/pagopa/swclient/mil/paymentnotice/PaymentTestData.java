package it.gov.pagopa.swclient.mil.paymentnotice;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public final class PaymentTestData {

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
