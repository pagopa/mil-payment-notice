package it.pagopa.swclient.mil.paymentnotice.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.paymentnotice.bean.Preset;

import java.util.List;

/**
 * Entity bean containing the data of a payment transaction
 */
@RegisterForReflection
public class PaymentTransaction {

	/**
	 * The identifier of the payment transaction
	 */
	private String transactionId;

	/**
	 * The identifier of the acquirer, passed by the client
	 */
	private String acquirerId;

	/**
	 * The channel used for the payment, passed by the client
	 */
	private String channel;

	/**
	 * The identifier of the merchant, passed by the client
	 */
	private String merchantId;

	/**
	 * The identifier of the terminal, passed by the client
	 */
	private String terminalId;

	/**
	 * The timestamp of the transaction creation on the DB
	 */
	private String insertTimestamp;

	/**
     * The list of notices paid in this transaction
	 */
	private List<Notice> notices;

	/**
	 * The total amount of the payment notices, calculated by the MIL
	 */
	private long totalAmount;

	/**
	 * The total fee for the payment transaction, retrieved by GEC and passed by the client
	 */
	private Long fee;

	/**
	 * The status of this payment transaction. Can be one of {@link PaymentTransactionStatus}
	 */
	private String status;

	/**
	 * The payment method used for this transaction, passed by the client
	 */
	private String paymentMethod;

	/**
	 * Timestamp of the e-money transaction ,  passed by the client
	 */
	private String paymentTimestamp;

	/**
	 * Timestamp of the call to the close API
	 */
	private String closeTimestamp;

	/**
	 *  Timestamp of the transaction payment, passed by the node in the callback
	 */
	private String paymentDate;

	/**
	 * Timestamp of the call to the callback by the node
	 */
	private String callbackTimestamp;

	/**
	 * Preset information
	 */
	private Preset preset;
	
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

	/**
	 * @return the preset
	 */
	public Preset getPreset() {
		return preset;
	}

	/**
	 * @param preset the preset to set
	 */
	public void setPreset(Preset preset) {
		this.preset = preset;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PaymentTransaction [transactionId=");
		builder.append(transactionId);
		builder.append(", acquirerId=");
		builder.append(acquirerId);
		builder.append(", channel=");
		builder.append(channel);
		builder.append(", merchantId=");
		builder.append(merchantId);
		builder.append(", terminalId=");
		builder.append(terminalId);
		builder.append(", insertTimestamp=");
		builder.append(insertTimestamp);
		builder.append(", notices=");
		builder.append(notices);
		builder.append(", totalAmount=");
		builder.append(totalAmount);
		builder.append(", fee=");
		builder.append(fee);
		builder.append(", status=");
		builder.append(status);
		builder.append(", paymentMethod=");
		builder.append(paymentMethod);
		builder.append(", paymentTimestamp=");
		builder.append(paymentTimestamp);
		builder.append(", closeTimestamp=");
		builder.append(closeTimestamp);
		builder.append(", paymentDate=");
		builder.append(paymentDate);
		builder.append(", callbackTimestamp=");
		builder.append(callbackTimestamp);
		builder.append(", preset=");
		builder.append(preset);
		builder.append("]");
		return builder.toString();
	}
}
