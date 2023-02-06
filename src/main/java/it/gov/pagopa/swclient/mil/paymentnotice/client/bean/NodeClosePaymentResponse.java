package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class NodeClosePaymentResponse {
	
	private String outcome;

	@NotNull
	@Pattern(regexp = "OK|KO")
	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("NodeClosePaymentResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
