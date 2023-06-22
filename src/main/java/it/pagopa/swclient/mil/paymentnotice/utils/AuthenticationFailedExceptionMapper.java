package it.pagopa.swclient.mil.paymentnotice.utils;

import io.quarkus.logging.Log;
import io.quarkus.security.AuthenticationFailedException;
import it.pagopa.swclient.mil.paymentnotice.ErrorCode;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    ContainerRequestContext context;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        Log.errorf("[%s] Authentication failed - %s", ErrorCode.AUTHENTICATION_ERROR, context.getHeaders());
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
