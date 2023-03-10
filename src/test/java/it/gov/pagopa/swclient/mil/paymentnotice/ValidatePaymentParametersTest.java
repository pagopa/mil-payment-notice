package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ActivatePaymentNoticeRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ActivatePaymentNoticeResource;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.VerifyPaymentNoticeResource;

@QuarkusTest
class ValidatePaymentParametersTest {
	
	final static String QR_CODE				= "qrCode";
	final static String PA_TAX_CODE 		= "paTaxCode";
	final static String NOTICE_NUMBER		= "noticeNumber";
	final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	static Stream<Arguments> configuratorPosFields() {
		return Stream.of(
		
			//MerchantId null
			Arguments.of(removeAndGet(getValidPosHeaders(), "MerchantId"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS ),
		   	//MerchantId invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "MerchantId","11°°°sffd"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_MATCH_REGEXP ));
	}
	
	static Stream<Arguments> configuratorAtmBasicFields() {

		 return Stream.of(
			//RequestId null
			Arguments.of(removeAndGet(getValidAtmHeaders(), "RequestId"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL ),
		   	//RequestId invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "RequestId","dmmmm0d654e6-97da-4848-b568-99fedccb642ba"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP ),

	   		//version exceeded max size
			Arguments.of(putAndGet(getValidAtmHeaders(), "Version","1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokay"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX ),
		   	//version invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "Version",".1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokay"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.VERSION_MUST_MATCH_REGEXP ),

			//AcquirerId null
			Arguments.of(removeAndGet(getValidAtmHeaders(), "AcquirerId"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_NOT_BE_NULL ),
		   	//version invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "AcquirerId","45856bb25"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_MATCH_REGEXP ),
			
			//Channel null
			Arguments.of(removeAndGet(getValidAtmHeaders(), "Channel"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_NOT_BE_NULL ),
		   	//Channel invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "Channel","ATOM"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_MATCH_REGEXP ),
			
			//TerminalId null
			Arguments.of(removeAndGet(getValidAtmHeaders(), "TerminalId"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_NOT_BE_NULL ),
		   	//Channel invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "TerminalId","0aB9wXyZ0029DDDsno9"), getValidParams(), it.gov.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_MATCH_REGEXP )
			
		   );
		}
	
	
	static Stream<Arguments> configuratorAtmFields() {
		
		Stream<Arguments> pathArgs = Stream.of(
						//PaTaxCode invalid regex
						Arguments.of(getValidAtmHeaders(), putAndGet(getValidParams(),"paTaxCode","a"), ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP ),
						
						Arguments.of(getValidAtmHeaders(), putAndGet(getValidParams(),"noticeNumber","a"), ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP )
						
					   );
		
		return Stream.concat(configuratorAtmBasicFields() , pathArgs);

	}

	
	static Stream<Arguments> configuratorPaymentBody() {

		 return Stream.of(
			//outcome null
			Arguments.of(removeAndGet(getValidPaymentBody(), "outcome"),  ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL ),
		   	//outcome invalid regex
			Arguments.of(putAndGet(getValidAtmHeaders(), "outcome","NA"), ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP ),

			//paymentTokens null
			Arguments.of(removeAndGet(getValidPaymentBody(), "paymentTokens"),  ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL ),
			//paymentTokens max element exceeded
			Arguments.of(putAndGet(getValidPaymentBody(), "paymentTokens",getPaymentTokens(6,Optional.empty())), ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST ),
			
			//paymentToken invalid regex
			Arguments.of(putAndGet(getValidPaymentBody(), "paymentTokens",getPaymentTokens(1, Optional.of("°"))), ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP ),
			
			//paymentMethod null
			Arguments.of(removeAndGet(getValidPaymentBody(), "paymentMethod"),  ErrorCode.ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL ),
			//paymentMethod invalid regex
			Arguments.of(putAndGet(getValidPaymentBody(), "paymentMethod","CAS"), ErrorCode.ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP ),
			
			//transactionId null
			Arguments.of(removeAndGet(getValidPaymentBody(), "transactionId"),  ErrorCode.ERROR_TRANSACTION_ID_MUST_NOT_BE_NULL ),
			//transactionId invalid regex
			Arguments.of(putAndGet(getValidPaymentBody(), "transactionId","C°AS"), ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP ),
			
			//totalAmount null
			Arguments.of(removeAndGet(getValidPaymentBody(), "totalAmount"),  ErrorCode.ERROR_TOTAL_AMOUNT_MUST_NOT_BE_NULL ),
			//totalAmount invalid min value 
			Arguments.of(putAndGet(getValidPaymentBody(), "totalAmount",BigInteger.ZERO), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_GREATER_THAN ),
			//totalAmount max element exceeded
			Arguments.of(putAndGet(getValidPaymentBody(), "totalAmount",BigInteger.valueOf(999999999999L)), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_LESS_THAN ),
			
			//fee null
			Arguments.of(removeAndGet(getValidPaymentBody(), "fee"),  ErrorCode.ERROR_FEE_MUST_NOT_BE_NULL ),
			//fee invalid min value 
			Arguments.of(putAndGet(getValidPaymentBody(), "fee",BigInteger.ZERO), ErrorCode.ERROR_FEE_MUST_BE_GREATER_THAN ),
			//fee max element exceeded
			Arguments.of(putAndGet(getValidPaymentBody(), "fee",BigInteger.valueOf(999999999999L)), ErrorCode.ERROR_FEE_MUST_BE_LESS_THAN ),
			
			//paymentMethod null
			Arguments.of(removeAndGet(getValidPaymentBody(), "timestampOp"),  ErrorCode.ERROR_TIMESTAMP_OP_MUST_NOT_BE_NULL ),
			//paymentMethod invalid regex
			Arguments.of(putAndGet(getValidPaymentBody(), "timestampOp","2022-11-12T08:53"), ErrorCode.ERROR_TIMESTAMP_OP_MUST_MATCH_REGEXP )
		   );
		}
	
	static Map<String, String> getValidParams() {
		HashMap<String,String> hash = new HashMap<>();
		hash.put("paTaxCode", "15376371009");
		hash.put("noticeNumber", "100000000000000000");
		return hash;
		
	}
	
	static <K, V> Map<K, V> removeAndGet(Map<K, V> map, K key) { 
		map.remove(key);   
		return map;
	}
	static <K, V> Map<K, V> putAndGet(Map<K, V> map, K key, V value) { 
		map.put(key, value);   
		return map;
	}
	
	static Map<String, String> getValidAtmHeaders() {   
		HashMap<String,String> hash = new HashMap<>();
		hash.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		hash.put("Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay");
		hash.put("AcquirerId", "4585625");
		hash.put("Channel", "ATM");
		hash.put("TerminalId", "0aB9wXyZ");
		return hash;
	}
	
	static Map<String, String> getValidPosHeaders() {   
		HashMap<String,String> hash = new HashMap<>();
		hash.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		hash.put("Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay");
		hash.put("AcquirerId", "4585625");
		hash.put("Channel", "POS");
		hash.put("MerchantId", "28405fHfk73x88D");
		hash.put("TerminalId", "0aB9wXyZ");
		return hash;
	}
	
	static Map<String, Object> getValidPaymentBody() {
		List<String> paymentTokens = new ArrayList<>();
		paymentTokens.add("648fhg36s95jfg7DS");
		HashMap<String,Object> hash = new HashMap<>();
		hash.put("outcome", "OK");
		hash.put("paymentTokens", paymentTokens);
		hash.put("paymentMethod", "PAGOBANCOMAT");
		hash.put("transactionId", "517a4216840E461fB011036A0fd134E1");
		hash.put("totalAmount", BigInteger.valueOf(12345));
		hash.put("fee", BigInteger.valueOf(50));
		hash.put("timestampOp", "2022-11-12T08:53:55");
		return hash;
	}
	
	static List<String> getPaymentTokens(int numberOfElement, Optional<String> str) {
		List<String> paymentTokens = new ArrayList<>();
		for (int i=0; i< numberOfElement; i++) {
			paymentTokens.add(i+"48fhg36s95jfg7S" +str);
		}
		return paymentTokens;
	}
	
	
	/* ***** VerifyPayment ******/
	
	/**
	 * Checks the fields when the API us called by ATM
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
	@MethodSource("configuratorAtmFields")
	void testVerifyPayment_400_checkFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		Response response = given()
				.headers(headers)
				.and()
				.pathParam(PA_TAX_CODE, pathParams.get(PA_TAX_CODE))
				.pathParam(NOTICE_NUMBER, pathParams.get(NOTICE_NUMBER))
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();
		Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}
	
	/**
	 * Checks the fields when the API us called by POS 
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
	@MethodSource("configuratorPosFields")
	void testVerifyPayment_400_checkPosFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		Response response = given()
				.headers(headers)
				.and()
				.pathParam(PA_TAX_CODE, pathParams.get(PA_TAX_CODE))
				.pathParam(NOTICE_NUMBER, pathParams.get(NOTICE_NUMBER))
				.when()
				.get("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();
		Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));
	}
	
	static Stream<Arguments> configuratorForQrcode() {
		return Stream.of(
		
		   	//MerchantId invalid regex
			Arguments.of(getValidAtmHeaders(),putAndGet(getValidParams(),"qrCode", "2134--24--2"), ErrorCode.QRCODE_MUST_MATCH_REGEXP ));
	}
	
	static Map<String, String> getValidQrCodePathParam() {
		HashMap<String,String> hash = new HashMap<>();
		hash.put("qrCode", "PAGOPA|100000000000000000|20000000000|9999");
		return hash;
		
	}
	
	@ParameterizedTest
	@TestHTTPEndpoint(VerifyPaymentNoticeResource.class)
	@MethodSource("configuratorForQrcode")
	void testVerifyByQrCode_400_checkPosFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {

		Response response = given()
				.headers(headers)
				.and()
				.pathParam(QR_CODE, pathParams.get("qrCode"))
				.when()
				.get("/{qrCode}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("dueDate"));
		Assertions.assertNull(response.jsonPath().getJsonObject("note"));
		Assertions.assertNull(response.jsonPath().getJsonObject("description"));
		Assertions.assertNull(response.jsonPath().getJsonObject("company"));
		Assertions.assertNull(response.jsonPath().getJsonObject("office"));

	}
	
	
	/* ***** ActivatePayment ******/
	
	/**
	 * Checks the fields when the API us called by ATM
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
	@MethodSource("configuratorAtmFields")
	void testActivatePayment_400_checkFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(headers)
				.and()
				.pathParam(PA_TAX_CODE, pathParams.get("paTaxCode"))
				.pathParam(NOTICE_NUMBER, pathParams.get(NOTICE_NUMBER))
				.body(activateRequest)
				.when()
				.patch("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	}
	
	/**
	 * Checks the fields when the API us called by POS 
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
	@MethodSource("configuratorPosFields")
	void testActivatePayment_400_checkPosFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(headers)
				.and()
				.pathParam(PA_TAX_CODE, pathParams.get("paTaxCode"))
				.pathParam(NOTICE_NUMBER, pathParams.get(NOTICE_NUMBER))
				.body(activateRequest)
				.when()
				.patch("/{"+ PA_TAX_CODE + "}/{" + NOTICE_NUMBER + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	}

	
	@ParameterizedTest
	@TestHTTPEndpoint(ActivatePaymentNoticeResource.class)
	@MethodSource("configuratorForQrcode")
	void testActivateByQrCode_400_checkPosFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		ActivatePaymentNoticeRequest activateRequest = new ActivatePaymentNoticeRequest();
		activateRequest.setAmount(10099L);
		activateRequest.setIdempotencyKey("77777777777_abcDEF1238");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(headers)
				.and()
				.pathParam(QR_CODE, pathParams.get("qrCode"))
				.body(activateRequest)
				.when()
				.patch("/{"+ QR_CODE + "}")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
		Assertions.assertNull(response.jsonPath().getJsonObject("amount"));
		Assertions.assertNull(response.jsonPath().getJsonObject("paTaxCode"));
		Assertions.assertNull(response.jsonPath().getJsonObject("transfers"));
	}
	
	/* ***** ClosePayment ******/
	/**
	 * Checks the fields when the API us called by ATM
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(PaymentResource.class)
	@MethodSource("configuratorAtmBasicFields")
	void testClosePayment_400_checkFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(headers)
				.and()
				.body(getClosePaymentRequest())
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull( response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-after"));
		Assertions.assertNull(response.getHeader("Max-Retry"));
	}
	
	/**
	 * Checks the fields when the API us called by POS 
	 * @param headers
	 * @param pathParams
	 * @param expectedErrorCode
	 */
	@ParameterizedTest
	@TestHTTPEndpoint(PaymentResource.class)
	@MethodSource("configuratorPosFields")
	void testClosePayment_400_checkPosFields(Map<String, String> headers, Map<String, String> pathParams, String expectedErrorCode) {
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(headers)
				.and()
				.body(getClosePaymentRequest())
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull( response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-after"));
		Assertions.assertNull(response.getHeader("Max-Retry"));
	}
	
	@ParameterizedTest
	@TestHTTPEndpoint(PaymentResource.class)
	@MethodSource("configuratorPaymentBody")
	void testClosePayment_400_checkBodyFields(Map<String, Object> body, String expectedErrorCode) {
		
		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		closePaymentRequest.setOutcome((String)body.get("outcome"));
		closePaymentRequest.setPaymentTokens((List<String>)body.get("paymentTokens"));
		closePaymentRequest.setPaymentMethod((String)body.get("paymentMethod"));
		closePaymentRequest.setTransactionId((String)body.get("transactionId"));
		closePaymentRequest.setTotalAmount((BigInteger)body.get("totalAmount"));
		closePaymentRequest.setFee((BigInteger)body.get("fee"));
		closePaymentRequest.setTimestampOp((String)body.get("timestampOp"));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ")
				.and()
				.body(closePaymentRequest)
				.when()
				.post("/")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(expectedErrorCode));
		Assertions.assertNull( response.jsonPath().getString("outcome"));
		Assertions.assertNull(response.getHeader("Location"));
		Assertions.assertNull(response.getHeader("Retry-after"));
		Assertions.assertNull(response.getHeader("Max-Retry"));
	}
	
	private ClosePaymentRequest getClosePaymentRequest() {
		ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
		closePaymentRequest.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		closePaymentRequest.setPaymentTokens(tokens);
		closePaymentRequest.setPaymentMethod("PAGOBANCOMAT");
		closePaymentRequest.setTransactionId("517a4216840E461fB011036A0fd134E1");
		closePaymentRequest.setTotalAmount(BigInteger.valueOf(234234));
		closePaymentRequest.setFee(BigInteger.valueOf(897));
		closePaymentRequest.setTimestampOp("2022-11-12T08:53:55");
		return closePaymentRequest;
	}
}
