package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import javax.validation.constraints.Pattern;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

public class Payments {
	@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_NOT_VALID + "] paymnetToken must match \"{regexp}\"")
	private String paymentToken;
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_DESCRIPTION_NOT_VALID + "] description must match \"{regexp}\"")
	private String description;
	
	@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_CREDITOR_REFERENCE_ID_NOT_VALID + "] creditorReferenceId must match \"{regexp}\"")
	private String creditorReferenceId;
	
	@Pattern(regexp = "^\\d{11}$", message = "[" + ErrorCode.ERROR_FISCAL_CODE_ID_NOT_VALID + "] fiscalCode must match \"{regexp}\"")
	private String fiscalCode;
	
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_COMPANY_ID_NOT_VALID + "] company must match \"{regexp}\"")
	private String company;
	@Pattern(regexp = "^[ -~]{1,140}$", message = "[" + ErrorCode.ERROR_OFFICE_ID_NOT_VALID + "] office must match \"{regexp}\"")
	private String office;
	
	@Pattern(regexp = "^[ -~]{0,1024}$", message = "[" + ErrorCode.ERROR_DEBTOR_NOT_VALID + "] debtor must match \"{regexp}\"")
	private String debtor;

	public String getPaymentToken() {
		return paymentToken;
	}

	public void setPaymentToken(String paymentToken) {
		this.paymentToken = paymentToken;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreditorReferenceId() {
		return creditorReferenceId;
	}

	public void setCreditorReferenceId(String creditorReferenceId) {
		this.creditorReferenceId = creditorReferenceId;
	}

	public String getFiscalCode() {
		return fiscalCode;
	}

	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getDebtor() {
		return debtor;
	}

	public void setDebtor(String debtor) {
		this.debtor = debtor;
	}

	@Override
	public String toString() {
		return "Payments [paymentToken=" + paymentToken + ", description=" + description + ", creditorReferenceId="
				+ creditorReferenceId + ", fiscalCode=" + fiscalCode + ", company=" + company + ", office=" + office
				+ ", debtor=" + debtor + "]";
	}
}
