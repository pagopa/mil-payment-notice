package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ActivatePaymentV2Rsponse {
	private String outcome;
	@JsonInclude(Include.NON_NULL)
	private BigDecimal amount;
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;
	@JsonInclude(Include.NON_NULL)
	private String paymentToken;
	@JsonInclude(Include.NON_NULL)
	private List<Transfer> transfers;
	
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
	public String getPaTaxCode() {
		return paTaxCode;
	}
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}
	public String getPaymentToken() {
		return paymentToken;
	}
	public void setPaymentToken(String paymentToken) {
		this.paymentToken = paymentToken;
	}
	public List<Transfer> getTransfers() {
		return transfers;
	}
	public void setTransfers(List<Transfer> transfers) {
		this.transfers = transfers;
	}
	@Override
	public String toString() {
		return "ActivatePaymentV2Rsponse [outcome=" + outcome + ", amount=" + amount + ", paTaxCode=" + paTaxCode
				+ ", paymentToken=" + paymentToken + ", transfers=" + transfers + "]";
	}
	
}
