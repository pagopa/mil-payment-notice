package it.gov.pagopa.swclient.mil.paymentnotice.dao;

public class PspConfiguration {

	private String pspId;
	private String pspBroker;
	private String pspPassword;


	public String getPspId() {
		return pspId;
	}

	public void setPspId(String pspId) {
		this.pspId = pspId;
	}

	public String getPspBroker() {
		return pspBroker;
	}

	public void setPspBroker(String pspBroker) {
		this.pspBroker = pspBroker;
	}

	public String getPspPassword() {
		return pspPassword;
	}

	public void setPspPassword(String pspPassword) {
		this.pspPassword = pspPassword;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PspConf{");
		sb.append("pspId='").append(pspId).append('\'');
		sb.append(", pspBroker='").append(pspBroker).append('\'');
		sb.append(", pspPassword='").append(pspPassword).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
