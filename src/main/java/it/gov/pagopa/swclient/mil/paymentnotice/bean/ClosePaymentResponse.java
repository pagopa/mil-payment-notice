package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RegisterForReflection
public class ClosePaymentResponse {

	@NotNull
	@Pattern(regexp = "^(?:OK|KO)$")
	private Outcome outcome;


	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentResponse{");
		sb.append("outcome=").append(outcome);
		sb.append('}');
		return sb.toString();
	}
}
