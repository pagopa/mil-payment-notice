package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

public class NodeClosePaymentResponse {
	
	private String outcome;


	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
