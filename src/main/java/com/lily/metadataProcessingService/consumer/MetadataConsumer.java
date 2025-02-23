package com.lily.metadataProcessingService.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lily.metadataProcessingService.config.BatchConfigurationService;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.service.IngestionService;
import com.lily.metadataProcessingService.service.ProcessingService;
import com.lily.metadataProcessingService.util.OAuthTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataConsumer {
    private final IngestionService ingestionService;
    private final ProcessingService processingService;
    private final OAuthTokenValidator tokenValidator;
    private final BatchConfigurationService batchConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
        topics = "${spring.kafka.topic.inbound}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record,
    Acknowledgment acknowledgment) {
        try {
            // Extract OAuth Token from Kafka Header
            String authToken = record.headers().lastHeader("Authorization") != null
                    ? new String(record.headers().lastHeader("Authorization").value(), StandardCharsets.UTF_8)
                    : null;

            if (authToken == null || !tokenValidator.validateToken(authToken)) {
                log.error("Invalid or missing OAuth token. Rejecting message.");
                return;
            }

            log.info("Received Kafka Message: Key={}, Value={}", record.key(), record.value());

            // Deserialize JSON into MetadataRequestDTO
            MetadataRequest metadataRequest = objectMapper.readValue(record.value(), MetadataRequest.class);
            ingestionService.ingestMetadata(metadataRequest);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.large-volume-source}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consumeMonteCarloBatch(List<ConsumerRecord<String, MetadataRequest>> records) {
        processBatch(records, "monte-carlo");
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.internal-service}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consumeSlackBatch(List<ConsumerRecord<String, MetadataRequest>> records) {
        processBatch(records, "slack");
    }

    private void processBatch(List<ConsumerRecord<String, MetadataRequest>> records, String sourceType) {
        var config = batchConfig.getConfigForTopic(sourceType);
        if (config != null) {
            processingService.processBatch(records, config);
        } else {
            records.forEach(record -> processingService.processMetadata(record.value()));
        }
    }
}