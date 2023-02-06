package it.gov.pagopa.swclient.mil.paymentnotice.it;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTestProfile;
import it.gov.pagopa.swclient.mil.paymentnotice.it.resource.MongoTestResource;
import it.gov.pagopa.swclient.mil.paymentnotice.it.resource.RedisTestResource;
import it.gov.pagopa.swclient.mil.paymentnotice.it.resource.WiremockTestResource;

import java.util.List;
import java.util.Map;

public class IntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return ImmutableMap.of(
                "paymentnotice.quarkus-log-level", "DEBUG",
                "paymentnotice.app-log-level", "DEBUG",
                "paymentnotice.rest-client.connect-timeout", "3000",
                "paymentnotice.rest-client.read-timeout", "3000",
                "paymentnotice.closepayment.max-retry", "2",
                "paymentnotice.closepayment.retry-after", "35",
                "paymentnotice.activatepayment.expiration-time", "25000",
                "node.soap-client.connect-timeout", "3000",
                "node.soap-client.read-timeout", "3000",
                "node.soap-client.apim-subscription-key", ""
        );
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return ImmutableList.of(
               // new TestResourceEntry(EnvironmentTestResource.class),
                new TestResourceEntry(WiremockTestResource.class),
                new TestResourceEntry(RedisTestResource.class),
                new TestResourceEntry(MongoTestResource.class)
        );
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

}
