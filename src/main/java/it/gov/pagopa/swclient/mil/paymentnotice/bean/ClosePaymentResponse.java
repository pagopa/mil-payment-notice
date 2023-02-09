package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Response of the closePayment API.
 * Because the closePayment operation is asynchronous on the node, the outcome is just a notification
 * that the node has validated the request and that it will be processed
 */
@RegisterForReflection
public class ClosePaymentResponse {

	/**
	 * Outcome of the take in charge of the close payment
	 */
	@NotNull
	@Pattern(regexp = "^(?:OK|KO)$")
	private Outcome outcome;

	/**
	 * Gets outcome
	 * @return value of outcome
	 */
	public Outcome getOutcome() {
		return outcome;
	}

	/**
	 * Sets outcome
	 * @param outcome value of outcome
	 */
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
