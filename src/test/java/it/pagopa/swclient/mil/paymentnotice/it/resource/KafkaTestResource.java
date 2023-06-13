package it.pagopa.swclient.mil.paymentnotice.it.resource;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTestResource.class);

    private static final String KAFKA_NETWORK_ALIAS = "kafka";

    private GenericContainer<?> kafkaContainer;

    private DevServicesContext devServicesContext;

    @Override
    public void setIntegrationTestContext(DevServicesContext devServicesContext){
        this.devServicesContext = devServicesContext;
    }

    @Override
    public Map<String, String> start() {

        try {
            logger.info("Starting Kafka container...");

            Map<String, String> environmentVariables = new HashMap<>();

            environmentVariables.put("BITNAMI_DEBUG","true");
            environmentVariables.put("ALLOW_PLAINTEXT_LISTENER", "yes");
            environmentVariables.put("KAFKA_ENABLE_KRAFT", "yes");
            environmentVariables.put("KAFKA_CFG_PROCESS_ROLES", "broker,controller");
            environmentVariables.put("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER");
            environmentVariables.put("KAFKA_BROKER_ID", "1");
            environmentVariables.put("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS","1@127.0.0.1:9094");
            environmentVariables.put("KAFKA_CFG_NODE_ID", "1");
            environmentVariables.put("KAFKA_CFG_LISTENERS", "INTERNAL://:9093,CLIENT://:9092,CONTROLLER://:9094,EXTERNAL://:29092");
            environmentVariables.put("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "INTERNAL:SASL_PLAINTEXT,CLIENT:SASL_PLAINTEXT,CONTROLLER:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT");
            environmentVariables.put("KAFKA_CFG_ADVERTISED_LISTENERS", "INTERNAL://kafka:9093,CLIENT://kafka:9092,EXTERNAL://localhost:29092");
            environmentVariables.put("KAFKA_CFG_INTER_BROKER_LISTENER_NAME", "INTERNAL");
            environmentVariables.put("KAFKA_CFG_SASL_ENABLED_MECHANISMS", "PLAIN");
            environmentVariables.put("KAFKA_CFG_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "PLAIN");
            environmentVariables.put("KAFKA_CFG_SASL_MECHANISM_CONTROLLER_PROTOCOL", "PLAIN");
            environmentVariables.put("KAFKA_CFG_NUM_PARTITIONS", "1");

            kafkaContainer = new FixedHostPortGenericContainer<>("bitnami/kafka:3.4.0")
                    .withFixedExposedPort(29092,29092)
                    .withNetwork(getNetwork())
                    .withNetworkAliases(KAFKA_NETWORK_ALIAS)
                    .withEnv(environmentVariables)
                    .waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1));

            //kafkaContainer.withLogConsumer(new Slf4jLogConsumer(logger, true));

            kafkaContainer.start();

            logger.info("kafkaContainer.isRunning(): {}", kafkaContainer.isRunning());

            String topic = RandomStringUtils.random(8, 0, 0, true, true, null, new SecureRandom()).toLowerCase();
            try {
                String config = "sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"bitnami\";\n";
                config = config + "security.protocol=SASL_PLAINTEXT\n";
                config = config + "sasl.mechanism=PLAIN";
                kafkaContainer.copyFileToContainer(Transferable.of(config, 511), "tmp/config.properties");

                logger.info(kafkaContainer.execInContainer("/opt/bitnami/kafka/bin/kafka-topics.sh",
                        "--create",
                        "--bootstrap-server", "127.0.0.1:9092",
                        "--replication-factor", "1",
                        "--partitions", "1",
                        "--topic", topic,
                        "--command-config", "tmp/config.properties").toString());
            }
            catch (UnsupportedOperationException | IOException | InterruptedException e) {
                logger.error("Could not create topic", e);
            }

            devServicesContext.devServicesProperties().put("test.kafka.bootstrap-server", "localhost:29092");
            devServicesContext.devServicesProperties().put("test.kafka.security-protocol", "SASL_PLAINTEXT");
            devServicesContext.devServicesProperties().put("test.kafka.sasl-mechanism", "PLAIN");
            devServicesContext.devServicesProperties().put("test.kafka.sasl-jaas-config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"bitnami\";");

            devServicesContext.devServicesProperties().put("test.kafka.topic", topic);

            // Pass the configuration to the application under test
            return ImmutableMap.of(
                    "kafka-bootstrap-server", KAFKA_NETWORK_ALIAS + ":" + 9093,
                    "kafka-security-protocol", "SASL_PLAINTEXT",
                    "kafka-sasl-mechanism", "PLAIN",
                    "kafka-sasl-jaas-config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"bitnami\";",
                    "kafka-topic", topic
            );
        }
		catch (Exception e) {
            logger.error("Error while starting kafka", e);
            throw e;
        }
    }

    // create a "fake" network using the same id as the one that will be used by Quarkus
    // using the network is the only way to make the withNetworkAliases work
    private Network getNetwork() {
        logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
        return new Network() {
            @Override
            public String getId() {
                return devServicesContext.containerNetworkId().orElse(null);
            }

            @Override
            public void close() {
            }

            @Override
            public Statement apply(Statement statement, Description description) {
                return null;
            }
        };
    }

    @Override
    public void stop() {
        if (null != kafkaContainer) {
            logger.info("Stopping kafka container...");
            kafkaContainer.stop();
            logger.info("Kafka container stopped");
        }

    }

}
