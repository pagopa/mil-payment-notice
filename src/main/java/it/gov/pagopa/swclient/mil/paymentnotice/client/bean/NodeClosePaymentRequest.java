package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class NodeClosePaymentRequest {

	@NotNull
	private List<String> paymentTokens;

	@NotNull
	private String outcome;

	@NotNull
	private String idPsp;

	@NotNull
	private String pspBroker;

	@NotNull
	private String idChannel;

	@NotNull
	private String paymentMethod;

	@NotNull
	private String transactionId;

	@NotNull
	private BigDecimal totalAmount;

	@NotNull
	private BigDecimal fee;

	@NotNull
	private String timestampOperation;

	@NotNull
	private AdditionalPaymentInformations additionalPaymentInformations;


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

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public String getTimestampOperation() {
		return timestampOperation;
	}

	public void setTimestampOperation(String timestampOperation) {
		this.timestampOperation = timestampOperation;
	}

	public AdditionalPaymentInformations getAdditionalPaymentInformations() {
		return additionalPaymentInformations;
	}

	public void setAdditionalPaymentInformations(AdditionalPaymentInformations additionalPaymentInformations) {
		this.additionalPaymentInformations = additionalPaymentInformations;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentRequest{");
		sb.append("paymentTokens=").append(paymentTokens);
		sb.append(", outcome='").append(outcome).append('\'');
		sb.append(", idPsp='").append(idPsp).append('\'');
		sb.append(", pspBroker='").append(pspBroker).append('\'');
		sb.append(", idChannel='").append(idChannel).append('\'');
		sb.append(", paymentMethod='").append(paymentMethod).append('\'');
		sb.append(", transactionId='").append(transactionId).append('\'');
		sb.append(", totalAmount=").append(totalAmount);
		sb.append(", fee=").append(fee);
		sb.append(", timestampOperation='").append(timestampOperation).append('\'');
		sb.append(", additionalPaymentInformations=").append(additionalPaymentInformations);
		sb.append('}');
		return sb.toString();
	}
}
