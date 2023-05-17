package it.pagopa.swclient.mil.paymentnotice.bean;

import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request of the preClose API
 */
public class PreCloseRequest {

	/**
	 * Operation to perform in the preClose step
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "PRE_CLOSE|ABORT", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] idempotencyKey must match \"{regexp}\"")
	private String outcome;

	/**
	 * IDs of the payment activations
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL + "] paymentTokens must not be null")
	@Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST + "] paymentTokens must have at most {max} elements")
	private List<@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP + "] paymentTokens element must match \"{regexp}\"") String> paymentTokens;

	/**
	 * Transaction ID
	 */
	@Pattern(regexp = PaymentNoticeConstants.TRANSACTION_ID_REGEX, message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
	private String transactionId;

	/**
	 * Total amount in euro cents
	 */
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_GREATER_THAN + "] totalAmount must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_LESS_THAN + "] totalAmount must less than {value}")
	private Long totalAmount;

	/**
	 * Total fee amount in euro cents
	 */
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_FEE_MUST_BE_GREATER_THAN + "] fee must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_FEE_MUST_BE_LESS_THAN + "] fee must less than {value}")
	private Long fee;

	@AssertFalse(message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_NOT_BE_NULL + "] transactionId must not be null when outcome is PRE_CLOSE")
	private boolean isTransactionIdNullForPreClose() {
		return PaymentTransactionOutcome.PRE_CLOSE.name().equals(outcome) && transactionId == null;
	}

	@AssertFalse(message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_NOT_BE_NULL + "] totalAmount must not be null when outcome is PRE_CLOSE")
	private boolean isTotalAmountNullForPreClose() {
		return PaymentTransactionOutcome.PRE_CLOSE.name().equals(outcome) && totalAmount == null;
	}

	@AssertFalse(message = "[" + ErrorCode.ERROR_FEE_MUST_NOT_BE_NULL + "] fee must not be null when outcome is PRE_CLOSE")
	private boolean isFeeNullForPreClose() {
		return PaymentTransactionOutcome.PRE_CLOSE.name().equals(outcome) && fee == null;
	}


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

	/**
	 * Gets paymentTokens
	 * @return value of paymentTokens
	 */
	public List<String> getPaymentTokens() {
		return paymentTokens;
	}

	/**
	 * Sets paymentTokens
	 * @param paymentTokens value of paymentTokens
	 */
	public void setPaymentTokens(List<String> paymentTokens) {
		this.paymentTokens = paymentTokens;
	}

	/**
	 * Gets transactionId
	 * @return value of transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * Sets transactionId
	 * @param transactionId value of transactionId
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Gets totalAmount
	 * @return value of totalAmount
	 */
	public Long getTotalAmount() {
		return totalAmount;
	}

	/**
	 * Sets totalAmount
	 * @param totalAmount value of totalAmount
	 */
	public void setTotalAmount(Long totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * Gets fee
	 * @return value of fee
	 */
	public Long getFee() {
		return fee;
	}

	/**
	 * Sets fee
	 * @param fee value of fee
	 */
	public void setFee(Long fee) {
		this.fee = fee;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PreCloseRequest{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", paymentTokens=").append(paymentTokens);
		sb.append(", transactionId='").append(transactionId).append('\'');
		sb.append(", totalAmount=").append(totalAmount);
		sb.append(", fee=").append(fee);
		sb.append('}');
		return sb.toString();
	}
}
