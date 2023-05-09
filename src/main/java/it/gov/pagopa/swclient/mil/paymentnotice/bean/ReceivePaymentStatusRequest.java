package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

/**
 * Request of the receivePaymentStatus API, used by the node to notify the result od the asynchronous closePayment operation.
 * Contains the details of the payment and the outcome of the closePayment operation
 */
public class ReceivePaymentStatusRequest {

	/**
	 * Outcome of the payment
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "^(?:OK|KO)$", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] outcome must match \"{regexp}\"")
	private String outcome;

	/**
	 * Notification timestamp
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_DATE_MUST_NOT_BE_NULL + "] paymentDate must not be null")
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d",
			message = "[" + ErrorCode.ERROR_PAYMENT_DATE_MUST_MATCH_REGEXP + "] paymentDate must match \"{regexp}\"")
	private String paymentDate;

	/**
	 * Details of payment notices
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENTS_MUST_NOT_BE_NULL + "] payments must not be null")
	@Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMENTS_MUST_HAVE_AT_MOST_ELEMENTS + "] payments must have at most {max} elements")
	private List<@Valid Payment> payments;


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
	 * Gets paymentDate
	 * @return value of paymentDate
	 */
	public String getPaymentDate() {
		return paymentDate;
	}

	/**
	 * Sets paymentDate
	 * @param paymentDate value of paymentDate
	 */
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	/**
	 * Gets payments
	 * @return value of payments
	 */
	public List<Payment> getPayments() {
		return payments;
	}

	/**
	 * Sets payments
	 * @param payments value of payments
	 */
	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ReceivePaymentStatusRequest{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", paymentDate='").append(paymentDate).append('\'');
		sb.append(", payments=").append(payments);
		sb.append('}');
		return sb.toString();
	}
}
