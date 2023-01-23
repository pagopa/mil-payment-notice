package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.util.List;

public class ClosePaymentRequest {

	private List<String> paymentTokens;
	private String outcome;
	private String idPsp;
	private String pspBroker;
	private String idChannel;
	private String paymentMethod;
	private String transactionId;
	private String totalAmount;
	private String fee;
	private String timestampOperation;
	private AdditionaPaymentInformation additionaPaymentInformation;
	public List<String> getPaymentTokens() {
		return paymentTokens;
	}
	public void setPaymentTokens(List<String> paymentTokens) {
		this.paymentTokens = paymentTokens;
	}
	public String getOutcome() {
		return outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
	public String getIdPsp() {
		return idPsp;
	}
	public void setIdPsp(String idPsp) {
		this.idPsp = idPsp;
	}
	public String getPspBroker() {
		return pspBroker;
	}
	public void setPspBroker(String pspBroker) {
		this.pspBroker = pspBroker;
	}
	public String getIdChannel() {
		return idChannel;
	}
	public void setIdChannel(String idChannel) {
		this.idChannel = idChannel;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getFee() {
		return fee;
	}
	public void setFee(String fee) {
		this.fee = fee;
	}
	public String getTimestampOperation() {
		return timestampOperation;
	}
	public void setTimestampOperation(String timestampOperation) {
		this.timestampOperation = timestampOperation;
	}
	public AdditionaPaymentInformation getAdditionaPaymentInformation() {
		return additionaPaymentInformation;
	}
	public void setAdditionaPaymentInformation(AdditionaPaymentInformation additionaPaymentInformation) {
		this.additionaPaymentInformation = additionaPaymentInformation;
	}
	@Override
	public String toString() {
		return "ClosePaymentRequest [paymentTokens=" + paymentTokens + ", outcome=" + outcome + ", idPsp=" + idPsp
				+ ", pspBroker=" + pspBroker + ", idChannel=" + idChannel + ", paymentMethod=" + paymentMethod
				+ ", transactionId=" + transactionId + ", totalAmount=" + totalAmount + ", fee=" + fee
				+ ", timestampOperation=" + timestampOperation + ", additionaPaymentInformation="
				+ additionaPaymentInformation + "]";
	}
	
}
