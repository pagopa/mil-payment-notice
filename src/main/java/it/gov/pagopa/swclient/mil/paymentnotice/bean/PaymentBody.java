package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.util.List;

import javax.validation.constraints.Pattern;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

public class PaymentBody {

	@Pattern(regexp = "OK|KO", message = "[" + ErrorCode.ERROR_OUTCOME + "] outcome must match \"{regexp}\"")
	private String outcome;
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d", message = "[" + ErrorCode.ERROR_TIMESTAMPOP + "] paymentDate must match \"{regexp}\"")
	private String paymentDate;
	
	private List<Payments> payments;

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

	public List<Payments> getPayments() {
		return payments;
	}

	public void setPayments(List<Payments> payments) {
		this.payments = payments;
	}

	@Override
	public String toString() {
		return "PaymentBody [outcome=" + outcome + ", paymentDate=" + paymentDate + ", payments=" + payments + "]";
	} 
}
