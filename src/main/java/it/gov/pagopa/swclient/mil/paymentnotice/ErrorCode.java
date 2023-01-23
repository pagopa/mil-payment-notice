package it.gov.pagopa.swclient.mil.paymentnotice;


public final class ErrorCode {
	public static final String MODULE_ID = "008";

	public static final String QRCODE_MUST_MATCH_REGEXP 									= MODULE_ID + "000001";
	public static final String PAX_TAX_CODE_MUST_MATCH_REGEXP 								= MODULE_ID + "000002";
	public static final String NOTICE_NUMBER_MUST_MATCH_REGEXP 								= MODULE_ID + "000003";
	public static final String ERROR_NODE 													= MODULE_ID + "000004";
	public static final String ERROR_READING_PSP_INFO										= MODULE_ID + "000005";
	public static final String ERROR_IDEMPOTENCY_KEY										= MODULE_ID + "000006";
	public static final String ERROR_INVALID_AMOUNT											= MODULE_ID + "000007";
	public static final String ERROR_OUTCOME												= MODULE_ID + "000008";
	public static final String ERROR_PAYMENT_TOKEN_NOT_VALID								= MODULE_ID + "000009";
	public static final String ERROR_PAYMEN_TOKEN_LIST_EXCEEDED								= MODULE_ID + "000010";
	public static final String ERROR_PAYMENT_METHOD											= MODULE_ID + "000011";
	public static final String ERROR_CALLING_NODE_SERVICE									= MODULE_ID + "000012";
	public static final String ERROR_TRANSACTION_ID											= MODULE_ID + "000013";
	public static final String ERROR_INVALID_FEE											= MODULE_ID + "000014";
	public static final String ERROR_TIMESTAMPOP											= MODULE_ID + "000015";
	public static final String ERROR_DESCRIPTION_NOT_VALID									= MODULE_ID + "000016";
	public static final String ERROR_CREDITOR_REFERENCE_ID_NOT_VALID						= MODULE_ID + "000017";
	public static final String ERROR_DEBTOR_NOT_VALID										= MODULE_ID + "000018";
	public static final String ERROR_FISCAL_CODE_ID_NOT_VALID								= MODULE_ID + "000019";
	public static final String ERROR_COMPANY_ID_NOT_VALID									= MODULE_ID + "000020";
	public static final String ERROR_OFFICE_ID_NOT_VALID									= MODULE_ID + "000021";
	public static final String REDIS_ERROR_WHILE_RETRIEVING_PAYMENT_RESULT					= MODULE_ID + "000022";
	public static final String REDIS_ERROR_PAYMENT_RESULT_WITH_TRANSACTION_ID_NOT_FOUND		= MODULE_ID + "000023";
	public static final String REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT						= MODULE_ID + "000024";
	
	
	
	
	
	
	private ErrorCode() {
	}
}
