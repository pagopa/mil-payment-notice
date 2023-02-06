package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

public class ReceivePaymentStatusRequest {

	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "^(?:OK|KO)$", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] outcome must match \"{regexp}\"")
	private String outcome;

	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_DATE_MUST_NOT_BE_NULL + "] paymentDate must not be null")
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d",
			message = "[" + ErrorCode.ERROR_PAYMENT_DATE_MUST_MATCH_REGEXP + "] paymentDate must match \"{regexp}\"")
	private String paymentDate;

	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENTS_MUST_NOT_BE_NULL + "] payments must not be null")
	@Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMENTS_MUST_HAVE_AT_MOST_ELEMENTS + "] payments must have at most {max} elements")
	private List<@Valid Payment> payments;


	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public List<Payment> getPayments() {
		return payments;
	}

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
