package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RegisterForReflection
public class ActivatePaymentNoticeResponse {

	@NotNull
	@Pattern(regexp = "^(?:OK|NOTICE_GLITCH|WRONG_NOTICE_DATA|CREDITOR_PROBLEMS|PAYMENT_ALREADY_IN_PROGRESS|EXPIRED_NOTICE|REVOKED_NOTICE|NOTICE_ALREADY_PAID|UNEXPECTED_ERROR)$")
	private String outcome;

	@Min(1)
	@Max(99999999999L)
	@JsonInclude(Include.NON_NULL)
	private BigInteger amount;

	@Pattern(regexp = "^\\d{11}$")
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;

	@Pattern(regexp = "^[ -~]{1,35}$")
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

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
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
		final StringBuilder sb = new StringBuilder("ActivatePaymentResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", amount=").append(amount);
		sb.append(", paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", paymentToken='").append(paymentToken).append('\'');
		sb.append(", transfers=").append(transfers);
		sb.append('}');
		return sb.toString();
	}
}
