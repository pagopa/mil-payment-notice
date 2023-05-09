package it.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Response of the verifyPaymentNotice API
 */
@RegisterForReflection
public class VerifyPaymentNoticeResponse {

	@NotNull
	@Pattern(regexp = "^(?:OK|NOTICE_GLITCH|WRONG_NOTICE_DATA|CREDITOR_PROBLEMS|PAYMENT_ALREADY_IN_PROGRESS|EXPIRED_NOTICE|REVOKED_NOTICE|NOTICE_ALREADY_PAID|UNEXPECTED_ERROR)$")
	private String outcome;

	@Min(1)
	@Max(99999999999L)
	@JsonInclude(Include.NON_NULL)
	private BigInteger amount;

	// string($date) 2022-11-30
	@Size(min =10, max = 10)
	@JsonInclude(Include.NON_NULL)
	private String dueDate;

	@Pattern(regexp = "^[ -~]{1,210}$")
	@JsonInclude(Include.NON_NULL)
	private String note;

	@Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String description;

	@Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String company;

	@Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String office;

	@Pattern(regexp = "^\\d{11}$")
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;

	@Pattern(regexp = "^\\d{18}$")
	@JsonInclude(Include.NON_NULL)
	private String noticeNumber;


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
	 * Gets dueDate
	 * @return value of dueDate
	 */
	public String getDueDate() {
		return dueDate;
	}

	/**
	 * Sets dueDate
	 * @param dueDate value of dueDate
	 */
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Gets note
	 * @return value of note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Sets note
	 * @param note value of note
	 */
	public void setNote(String note) {
		this.note = note;
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
	 * Gets noticeNumber
	 * @return value of noticeNumber
	 */
	public String getNoticeNumber() {
		return noticeNumber;
	}

	/**
	 * Sets noticeNumber
	 * @param noticeNumber value of noticeNumber
	 */
	public void setNoticeNumber(String noticeNumber) {
		this.noticeNumber = noticeNumber;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("VerifyPaymentNoticeResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", amount=").append(amount);
		sb.append(", dueDate='").append(dueDate).append('\'');
		sb.append(", note='").append(note).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", company='").append(company).append('\'');
		sb.append(", office='").append(office).append('\'');
		sb.append(", paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", noticeNumber='").append(noticeNumber).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
