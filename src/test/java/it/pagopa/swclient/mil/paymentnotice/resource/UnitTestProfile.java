package it.pagopa.swclient.mil.paymentnotice.resource;

import com.google.common.collect.ImmutableList;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        return new HashMap<>();
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return ImmutableList.of(
                new TestResourceEntry(KafkaInMemoryTestResource.class)
        );
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

}
