package it.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Transfer essential data
 */
@RegisterForReflection
public class Transfer {

	/**
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^\\d{11}$")
	private String paTaxCode;

	/**
	 * Transfer category
	 */
	@NotNull
	@Pattern(regexp = "^[ -~]{0,1024}$")
	private String category;


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
	 * Gets category
	 * @return value of category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets category
	 * @param category value of category
	 */
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
