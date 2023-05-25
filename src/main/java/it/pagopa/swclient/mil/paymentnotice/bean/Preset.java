/**
 * 
 */
package it.pagopa.swclient.mil.paymentnotice.bean;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class Preset implements Serializable{

	/**
	 *Preset.java
	 */
	private static final long serialVersionUID = -8846393398844116002L;

	/*
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^[0-9]{11}$")
	private String paTaxCode;
	
	/*
	 * Subscriber ID
	 */
	@NotNull
	@Pattern(regexp = "^[0-9a-z]{6}$")
	private String subscriberId;
	
	/*
	 * Preset Id
	 */
	@NotNull
	@Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")
	private String presetId;
	
	/**
	 * @return the paTaxCode
	 */
	public String getPaTaxCode() {
		return paTaxCode;
	}
	
	/**
	 * @param paTaxCode the paTaxCode to set
	 */
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}
	
	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}
	
	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	
	/**
	 * @return the presetId
	 */
	public String getPresetId() {
		return presetId;
	}
	
	/**
	 * @param presetId the presetId to set
	 */
	public void setPresetId(String presetId) {
		this.presetId = presetId;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Preset [paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", presetId=");
		builder.append(presetId);
		builder.append("]");
		return builder.toString();
	}
}
