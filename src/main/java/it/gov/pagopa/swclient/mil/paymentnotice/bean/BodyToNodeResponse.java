package it.gov.pagopa.swclient.mil.paymentnotice.bean;

public class BodyToNodeResponse {

	private String outcome;

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	@Override
	public String toString() {
		return "BodyToNodeResponse [outcome=" + outcome + "]";
	}
}
