package it.pagopa.swclient.mil.paymentnotice.client;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.client.bean.ADAccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
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
    @ClientHeaderParam(name = "x-ms-version", value = "${azure-storage-api.version}")
    Uni<ADAccessToken> getAccessToken(
            @HeaderParam("x-identity-header") String identity,
            @QueryParam("resource") String scope);
}