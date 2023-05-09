package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Response of the receivePaymentStatus API
 */
@RegisterForReflection
public class ReceivePaymentStatusResponse {

	/**
	 * The outcome of the operation
	 */
	@NotNull
	@Pattern(regexp = "^OK$")
	private String outcome;


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


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ReceivePaymentStatusResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
