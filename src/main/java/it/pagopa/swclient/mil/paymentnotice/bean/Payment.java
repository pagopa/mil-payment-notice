package it.pagopa.swclient.mil.paymentnotice.bean;

import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import jakarta.validation.constraints.Pattern;

/**
 * Details of a payment
 */
public class Payment {

	/**
	 * ID of the payment activation
	 */
	@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP + "] paymentToken must match \"{regexp}\"")
	private String paymentToken;

	/**
	 * Payment notice description
	 */
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_DESCRIPTION_MUST_MATCH_REGEXP + "] description must match \"{regexp}\"")
	private String description;

	/**
	 * ID for the creditor company
	 */
	@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_CREDITOR_ID_REFERENCE_MUST_MATCH_REGEXP + "] creditorReferenceId must match \"{regexp}\"")
	private String creditorReferenceId;

	/**
	 * Tax code of the creditor company
	 */
	@Pattern(regexp = "^\\d{11}$", message = "[" + ErrorCode.ERROR_FISCAL_CODE_ID_MUST_MATCH_REGEXP + "] fiscalCode must match \"{regexp}\"")
	private String fiscalCode;

	/**
	 * Name of the creditor company
	 */
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_COMPANY_ID_MUST_MATCH_REGEXP + "] company must match \"{regexp}\"")
	private String company;

	/**
	 * Name of the creditor company office
	 */
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_OFFICE_ID_MUST_MATCH_REGEXP + "] office must match \"{regexp}\"")
	private String office;

	/**
	 * Debtor unstructured data
	 */
	@Pattern(regexp = "^[ -~]{0,1024}$", message = "[" + ErrorCode.ERROR_DEBTOR_MUST_MATCH_REGEXP + "] debtor must match \"{regexp}\"")
	private String debtor;


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
	 * Gets description
	 * @return value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets description
	 * @param description value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets creditorReferenceId
	 * @return value of creditorReferenceId
	 */
	public String getCreditorReferenceId() {
		return creditorReferenceId;
	}

	/**
	 * Sets creditorReferenceId
	 * @param creditorReferenceId value of creditorReferenceId
	 */
	public void setCreditorReferenceId(String creditorReferenceId) {
		this.creditorReferenceId = creditorReferenceId;
	}

	/**
	 * Gets fiscalCode
	 * @return value of fiscalCode
	 */
	public String getFiscalCode() {
		return fiscalCode;
	}

	/**
	 * Sets fiscalCode
	 * @param fiscalCode value of fiscalCode
	 */
	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	/**
	 * Gets company
	 * @return value of company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * Sets company
	 * @param company value of company
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * Gets office
	 * @return value of office
	 */
	public String getOffice() {
		return office;
	}

	/**
	 * Sets office
	 * @param office value of office
	 */
	public void setOffice(String office) {
		this.office = office;
	}

	/**
	 * Gets debtor
	 * @return value of debtor
	 */
	public String getDebtor() {
		return debtor;
	}

	/**
	 * Sets debtor
	 * @param debtor value of debtor
	 */
	public void setDebtor(String debtor) {
		this.debtor = debtor;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Payments{");
		sb.append("paymentToken='").append(paymentToken).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", creditorReferenceId='").append(creditorReferenceId).append('\'');
		sb.append(", fiscalCode='").append(fiscalCode).append('\'');
		sb.append(", company='").append(company).append('\'');
		sb.append(", office='").append(office).append('\'');
		sb.append(", debtor='").append(debtor).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
