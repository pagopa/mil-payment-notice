package it.gov.pagopa.swclient.mil.paymentnotice.bean;

public class Transfer {
	private String paTaxCode;
	private String category;
	public String getPaTaxCode() {
		return paTaxCode;
	}
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public String toString() {
		return "Transfer [paTaxCode=" + paTaxCode + ", category=" + category + "]";
	}
}
