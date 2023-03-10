package it.gov.pagopa.swclient.mil.paymentnotice.util;

import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;

import org.jboss.resteasy.reactive.ClientWebApplicationException;

public class TestUtils {

    private TestUtils() {}

    public static Throwable getException(ExceptionType exceptionType) {
        return switch (exceptionType) {
            case TIMEOUT_EXCEPTION -> new TimeoutException();
            case CLIENT_WEB_APPLICATION_EXCEPTION_400 -> new ClientWebApplicationException(400);
            case CLIENT_WEB_APPLICATION_EXCEPTION_404 -> new ClientWebApplicationException(404);
            case CLIENT_WEB_APPLICATION_EXCEPTION_500 -> new ClientWebApplicationException(500);
            case UNPARSABLE_EXCEPTION -> new ClientWebApplicationException(); // TODO generate correct exception
            case REDIS_TIMEOUT_EXCEPTION -> new SSLHandshakeException("Timeout");
        };
    }

}
