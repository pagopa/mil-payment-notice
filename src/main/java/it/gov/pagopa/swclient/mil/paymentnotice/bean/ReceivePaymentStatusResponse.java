package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RegisterForReflection
public class ReceivePaymentStatusResponse {

	@NotNull
	@Pattern(regexp = "^OK$")
	private String outcome;


	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ReceivePaymentStatusResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
