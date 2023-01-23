package it.gov.pagopa.swclient.mil.paymentnotice.bean;

public class BaseResponse {
	private String outcome;

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder .append("BaseResponse [outcome=").append(outcome)
				.append("]");
		return builder.toString();
	}
}
