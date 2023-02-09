package it.gov.pagopa.swclient.mil.paymentnotice.utils;

/**
 * Class containing common regex used by the REST endpoint to validate path params
 */
public class PaymentNoticeConstants {

	/**
	 * The regex to be used to validate the QR code
	 */
	public static final String QRCODE_REGEX 		= "^[ -~]{6}\\|[ -~]{3}\\|\\d{18}\\|\\d{11}\\|\\d{2,11}$";

	/**
	 * The regex to be used to validate the PA tax code
	 */
	public static final String PA_TAX_CODE_REGEX 	= "^\\d{11}$";

	/**
	 * The regex to be used to validate the notice number
	 */
	public static final String NOTICE_NUMBER_REGEX	= "^\\d{18}$";

	private PaymentNoticeConstants() {}
}
