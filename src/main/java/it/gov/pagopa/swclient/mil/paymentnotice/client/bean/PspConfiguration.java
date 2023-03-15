package it.gov.pagopa.swclient.mil.paymentnotice.client.bean;

/**
 * Configuration of a PSP used when connecting to the node
 */
public class PspConfiguration {

	/**
	 * Identifier of the PSP, assigned by PagoPA
	 */
	private String psp;

	/**
	 * Identifier of the broker, assigned by PagoPA
	 */
	private String broker;

	/**
	 * Identifier of the channel used for the payment transaction.
	 * Is assigned by PagoPA and is unique for the psp
	 */
	private String channel;

	/**
	 * Channel's password, assigned by PagoPA
	 */
	private String password;


	/**
	 * Gets psp
	 * @return value of psp
	 */
	public String getPsp() {
		return psp;
	}

	/**
	 * Sets psp
	 * @param psp value of psp
	 */
	public void setPsp(String psp) {
		this.psp = psp;
	}

	/**
	 * Gets broker
	 * @return value of broker
	 */
	public String getBroker() {
		return broker;
	}

	/**
	 * Sets broker
	 * @param broker value of broker
	 */
	public void setBroker(String broker) {
		this.broker = broker;
	}

	/**
	 * Gets channel
	 * @return value of channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Sets channel
	 * @param channel value of channel
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * Gets password
	 * @return value of password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets password
	 * @param password value of password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PspConfiguration{");
		sb.append("psp='").append(psp).append('\'');
		sb.append(", broker='").append(broker).append('\'');
		sb.append(", channel='").append(channel).append('\'');
		sb.append(", password='").append(password != null ? "***" : null).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
