package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.PathParam;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

public class PaymentsPathParams {
	@PathParam(value = "transactionId")
	@NotNull(message = "[" + ErrorCode.ERROR_TRANSACTION_ID + "] taxCode must not be null")
	@Pattern(regexp = "^[a-zA-Z0-9]{1,255}$", message = "[" + ErrorCode.ERROR_TRANSACTION_ID + "] transactionId must match \"{regexp}\"")
	private String transactionId;

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@Override
	public String toString() {
		return "PaymentsPathParams [transactionId=" + transactionId + "]";
	}
}
