package it.pagopa.swclient.mil.paymentnotice.client.bean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Maps containing additional payment information to be passed in the closePayment API to the node.
 * Currently not implemented
 */
@JsonSerialize
public class AdditionalPaymentInformations {

	public AdditionalPaymentInformations() {
		//empty object to pass to the close payment request to the Node
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AdditionalPaymentInformations{");
		sb.append('}');
		return sb.toString();
	}
}
