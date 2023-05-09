package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request of the closePayment API exposed by the node
 */
public class NodeClosePaymentRequest {

	@NotNull
	@Size(min = 1)
	private List<@Pattern(regexp = "^[ -~]{1,35}$") String> paymentTokens;

	@NotNull
	@Pattern(regexp = "OK|KO")
	private String outcome;

	@NotNull
	@Pattern(regexp = "^[ -~]{1,35}$")
	private String idPsp;

	@NotNull
	@Pattern(regexp = "^[ -~]{1,35}$")
	private String idBrokerPSP;

	@NotNull
	@Pattern(regexp = "^[ -~]{1,35}$")
	private String idChannel;

	@NotNull
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|BANK_ACCOUNT|CASH")
	private String paymentMethod;

	@NotNull
	@Pattern(regexp = "^[a-zA-Z0-9]{1,255}$")
	private String transactionId;

	@NotNull
	@Min(value = 1L)
	@Max(value = 999999999L)
	private BigDecimal totalAmount;

	@NotNull
	@Min(value = 1L)
	@Max(value = 999999999L)
	private BigDecimal fee;

	@NotNull
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d")
	private String timestampOperation;

	@NotNull
	private AdditionalPaymentInformations additionalPaymentInformations;


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
	 * Gets idPsp
	 * @return value of idPsp
	 */
	public String getIdPsp() {
		return idPsp;
	}

	/**
	 * Sets idPsp
	 * @param idPsp value of idPsp
	 */
	public void setIdPsp(String idPsp) {
		this.idPsp = idPsp;
	}

	/**
	 * Gets idBrokerPSP
	 *
	 * @return value of idBrokerPSP
	 */
	public String getIdBrokerPSP() {
		return idBrokerPSP;
	}

	/**
	 * Sets idBrokerPSP
	 *
	 * @param idBrokerPSP value of idBrokerPSP
	 */
	public void setIdBrokerPSP(String idBrokerPSP) {
		this.idBrokerPSP = idBrokerPSP;
	}

	/**
	 * Gets idChannel
	 * @return value of idChannel
	 */
	public String getIdChannel() {
		return idChannel;
	}

	/**
	 * Sets idChannel
	 * @param idChannel value of idChannel
	 */
	public void setIdChannel(String idChannel) {
		this.idChannel = idChannel;
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
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	/**
	 * Sets totalAmount
	 * @param totalAmount value of totalAmount
	 */
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * Gets fee
	 * @return value of fee
	 */
	public BigDecimal getFee() {
		return fee;
	}

	/**
	 * Sets fee
	 * @param fee value of fee
	 */
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	/**
	 * Gets timestampOperation
	 * @return value of timestampOperation
	 */
	public String getTimestampOperation() {
		return timestampOperation;
	}

	/**
	 * Sets timestampOperation
	 * @param timestampOperation value of timestampOperation
	 */
	public void setTimestampOperation(String timestampOperation) {
		this.timestampOperation = timestampOperation;
	}

	/**
	 * Gets additionalPaymentInformations
	 * @return value of additionalPaymentInformations
	 */
	public AdditionalPaymentInformations getAdditionalPaymentInformations() {
		return additionalPaymentInformations;
	}

	/**
	 * Sets additionalPaymentInformations
	 * @param additionalPaymentInformations value of additionalPaymentInformations
	 */
	public void setAdditionalPaymentInformations(AdditionalPaymentInformations additionalPaymentInformations) {
		this.additionalPaymentInformations = additionalPaymentInformations;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ClosePaymentRequest{");
		sb.append("paymentTokens=").append(paymentTokens);
		sb.append(", outcome='").append(outcome).append('\'');
		sb.append(", idPsp='").append(idPsp).append('\'');
		sb.append(", pspBroker='").append(idBrokerPSP).append('\'');
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
