package it.pagopa.swclient.mil.paymentnotice.util;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.common.DevServicesContext;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class KafkaUtils {

    static final Logger logger = LoggerFactory.getLogger(KafkaUtils.class);

    public static <T> KafkaConsumer<String, T> getKafkaConsumer(DevServicesContext devServicesContext, Class<T> clazz) {
        // initialize kafka consumer
        Properties kafkaConfig = new Properties();

        Map<String, String> testProperties = devServicesContext.devServicesProperties();
        kafkaConfig.put("bootstrap.servers", testProperties.get("test.kafka.bootstrap-server"));
        kafkaConfig.put("security.protocol", testProperties.get("test.kafka.security-protocol"));
        kafkaConfig.put("sasl.mechanism", testProperties.get("test.kafka.sasl-mechanism"));
        kafkaConfig.put("sasl.jaas.config", testProperties.get("test.kafka.sasl-jaas-config"));

        kafkaConfig.put("group.id", "it-consumer");
        kafkaConfig.put("client.id", "it-consumer");
        kafkaConfig.put("enable.auto.commit", "true");
        kafkaConfig.put("auto.offset.reset", "earliest");
        kafkaConfig.put("auto.commit.interval.ms", "1000");

        kafkaConfig.put("linger.ms", 1);

        KafkaConsumer<String, T> kafkaConsumer = new KafkaConsumer<>(kafkaConfig, new StringDeserializer(), new ObjectMapperDeserializer<>(clazz));

        String topic = testProperties.get("test.kafka.topic");
        logger.info("topic: {}", kafkaConsumer.listTopics().get(topic));

        kafkaConsumer.subscribe(List.of(topic), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                logger.info("revoked partition {}", partitions);
                for (TopicPartition topicPartition : partitions) {
                    logger.debug("topic [{}] partition [{}] beginning offset [{}] end offset [{}] committed [{}] position [{}] current lag [{}]",
                            topicPartition.topic(),
                            topicPartition.partition(),
                            kafkaConsumer.beginningOffsets(List.of(topicPartition)),
                            kafkaConsumer.endOffsets(List.of(topicPartition)),
                            kafkaConsumer.committed(Set.of(topicPartition), Duration.of(10, ChronoUnit.SECONDS)),
                            kafkaConsumer.position(topicPartition, Duration.of(10, ChronoUnit.SECONDS)),
                            kafkaConsumer.currentLag(topicPartition));
                }
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                logger.info("assigned partition {}", partitions);
                for (TopicPartition topicPartition : partitions) {
                    logger.debug("topic [{}] partition [{}] beginning offset [{}] end offset [{}] committed [{}] position [{}] current lag [{}]",
                            topicPartition.topic(),
                            topicPartition.partition(),
                            kafkaConsumer.beginningOffsets(List.of(topicPartition)),
                            kafkaConsumer.endOffsets(List.of(topicPartition)),
                            kafkaConsumer.committed(Set.of(topicPartition), Duration.of(10, ChronoUnit.SECONDS)),
                            kafkaConsumer.position(topicPartition, Duration.of(10, ChronoUnit.SECONDS)),
                            kafkaConsumer.currentLag(topicPartition));

                    //paymentTransactionConsumer.seek(topicPartition, 0);
                }
            }
        });

        return kafkaConsumer;
    }
}
