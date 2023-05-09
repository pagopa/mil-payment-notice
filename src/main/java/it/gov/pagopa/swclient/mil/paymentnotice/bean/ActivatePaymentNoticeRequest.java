package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

/**
 * Request of the activatePaymentNotice API
 */
public class ActivatePaymentNoticeRequest {

	/**
	 * Idempotency key for activate request
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_NOT_BE_NULL + "] idempotencyKey must not be null")
	@Pattern(regexp = "^\\d{11}_[a-zA-Z0-9]{10}$", message = "[" + ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_MATCH_REGEXP + "] idempotencyKey must match \"{regexp}\"")
	private String idempotencyKey;

	/**
	 * Amount of the payment notice in euro cents
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_AMOUNT_MUST_NOT_BE_NULL + "] amount must not be null")
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_AMOUNT_MUST_BE_GREATER_THAN + "] amount must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_AMOUNT_MUST_BE_LESS_THAN + "] amount must less than {value}")
	private Long amount;

	/**
	 * Gets idempotencyKey
	 * @return value of idempotencyKey
	 */
	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	/**
	 * Sets idempotencyKey
	 * @param idempotencyKey value of idempotencyKey
	 */
	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	/**
	 * Gets amount
	 * @return value of amount
	 */
	public Long getAmount() {
		return amount;
	}

	/**
	 * Sets amount
	 * @param amount value of amount
	 */
	public void setAmount(Long amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ActivatePaymentNoticeRequest{");
		sb.append("idempotencyKey='").append(idempotencyKey).append('\'');
		sb.append(", amount=").append(amount);
		sb.append('}');
		return sb.toString();
	}
}
