package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request of the closePayment API.
 * Contains the details of the e-money payment and its outcome
 */
public class ClosePaymentRequest {

	/**
	 * Outcome of the e-payment transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "CLOSE|ERROR_ON_PAYMENT", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] outcome must match \"{regexp}\"")
	private String outcome;


	/**
	 * Method used to pay notice/s
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL + "] paymentMethod must not be null")
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|PAYMENT_CARD|BANK_ACCOUNT|CASH",
			message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP + "] paymentMethod must match \"{regexp}\"")
	private String paymentMethod;


	/**
	 * Timestamp of e-money transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_NOT_BE_NULL + "] paymentTimestamp must not be null")
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d",
			message = "[" + ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_MATCH_REGEXP + "] paymentTimestamp must match \"{regexp}\"")
	private String paymentTimestamp;


	/**
	 * Gets outcome
	 * @return value of outcome
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * Sets outcome
	 * @param outcome value of outcome
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	/**
	 * Gets paymentMethod
	 * @return value of paymentMethod
	 */
	public String getPaymentMethod() {
		return paymentMethod;
	}

	/**
	 * Sets paymentMethod
	 * @param paymentMethod value of paymentMethod
	 */
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	/**
	 * Gets paymentTimestamp
	 * @return value of paymentTimestamp
	 */
	public String getPaymentTimestamp() {
		return paymentTimestamp;
	}

	/**
	 * Sets paymentTimestamp
	 * @param paymentTimestamp value of paymentTimestamp
	 */
	public void setPaymentTimestamp(String paymentTimestamp) {
		this.paymentTimestamp = paymentTimestamp;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentRequest{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", paymentMethod='").append(paymentMethod).append('\'');
		sb.append(", paymentTimestamp='").append(paymentTimestamp).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
