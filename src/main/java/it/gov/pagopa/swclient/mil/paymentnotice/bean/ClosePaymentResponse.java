package it.gov.pagopa.swclient.mil.paymentnotice.bean;

public class ClosePaymentResponse {
	
	private String outcome;

	public ClosePaymentResponse() {}
	
	public ClosePaymentResponse(String outcoume) {
		this.outcome = outcoume;
	}

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	@Override
	public String toString() {
		return "ClosePaymentResponse [outcome=" + outcome + "]";
	}
}
