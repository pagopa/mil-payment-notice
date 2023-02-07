package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Response of the activatePaymentNotice API.
 * Contains the details of the payment notice as returned by the node
 */
@RegisterForReflection
public class ActivatePaymentNoticeResponse {

	/**
	 * Outcome of the operation
	 */
	@NotNull
	@Pattern(regexp = "^(?:OK|NOTICE_GLITCH|WRONG_NOTICE_DATA|CREDITOR_PROBLEMS|PAYMENT_ALREADY_IN_PROGRESS|EXPIRED_NOTICE|REVOKED_NOTICE|NOTICE_ALREADY_PAID|UNEXPECTED_ERROR)$")
	private String outcome;

	/**
	 * Amount in euro cents
	 */
	@Min(1)
	@Max(99999999999L)
	@JsonInclude(Include.NON_NULL)
	private BigInteger amount;

	/**
	 * Tax code of the creditor company
	 */
	@Pattern(regexp = "^\\d{11}$")
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;

	/**
	 * ID of the payment activation
	 */
	@Pattern(regexp = "^[ -~]{1,35}$")
	@JsonInclude(Include.NON_NULL)
	private String paymentToken;

	/**
	 * List of transfers
	 */
	@JsonInclude(Include.NON_NULL)
	private List<Transfer> transfers;


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
	 * Gets amount
	 * @return value of amount
	 */
	public BigInteger getAmount() {
		return amount;
	}

	/**
	 * Sets amount
	 * @param amount value of amount
	 */
	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	/**
	 * Gets paTaxCode
	 * @return value of paTaxCode
	 */
	public String getPaTaxCode() {
		return paTaxCode;
	}

	/**
	 * Sets paTaxCode
	 * @param paTaxCode value of paTaxCode
	 */
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}

	/**
	 * Gets paymentToken
	 * @return value of paymentToken
	 */
	public String getPaymentToken() {
		return paymentToken;
	}

	/**
	 * Sets paymentToken
	 * @param paymentToken value of paymentToken
	 */
	public void setPaymentToken(String paymentToken) {
		this.paymentToken = paymentToken;
	}

	/**
	 * Gets transfers
	 * @return value of transfers
	 */
	public List<Transfer> getTransfers() {
		return transfers;
	}

	/**
	 * Sets transfers
	 * @param transfers value of transfers
	 */
	public void setTransfers(List<Transfer> transfers) {
		this.transfers = transfers;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ActivatePaymentResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", amount=").append(amount);
		sb.append(", paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", paymentToken='").append(paymentToken).append('\'');
		sb.append(", transfers=").append(transfers);
		sb.append('}');
		return sb.toString();
	}
}
