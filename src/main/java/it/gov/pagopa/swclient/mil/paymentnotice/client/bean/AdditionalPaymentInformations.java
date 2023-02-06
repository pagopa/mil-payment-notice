package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class AdditionalPaymentInformations {

	public AdditionalPaymentInformations() {
		//empty object to pass to the close payment request to the Node
	}
}
