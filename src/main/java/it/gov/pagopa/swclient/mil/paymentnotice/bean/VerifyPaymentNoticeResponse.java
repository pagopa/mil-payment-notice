package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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


	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
		sb.append('}');
		return sb.toString();
	}
}
