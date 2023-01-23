package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class VerifyNoticeResponse {
	private String outcome;
	@JsonInclude(Include.NON_NULL)
	private BigDecimal amount;
	@JsonInclude(Include.NON_NULL)
	private String dueDate;
	@JsonInclude(Include.NON_NULL)
	private String note;
	@JsonInclude(Include.NON_NULL)
	private String description;
	@JsonInclude(Include.NON_NULL)
	private String company;
	@JsonInclude(Include.NON_NULL)
	private String office;
	public String getOutcome() {
		return outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
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
		StringBuilder builder = new StringBuilder();
		builder .append("VerifyNoticeResponse [outcome=").append(outcome)
				.append(" amount=").append(amount)
				.append(" dueDate=").append(dueDate)
				.append(" note=").append(note)
				.append(" description=").append(description)
				.append(" company=").append(company)
				.append(" office=").append(office)
				.append("]");
		return builder.toString();
	}
}
