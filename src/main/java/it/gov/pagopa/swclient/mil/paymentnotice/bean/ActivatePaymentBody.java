package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;


public class ActivatePaymentBody {
	
	@Pattern(regexp = "^\\d{11}_[a-zA-Z0-9]{10}$", message = "[" + ErrorCode.ERROR_IDEMPOTENCY_KEY + "] itempotencyKey must match \"{regexp}\"")
	private String idempotencyKey;
	
	private long amount;
	
	@AssertTrue(message = "[" + ErrorCode.ERROR_INVALID_AMOUNT + "] amount passed in the body is not valid")
    private boolean isAmountValid() {
        return amount >= 1 && amount <= 99999999999L;
    }

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "ActivatePaymentBody [idempotencyKey=" + idempotencyKey + ", amount=" + amount + "]";
	}
}
