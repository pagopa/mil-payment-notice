package it.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KafkaInMemoryTestResource implements QuarkusTestResourceLifecycleManager {

    static final Logger logger = LoggerFactory.getLogger(KafkaInMemoryTestResource.class);

    @Override
    public Map<String, String> start() {
        logger.info("Starting in memory Kafka connector");
        Map<String, String> props1 = InMemoryConnector.switchOutgoingChannelsToInMemory("presets");
        return new HashMap<>(props1);
    }

    @Override
    public void stop() {
        logger.info("Stopping in memory Kafka connector");
        InMemoryConnector.clear();  
    }
}
