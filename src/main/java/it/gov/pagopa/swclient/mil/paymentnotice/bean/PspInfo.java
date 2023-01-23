package it.gov.pagopa.swclient.mil.paymentnotice.bean;

public class PspInfo {
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
		return "PspInfo [pspId=" + pspId + ", pspBroker=" + pspBroker + ", pspPassword=" + pspPassword + "]";
	}
}
