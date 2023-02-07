package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Response of the closePayment API exposed by the node.
 * Because the closePayment operation is asynchronous on the node, the OK outcome is just a confirmation that the
 * node will process the request, but not its actual status
 */
public class NodeClosePaymentResponse {

	/**
	 * Outcome of the take in charge of the close payment
	 */
	@NotNull
	@Pattern(regexp = "OK|KO")
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
		final StringBuilder sb = new StringBuilder("NodeClosePaymentResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
