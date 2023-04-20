package it.gov.pagopa.swclient.mil.paymentnotice.bean;

/**
 * Enum mapping the possible outcome in request of the preClose and closePayment APIs
 */
public enum PaymentTransactionOutcome {

	PRE_CLOSE,
	ERROR_ON_PAYMENT,
	CLOSE,
	ABORT

}
