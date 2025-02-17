package com.lily.metadataProcessingService.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lily.metadataProcessingService.dto.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lily.metadataProcessingService.util.CommonUtil.convertToStringMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.kafka.topic.outbound}")
    private String outBoundTopic;

    public void publishMetadata(ProducerRecord<String, String> outBoundMessage) {
        kafkaTemplate.send(outBoundTopic, String.valueOf(outBoundMessage));
        log.info("Published metadata: {}", outBoundMessage);
    }

    public void sendOutboundKafkaMessage(Metadata metadata, String cdc) {
        // generate outbound message for the current topic
        // check what type of message should be created and what should be the data level
        Map<String, String> messageMetadata = convertToStringMap(metadata.getMetaData());
        ProducerRecord<String, String> outBoundMessage = createOutboundMessage(metadata.getEventType(), messageMetadata, cdc);
        publishMetadata(outBoundMessage);

    }

    private ProducerRecord<String, String> createOutboundMessage(String topic,
                                                                 Map<String, String> data,
                                                                 String previousDataJson) {
        try {
            Map<String, Object> kafkaMessage = new HashMap<>();

            // Determine the operation type
            boolean isInsert = (previousDataJson == null || previousDataJson.isEmpty());
            String operation = isInsert ? "INSERT" : "UPDATE";

            // Extract key attributes
            String primaryKey = data.getOrDefault("primaryKey", "UNKNOWN");
            String primaryKeyValue = data.getOrDefault("primaryKeyValue", "UNKNOWN");
            String table = data.getOrDefault("table", "UNKNOWN");

            // Construct CDC structure
            kafkaMessage.put("eventId", UUID.randomUUID().toString());
            kafkaMessage.put("operation", operation);
            kafkaMessage.put("table", table);
            kafkaMessage.put("primaryKey", primaryKey);
            kafkaMessage.put("primaryKeyValue", primaryKeyValue);
            kafkaMessage.put("timestamp", Instant.now().toString());
            kafkaMessage.put("before", previousDataJson); // Previous state as JSON
            kafkaMessage.put("after", objectMapper.writeValueAsString(data));

            // Serialize to JSON
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);

            // Kafka Key: Primary Key Value (Ensures ordering per entity)
            String kafkaKey = primaryKeyValue;

            return new ProducerRecord<>(topic, kafkaKey, messageJson);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Kafka message", e);
        }
    }
}
