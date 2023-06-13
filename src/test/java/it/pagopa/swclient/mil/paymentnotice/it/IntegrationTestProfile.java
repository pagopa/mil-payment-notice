package it.pagopa.swclient.mil.paymentnotice.it;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.swclient.mil.paymentnotice.it.resource.KafkaTestResource;
import it.pagopa.swclient.mil.paymentnotice.it.resource.MongoTestResource;
import it.pagopa.swclient.mil.paymentnotice.it.resource.RedisTestResource;
import it.pagopa.swclient.mil.paymentnotice.it.resource.WiremockTestResource;

public class IntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        Map<String, String> configOverrides = new HashMap<>();

        configOverrides.put("paymentnotice.quarkus-log-level", "DEBUG");
        configOverrides.put("paymentnotice.app-log-level", "DEBUG");
        configOverrides.put("paymentnotice.rest-client.connect-timeout", "3000");
        configOverrides.put("paymentnotice.rest-client.read-timeout", "3000");
        configOverrides.put("paymentnotice.closepayment.max-retry", "2");
        configOverrides.put("paymentnotice.closepayment.retry-after", "35");
        configOverrides.put("paymentnotice.activatepayment.expiration-time", "25000");
        configOverrides.put("node.soap-client.connect-timeout", "3000");
        configOverrides.put("node.soap-client.read-timeout", "3000");
        configOverrides.put("node.soap-client.apim-subscription-key", "");
        configOverrides.put("node-rest-client-subscription-key", "abc");
        configOverrides.put("mil-rest-client-subscription-key", "abc");
        configOverrides.put("mil-acquirer-conf-version", "1.0.0");
        configOverrides.put("paymentnotice.closepayment.location.base-url", "https://mil-d-apim.azure-api.net/mil-payment-notice");

        return configOverrides;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return ImmutableList.of(
                new TestResourceEntry(WiremockTestResource.class),
                new TestResourceEntry(RedisTestResource.class),
                new TestResourceEntry(MongoTestResource.class),
                new TestResourceEntry(KafkaTestResource.class)
        );
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

}
