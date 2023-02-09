package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigInteger;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

/**
 * Request of the closePayment API.
 * Contains the details of the e-money payment and its outcome
 */
public class ClosePaymentRequest {

	/**
	 * Outcome of the e-payment transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "OK|KO", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] outcome must match \"{regexp}\"")
	private String outcome;

	/**
	 * ID of the payment activation
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL + "] paymentTokens must not be null")
	@Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST + "] paymentTokens must have at most {max} elements")
	private List<@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP + "] paymentTokens element must match \"{regexp}\"") String> paymentTokens;

	/**
	 * Method used to pay notice/s
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL + "] paymentMethod must not be null")
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|BANK_ACCOUNT|CASH", message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP + "] paymentMethod must match \"{regexp}\"")
	private String paymentMethod;

	/**
	 * Transaction ID
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_NOT_BE_NULL + "] transactionId must not be null")
	@Pattern(regexp = "^[a-zA-Z0-9]{1,255}$", message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
	private String transactionId;

	/**
	 * Total amount of the payment in euro cents
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_NOT_BE_NULL + "] totalAmount must not be null")
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_GREATER_THAN + "] totalAmount must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_LESS_THAN + "] totalAmount must less than {value}")
	private BigInteger totalAmount;

	/**
	 * Total fee amount in euro cents
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_FEE_MUST_NOT_BE_NULL + "] fee must match not be null")
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_FEE_MUST_BE_GREATER_THAN + "] fee must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_FEE_MUST_BE_LESS_THAN + "] fee must less than {value}")
	private BigInteger fee;

	/**
	 * Timestamp of e-money transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_TIMESTAMP_OP_MUST_NOT_BE_NULL + "] timestampOp must not be null")
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d", message = "[" + ErrorCode.ERROR_TIMESTAMP_OP_MUST_MATCH_REGEXP + "] timestampOp must match \"{regexp}\"")
	private String timestampOp;


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
	 * Gets paymentMethod
	 * @return value of paymentMethod
	 */
	public String getPaymentMethod() {
		return paymentMethod;
	}

	/**
	 * Sets paymentMethod
	 * @param paymentMethod value of paymentMethod
	 */
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
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
	public BigInteger getTotalAmount() {
		return totalAmount;
	}

	/**
	 * Sets totalAmount
	 * @param totalAmount value of totalAmount
	 */
	public void setTotalAmount(BigInteger totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * Gets fee
	 * @return value of fee
	 */
	public BigInteger getFee() {
		return fee;
	}

	/**
	 * Sets fee
	 * @param fee value of fee
	 */
	public void setFee(BigInteger fee) {
		this.fee = fee;
	}

	/**
	 * Gets timestampOp
	 * @return value of timestampOp
	 */
	public String getTimestampOp() {
		return timestampOp;
	}

	/**
	 * Sets timestampOp
	 * @param timestampOp value of timestampOp
	 */
	public void setTimestampOp(String timestampOp) {
		this.timestampOp = timestampOp;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentRequest{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", paymentTokens=").append(paymentTokens);
		sb.append(", paymentMethod='").append(paymentMethod).append('\'');
		sb.append(", transactionId='").append(transactionId).append('\'');
		sb.append(", totalAmount=").append(totalAmount);
		sb.append(", fee=").append(fee);
		sb.append(", timestampOp='").append(timestampOp).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
