package com.lily.metadataProcessingService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lily.metadataProcessingService.cache.MetadataCache;
import com.lily.metadataProcessingService.common.MetadataStatus;
import com.lily.metadataProcessingService.config.ApplicationProperties.TopicConfig;
import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.exception.ProcessingException;
import com.lily.metadataProcessingService.model.rules.MetadataRule;
import com.lily.metadataProcessingService.producer.MetadataProducer;
import com.lily.metadataProcessingService.repository.MetadataRepository;
import com.lily.metadataProcessingService.rule.ProcessingRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lily.metadataProcessingService.common.Constants.*;
import static com.lily.metadataProcessingService.util.CommonUtil.convertToStringMap;

/**
 * Does the actual work of processing metadata.
 * This service:
 * 1. Checks what rules to apply
 * 2. Converts the metadata into our format
 * 3. Figuering out what changed
 * 4. Sending the processed data where it needs to go
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final MetadataRepository repository;
    private final ProcessingRules processingRules;
    private final PreProcessingService preProcessingService;
    private final MetadataProducer metadataProducer;
    private final StatusTrackingService statusTrackingService;
    private final MetadataCache metadataCache;

    /**
     * Main method to process incoming metadata requests.
     * This method:
     * 1. Gets the appropriate processing rules
     * 2. Converts the metadata to our format
     * 3. Generates change data capture (CDC)
     * 4. Sends the processed data to Kafka
     *
     * @param request The incoming metadata request to process
     * @throws ProcessingException if any step of processing fails
     */
    public void processMetadata(MetadataRequest request) {
        String eventId = request.getEventId();
        log.info("Starting metadata processing for eventId: {}", eventId);

        try {
            statusTrackingService.updateStatus(eventId, MetadataStatus.PROCESSING, "Processing started");
            
            log.debug("Fetching processing rules for eventType: {}", request.getEventType());
            MetadataRule rule = processingRules.getRule(request.getEventType());

            log.debug("Converting metadata to internal format. EventId: {}", eventId);
            Metadata processedData = convertToProcessableData(request, rule);

            log.debug("Generating change data capture. EventId: {}", eventId);
            String cdc = inferCdcFromMetaData(processedData, rule);

            log.debug("Sending processed metadata to output. EventId: {}", eventId);
            metadataProducer.sendOutboundKafkaMessage(processedData, cdc);

            log.info("Successfully completed metadata processing. EventId: {}", eventId);
            statusTrackingService.updateStatus(eventId, MetadataStatus.COMPLETED, "Processing completed");
        } catch (Exception e) {
            log.error("Failed to process metadata. EventId: {}, Error: {}", 
                eventId, e.getMessage(), e);
            statusTrackingService.updateStatus(eventId, MetadataStatus.FAILED, e.getMessage());
            throw new ProcessingException("Failed to process metadata", e);
        }
    }

    /**
     * Converts raw metadata into our internal processable format.
     * Uses the provided rule to determine how to convert the data.
     *
     * @param request The original metadata request
     * @param rule The processing rule to apply
     * @return Converted metadata in our internal format
     */
    private Metadata convertToProcessableData(MetadataRequest request, MetadataRule rule) {
        try {
            if (!preProcessingService.isMessageTypeAllowedInEvent(request, rule)) {
                throw new RuntimeException("Metadata message content does not match allowed source for this event type");
            }

            Metadata preProcessedMetaData = preProcessingService.convertToCommonDto(request, rule);

            if (!preProcessingService.isSchemaValid(preProcessedMetaData, rule)) {
                throw new RuntimeException("Metadata schema is not valid for this event type");
            }

            return preProcessingService.enrichMetaData(preProcessedMetaData, rule);

        } catch (Exception e) {
            throw new ProcessingException("Metadata cannot be processed due to : {}", e);
        }
    }

    /**
     * Analyzes metadata to determine what has changed.
     * This helps downstream systems understand what updates were made.
     *
     * @param metadata The processed metadata
     * @param rule The rule used for processing
     * @return A string representing the changes made
     */
    private String inferCdcFromMetaData(Metadata metadata, MetadataRule rule) {
        // business logic on how to infer cdc or lineage
        return generateCDC(metadata);
    }

    public String generateCDC(Metadata metadata) {
        try {
            Map<String,String> newMetadata = convertToStringMap(metadata.getPayload());
            Map<String,String> oldMetadata = fetchPreviousMetadata(metadata);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> cdcEvent = new HashMap<>();

                // Determine operation type
                boolean isInsert = (oldMetadata == null || oldMetadata.isEmpty());
                String operation = isInsert ? "INSERT" : "UPDATE";

                // Extract common metadata fields
                String table = newMetadata.getOrDefault("table", UNKNOWN);
                String primaryKey = newMetadata.getOrDefault("primaryKey", UNKNOWN);
                String primaryKeyValue = newMetadata.getOrDefault("primaryKeyValue", UNKNOWN);
                String user = newMetadata.getOrDefault("user", SYSTEM_USER);
                String version = oldMetadata.getOrDefault("VERSION", DEFAULT_VERSION);

                Map<String, String> beforeState = (isInsert) ? null : new HashMap<>(oldMetadata);
                Map<String, String> afterState = new HashMap<>(newMetadata);

                if (beforeState != null) {
                    beforeState.remove("operation");
                    beforeState.remove("timestamp");
                    beforeState.remove("user");
                }
                afterState.remove("operation");
                afterState.remove("timestamp");
                afterState.remove("user");

                // Build CDC structure with lineage tracking (history of each transaction)
                cdcEvent.put("operation", operation);
                cdcEvent.put("table", table);
                cdcEvent.put("primaryKey", primaryKey);
                cdcEvent.put("primaryKeyValue", primaryKeyValue);
                cdcEvent.put("timestamp", Instant.now().toString());
                cdcEvent.put("user", user);
                cdcEvent.put("before", beforeState);
                cdcEvent.put("after", afterState);
                cdcEvent.put("version", version + 1);

                // TODO: Store event in db as history

                return objectMapper.writeValueAsString(cdcEvent);
            } catch (Exception e) {
                throw new RuntimeException("Error generating CDC JSON", e);
            }
    }

    private Map<String, String> fetchPreviousMetadata(Metadata metadata) {
        // Use cache instead of direct DB call
        // return metadataCache.getPreviousMetadata(metadata.getId());
        return new HashMap<>();
    }

    public void processBatch(List<ConsumerRecord<String, MetadataRequest>> kafkaRecords, TopicConfig config) {
        log.info("Processing batch of {} records with config: batchSize={}, timeout={}ms", 
            kafkaRecords.size(), config.getBatchSize(), config.getTimeoutMs());
        
        kafkaRecords.forEach(requestRecord -> processMetadata(requestRecord.value()));
    }
}

