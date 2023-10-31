package it.pagopa.swclient.mil.paymentnotice.client;

import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.paymentnotice.client.bean.AcquirerConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MilRestService {

    @RestClient
    @Inject
    MilRestResource milRestResource;

    @CacheResult(cacheName = "cache-role")
    public Uni<AcquirerConfiguration> getPspConfiguration(String acquirerId) {
        return milRestResource.getPspConfiguration(acquirerId);
    }
}
