package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;

public class PaymentRequestBody {

	@Pattern(regexp = "OK|KO", message = "[" + ErrorCode.ERROR_OUTCOME + "] outcome must match \"{regexp}\"")
	private String outcome;
	
	@Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMEN_TOKEN_LIST_EXCEEDED + "] the max size of paymentTokens is 5")
	private List<String> paymentTokens;
	
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|BANK_ACCOUNT|CASH", message = "[" + ErrorCode.ERROR_PAYMENT_METHOD + "] paymentMethod must match one of the values \"{regexp}\"")
	private String paymentMethod;
	
	@Pattern(regexp = "^[a-zA-Z0-9]{1,255}$", message = "[" + ErrorCode.ERROR_TRANSACTION_ID + "] transactionId must match \"{regexp}\"")
	private String transactionId;
	
	private long totalAmount;
	
	private long fee;
	
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d", message = "[" + ErrorCode.ERROR_TIMESTAMPOP + "] timestampOp must match \"{regexp}\"")
	private String timestampOp;
	
	@AssertTrue(message = "[" + ErrorCode.ERROR_INVALID_AMOUNT + "] totalAmount passed in the body is not valid")
    private boolean isTotalAmountValid() {
        return totalAmount >= 1 && totalAmount <= 99999999999L;
	}
	@AssertTrue(message = "[" + ErrorCode.ERROR_INVALID_FEE + "] fee passed in the body is not valid")
    private boolean isFeeValid() {
        return fee >= 1 && fee <= 99999999999L;
	}
	@AssertTrue(message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_NOT_VALID + "] paymentToken passed in the body is not valid")
	private boolean isValidPaimentToken() {
		if (paymentTokens != null) {
			for (String token : paymentTokens) {
				if (!token.matches("^[ -~]{1,35}$")) {
					return false;
				}
			}
		}
		return true;
	}
	public String getOutcome() {
		return outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;
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
	public long getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(long amount) {
		this.totalAmount = amount;
	}
	public long getFee() {
		return fee;
	}
	public void setFee(long fee) {
		this.fee = fee;
	}
	public String getTimestampOp() {
		return timestampOp;
	}
	public void setTimestampOp(String timestampOp) {
		this.timestampOp = timestampOp;
	}
	public List<String> getPaymentTokens() {
		return paymentTokens;
	}
	public void setPaymentTokens(List<String> paymentTokens) {
		this.paymentTokens = paymentTokens;
	}
	@Override
	public String toString() {
		return "PaymentRequestBody [outcome=" + outcome + ", paymentTokens=" + paymentTokens + ", paymentMethod="
				+ paymentMethod + ", transactionId=" + transactionId + ", totalAmount=" + totalAmount + ", fee=" + fee
				+ ", timestampOp=" + timestampOp + "]";
	}
}
