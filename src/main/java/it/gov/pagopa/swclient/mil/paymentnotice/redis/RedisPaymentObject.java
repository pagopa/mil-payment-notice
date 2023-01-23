package it.gov.pagopa.swclient.mil.paymentnotice.redis;

import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentBody;

public class RedisPaymentObject {
	private String key;
	private PaymentBody paymentBody;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public PaymentBody getPaymentBody() {
		return paymentBody;
	}
	public void setPaymentBody(PaymentBody paymentBody) {
		this.paymentBody = paymentBody;
	}
	public RedisPaymentObject(String key, PaymentBody paymentBody) {
		this.key 			= key;
		this.paymentBody	= paymentBody;
	}
	public RedisPaymentObject() {
	}
}
