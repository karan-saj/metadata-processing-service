package com.lily.metadataProcessingService.config;

import com.lily.metadataProcessingService.model.MetadataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets up our Kafka configuration.
 *Config for both for inbound and outbound messages.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    
    private final ApplicationProperties applicationProperties;
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates a Kafka listener container factory for processing single records.
     * This factory is used for topics that don't require batch processing.
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MetadataRequest> 
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MetadataRequest> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(false);
        return factory;
    }

    /**
     * Creates a Kafka listener container factory for batch processing.
     * This factory is configured to process multiple records at once for improved throughput.
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MetadataRequest> 
            batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MetadataRequest> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(batchConsumerFactory());
        factory.setBatchListener(true);
        return factory;
    }

    /**
     * Creates a consumer factory for single record processing.
     * Configures basic consumer properties including deserializers and group ID.
     *
     * @return
     */
    @Bean
    public ConsumerFactory<String, MetadataRequest> consumerFactory() {
        log.info("Setting up Kafka consumer factory");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "metadata-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
            new JsonDeserializer<>(MetadataRequest.class));
    }

    /**
     * Creates a consumer factory specifically for batch processing.
     * Configures consumer properties with batch-specific settings like MAX_POLL_RECORDS.
     *
     * @return
     */
    @Bean
    public ConsumerFactory<String, MetadataRequest> batchConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "metadata-batch-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
            new JsonDeserializer<>(MetadataRequest.class));
    }

    /**
     * Creates a producer factory for sending messages to Kafka topics.
     * Configures serializers and basic producer properties.
     *
     * @return
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        log.info("Setting up Kafka producer factory");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate using the configured producer factory.
     * This template is used for sending messages to Kafka topics.
     *
     * @return 
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        log.info("Creating Kafka template");
        return new KafkaTemplate<>(producerFactory());
    }
}
