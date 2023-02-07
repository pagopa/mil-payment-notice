package it.gov.pagopa.swclient.mil.paymentnotice.dao;

/**
 * Configuration of a PSP used when connecting to the node
 */
public class PspConfiguration {

	/**
	 * Identifier of the PSP, assigned by PagoPA
	 */
	private String pspId;

	/**
	 * Identifier of the broker, assigned by PagoPA
	 */
	private String pspBroker;

	/**
	 * Identifier of the channel used for the payment transaction.
	 * Is assigned by PagoPA and is unique for the psp
	 */
	private String idChannel;

	/**
	 * Channel's password, , assigned by PagoPA
	 */
	private String pspPassword;


	/**
	 * Gets pspId
	 * @return value of pspId
	 */
	public String getPspId() {
		return pspId;
	}

	/**
	 * Sets pspId
	 * @param pspId value of pspId
	 */
	public void setPspId(String pspId) {
		this.pspId = pspId;
	}

	/**
	 * Gets pspBroker
	 * @return value of pspBroker
	 */
	public String getPspBroker() {
		return pspBroker;
	}

	/**
	 * Sets pspBroker
	 * @param pspBroker value of pspBroker
	 */
	public void setPspBroker(String pspBroker) {
		this.pspBroker = pspBroker;
	}

	/**
	 * Gets idChannel
	 * @return value of idChannel
	 */
	public String getIdChannel() {
		return idChannel;
	}

	/**
	 * Sets idChannel
	 * @param idChannel value of idChannel
	 */
	public void setIdChannel(String idChannel) {
		this.idChannel = idChannel;
	}

	/**
	 * Gets pspPassword
	 * @return value of pspPassword
	 */
	public String getPspPassword() {
		return pspPassword;
	}

	/**
	 * Sets pspPassword
	 * @param pspPassword value of pspPassword
	 */
	public void setPspPassword(String pspPassword) {
		this.pspPassword = pspPassword;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PspConfiguration{");
		sb.append("pspId='").append(pspId).append('\'');
		sb.append(", pspBroker='").append(pspBroker).append('\'');
		sb.append(", idChannel='").append(idChannel).append('\'');
		sb.append(", pspPassword='").append(pspPassword).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
