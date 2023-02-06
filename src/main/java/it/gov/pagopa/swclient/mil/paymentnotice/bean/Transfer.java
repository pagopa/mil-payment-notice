package it.gov.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RegisterForReflection
public class Transfer {

	@NotNull
	@Pattern(regexp = "^\\d{11}$")
	private String paTaxCode;

	@NotNull
	@Pattern(regexp = "^[ -~]{0,1024}$")
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
		final StringBuilder sb = new StringBuilder("Transfer{");
		sb.append("paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", category='").append(category).append('\'');
		sb.append('}');
		return sb.toString();
	}

}
