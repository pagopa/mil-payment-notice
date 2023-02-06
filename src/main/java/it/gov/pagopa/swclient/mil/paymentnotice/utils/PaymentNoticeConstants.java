package it.gov.pagopa.swclient.mil.paymentnotice.utils;

public class PaymentNoticeConstants {

	public static final String QRCODE_REGEX 		= "^[ -~]{6}\\|[ -~]{3}\\|\\d{18}\\|\\d{11}\\|\\d{2,11}$";

	public static final String PA_TAX_CODE_REGEX 	= "^\\d{11}$";

	public static final String NOTICE_NUMBER_REGEX	= "^\\d{18}$";

	private PaymentNoticeConstants() {}
}
