package it.pagopa.swclient.mil.paymentnotice.client;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.client.bean.ADAccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "azure-auth-api")
public interface AzureADRestClient {

    /**
     * @param identity
     * @param scope
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "api-version", value = "${azure-auth-api.version}")
    @WithSpan(kind = SpanKind.CLIENT)
    Uni<ADAccessToken> getAccessToken(
            @HeaderParam("x-identity-header") String identity,
            @QueryParam("resource") String scope);
}