package it.gov.pagopa.swclient.mil.paymentnotice.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import javax.net.ssl.SSLHandshakeException;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.params.provider.Arguments;

import static it.gov.pagopa.swclient.mil.paymentnotice.util.PaymentTestData.*;

public class TestUtils {

    private TestUtils() {}


    public static Stream<Arguments> provideNodeIntegrationErrorCases() {
        return Stream.of(
                Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
                Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
                Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES),
                Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)
        );
    }

    public static Stream<Arguments> provideMilIntegrationErrorCases() {
        return Stream.of(
                Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_400, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
                Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_404, ErrorCode.UNKNOWN_ACQUIRER_ID),
                Arguments.of(ExceptionType.CLIENT_WEB_APPLICATION_EXCEPTION_500, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
                Arguments.of(ExceptionType.TIMEOUT_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES),
                Arguments.of(ExceptionType.UNPARSABLE_EXCEPTION, ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)
        );
    }

    public static Stream<Arguments> provideHeaderValidationErrorCases() {
        return Stream.of(
                // RequestId null
                Arguments.of(removeAndGet(getMilHeaders(false, true), "RequestId"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL ),
                // RequestId invalid regex
                Arguments.of(putAndGet(getMilHeaders(false, true), "RequestId", "dmmmm0d654e6-97da-4848-b568-99fedccb642ba"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP ),
                // Version longer than max size
                Arguments.of(putAndGet(getMilHeaders(false, true), "Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokayokayokayokayokay"), it.pagopa.swclient.mil.ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX ),
                // Version invalid regex
                Arguments.of(putAndGet(getMilHeaders(false, true), "Version", ".1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"), it.pagopa.swclient.mil.ErrorCode.VERSION_MUST_MATCH_REGEXP ),
                // AcquirerId null
                Arguments.of(removeAndGet(getMilHeaders(false, true), "AcquirerId"), it.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_NOT_BE_NULL ),
                // AcquirerId invalid regex
                Arguments.of(putAndGet(getMilHeaders(false, true), "AcquirerId", "45856bb25"), it.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_MATCH_REGEXP ),
                // Channel null
                Arguments.of(removeAndGet(getMilHeaders(false, true), "Channel"), it.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_NOT_BE_NULL ),
                // Channel invalid regex
                Arguments.of(putAndGet(getMilHeaders(false, true), "Channel", "ATOM"), it.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_MATCH_REGEXP ),
                // TerminalId null
                Arguments.of(removeAndGet(getMilHeaders(false, true), "TerminalId"), it.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_NOT_BE_NULL ),
                // TerminalId invalid regex
                Arguments.of(putAndGet(getMilHeaders(false, true), "TerminalId", "0aB9wXyZ0029DDDsno9"), it.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_MATCH_REGEXP ),
                // Merchant invalid regex
                Arguments.of(putAndGet(getMilHeaders(true, true), "MerchantId", "0aB9wXyZ00_29DDDsno9"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_MATCH_REGEXP ),
                // Merchant null if pos
                Arguments.of(removeAndGet(getMilHeaders(true, true), "MerchantId"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS )
        );
    }

    public static Stream<Arguments> provideQrCodeValidationErrorCases() {

        byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode("https://www.test.com".getBytes(StandardCharsets.UTF_8));
        String encodedWrongString = new String(bytes, StandardCharsets.UTF_8);

        bytes = Base64.getUrlEncoder().withoutPadding().encode(QR_CODE.concat("|001").getBytes(StandardCharsets.UTF_8));
        String encodedInvalidQrCode = new String(bytes, StandardCharsets.UTF_8);

        return Stream.of(
                Arguments.of(encodedWrongString, ErrorCode.ENCODED_QRCODE_MUST_MATCH_REGEXP),
                Arguments.of(encodedInvalidQrCode, ErrorCode.QRCODE_FORMAT_IS_NOT_VALID)
        );
    }

    public static Stream<Arguments> providePaTaxCodeNoticeNumberValidationErrorCases() {

        return Stream.of(
                Arguments.of("abcde", "100000000000000000", ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP),
                Arguments.of("20000000000", "abcde", ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP)
        );
    }

    public static Stream<Arguments> provideActivateRequestValidationErrorCases() {

        return Stream.of(
                Arguments.of(setAndGet(getActivatePaymentRequest(), "idempotencyKey", null), ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getActivatePaymentRequest(), "idempotencyKey", "77777777777abcDEF1238"), ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(getActivatePaymentRequest(), "amount", null), ErrorCode.ERROR_AMOUNT_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getActivatePaymentRequest(), "amount", 0L), ErrorCode.ERROR_AMOUNT_MUST_BE_GREATER_THAN),
                Arguments.of(setAndGet(getActivatePaymentRequest(), "amount", 199999999999L), ErrorCode.ERROR_AMOUNT_MUST_BE_LESS_THAN)
        );
    }

    public static Stream<Arguments> providePreCloseRequestValidationErrorCases() {

        return Stream.of(
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "outcome", null), ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "outcome", "OK"), ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "paymentTokens", null), ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "paymentTokens", List.of("100","101","102","103","104","105")), ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "paymentTokens", List.of("123456789012345678901234567890123456")), ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "transactionId", null), ErrorCode.ERROR_TRANSACTION_ID_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "transactionId", "abc"), ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "totalAmount", null), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "totalAmount", 0L), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_GREATER_THAN),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "totalAmount", 199999999999L), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_BE_LESS_THAN),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "totalAmount", 12345L), ErrorCode.ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "fee", null), ErrorCode.ERROR_FEE_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "fee", 0L), ErrorCode.ERROR_FEE_MUST_BE_GREATER_THAN),
                Arguments.of(setAndGet(getPreCloseRequest(true, 1), "fee", 199999999999L), ErrorCode.ERROR_FEE_MUST_BE_LESS_THAN)
        );
    }

    public static Stream<Arguments> provideCloseRequestValidationErrorCases() {

        return Stream.of(
                Arguments.of(setAndGet(getClosePaymentRequest(true), "outcome", null), ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getClosePaymentRequest(true), "outcome", "O"), ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP),
                Arguments.of(setAndGet(getClosePaymentRequest(true), "paymentMethod", null), ErrorCode.ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getClosePaymentRequest(true), "paymentMethod", "INVALID_PAYMENT_METHOD"), ErrorCode.ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(getClosePaymentRequest(true), "paymentTimestamp", null), ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(getClosePaymentRequest(true), "paymentTimestamp", "abcde"), ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_MATCH_REGEXP)
        );
    }

    public static Throwable getException(ExceptionType exceptionType) {
        return switch (exceptionType) {
            case TIMEOUT_EXCEPTION -> new TimeoutException();
            case CLIENT_WEB_APPLICATION_EXCEPTION_400 -> new ClientWebApplicationException(400);
            case CLIENT_WEB_APPLICATION_EXCEPTION_404 -> new ClientWebApplicationException(404);
            case CLIENT_WEB_APPLICATION_EXCEPTION_500 -> new ClientWebApplicationException(500);
            case UNPARSABLE_EXCEPTION -> new ClientWebApplicationException(new JsonParseException(getJsonParser(), ""));
            case REDIS_TIMEOUT_EXCEPTION -> new SSLHandshakeException("Timeout");
            case DB_TIMEOUT_EXCEPTION -> new TimeoutException(); // TODO generate correct exception
            case DB_DUPLICATED_KEY -> {
                WriteError writeError = new WriteError(11000,
                        "'E11000 duplicate key error collection: mil.paymentTransactions index: _id_",
                        new BsonDocument("", new BsonString("")));
                ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
                yield new MongoWriteException(writeError, serverAddress);
            }
        };
    }

    public static ClientWebApplicationException getExceptionWithEntity(int statusCode) {
        return new ClientWebApplicationException(Response.status(statusCode).entity("").build());
    }

    private static JsonParser getJsonParser() {
        JsonParser jsonParser = null;
        try {
            jsonParser = new JsonFactory().createParser("{}");
        } catch (IOException ignored) {
        }
        return jsonParser;
    }

    private static <K, V> Map<K, V> removeAndGet(Map<K, V> map, K key) {
        map.remove(key);
        return map;
    }
    private static <K, V> Map<K, V> putAndGet(Map<K, V> map, K key, V value) {
        map.put(key, value);
        return map;
    }

    private static <T, V> T setAndGet(T object, String propertyName, V propertyValue) {

        try {
            PropertyDescriptor desc = new PropertyDescriptor(propertyName, object.getClass());
            Method setter = desc.getWriteMethod();
            setter.invoke(object, propertyValue);
        }
        catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return object;
    }


}
