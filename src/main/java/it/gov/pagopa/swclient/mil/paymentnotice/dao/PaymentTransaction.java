package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 *
 */
@RegisterForReflection
public class PaymentTransaction {

	/**
	 *
	 */
	private String transactionId;

	/**
	 *
	 */
	private String acquirerId;

	/**
	 *
	 */
	private String channel;

	/**
	 *
	 */
	private String merchantId;

	/**
	 *
	 */
	private String terminalId;

	/**
	 *
	 */
	private String insertTimestamp;

	/**
     *
	 */
	private List<Notice> notices;

	/**
	 *
	 */
	private long totalAmount;

	/**
	 *
	 */
	private Long fee;

	/**
	 *
	 */
	private String status;

	/**
	 *
	 */
	private String paymentMethod;

	/**
	 *
	 */
	private String paymentTimestamp;

	/**
	 *
	 */
	private String closeTimestamp;

	/**
	 *
	 */
	private String paymentDate;

	/**
	 *
	 */
	private String callbackTimestamp;

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
	 * Gets acquirerId
	 * @return value of acquirerId
	 */
	public String getAcquirerId() {
		return acquirerId;
	}

	/**
	 * Sets acquirerId
	 * @param acquirerId value of acquirerId
	 */
	public void setAcquirerId(String acquirerId) {
		this.acquirerId = acquirerId;
	}

	/**
	 * Gets channel
	 * @return value of channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Sets channel
	 * @param channel value of channel
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * Gets merchantId
	 * @return value of merchantId
	 */
	public String getMerchantId() {
		return merchantId;
	}

	/**
	 * Sets merchantId
	 * @param merchantId value of merchantId
	 */
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	/**
	 * Gets terminalId
	 * @return value of terminalId
	 */
	public String getTerminalId() {
		return terminalId;
	}

	/**
	 * Sets terminalId
	 * @param terminalId value of terminalId
	 */
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	/**
	 * Gets insertTimestamp
	 * @return value of insertTimestamp
	 */
	public String getInsertTimestamp() {
		return insertTimestamp;
	}

	/**
	 * Sets insertTimestamp
	 * @param insertTimestamp value of insertTimestamp
	 */
	public void setInsertTimestamp(String insertTimestamp) {
		this.insertTimestamp = insertTimestamp;
	}

	/**
	 * Gets notices
	 * @return value of notices
	 */
	public List<Notice> getNotices() {
		return notices;
	}

	/**
	 * Sets notices
	 * @param notices value of notices
	 */
	public void setNotices(List<Notice> notices) {
		this.notices = notices;
	}

	/**
	 * Gets totalAmount
	 * @return value of totalAmount
	 */
	public long getTotalAmount() {
		return totalAmount;
	}

	/**
	 * Sets totalAmount
	 * @param totalAmount value of totalAmount
	 */
	public void setTotalAmount(long totalAmount) {
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

	/**
	 * Gets status
	 * @return value of status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets status
	 * @param status value of status
	 */
	public void setStatus(String status) {
		this.status = status;
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
	 * Gets paymentTimestamp
	 * @return value of paymentTimestamp
	 */
	public String getPaymentTimestamp() {
		return paymentTimestamp;
	}

	/**
	 * Sets paymentTimestamp
	 * @param paymentTimestamp value of paymentTimestamp
	 */
	public void setPaymentTimestamp(String paymentTimestamp) {
		this.paymentTimestamp = paymentTimestamp;
	}

	/**
	 * Gets closeTimestamp
	 * @return value of closeTimestamp
	 */
	public String getCloseTimestamp() {
		return closeTimestamp;
	}

	/**
	 * Sets closeTimestamp
	 * @param closeTimestamp value of closeTimestamp
	 */
	public void setCloseTimestamp(String closeTimestamp) {
		this.closeTimestamp = closeTimestamp;
	}

	/**
	 * Gets paymentDate
	 * @return value of paymentDate
	 */
	public String getPaymentDate() {
		return paymentDate;
	}

	/**
	 * Sets paymentDate
	 * @param paymentDate value of paymentDate
	 */
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	/**
	 * Gets callbackTimestamp
	 * @return value of callbackTimestamp
	 */
	public String getCallbackTimestamp() {
		return callbackTimestamp;
	}

	/**
	 * Sets callbackTimestamp
	 * @param callbackTimestamp value of callbackTimestamp
	 */
	public void setCallbackTimestamp(String callbackTimestamp) {
		this.callbackTimestamp = callbackTimestamp;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PaymentTransaction{");
		sb.append("transactionId='").append(transactionId).append('\'');
		sb.append(", acquirerId='").append(acquirerId).append('\'');
		sb.append(", channel='").append(channel).append('\'');
		sb.append(", merchantId='").append(merchantId).append('\'');
		sb.append(", terminalId='").append(terminalId).append('\'');
		sb.append(", insertTimestamp='").append(insertTimestamp).append('\'');
		sb.append(", notices=").append(notices);
		sb.append(", totalAmount=").append(totalAmount);
		sb.append(", fee=").append(fee);
		sb.append(", status='").append(status).append('\'');
		sb.append(", paymentMethod='").append(paymentMethod).append('\'');
		sb.append(", paymentTimestamp='").append(paymentTimestamp).append('\'');
		sb.append(", closeTimestamp='").append(closeTimestamp).append('\'');
		sb.append(", paymentDate='").append(paymentDate).append('\'');
		sb.append(", callbackTimestamp='").append(callbackTimestamp).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
