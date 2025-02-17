package com.lily.metadataProcessingService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lily.metadataProcessingService.config.ProcessingRules;
import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.exception.ProcessingException;
import com.lily.metadataProcessingService.producer.MetadataProducer;
import com.lily.metadataProcessingService.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.lily.metadataProcessingService.util.CommonUtil.convertToStringMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final MetadataRepository repository;
    private final ProcessingRules processingRules;
    private final PreProcessingService preProcessingService;
    private final MetadataProducer metadataProducer;

    public void processMetadata(MetadataRequest request) {
        log.info("Starting metadata processing...");

        try {
            log.info("Fetching processing rule for current event");
            String rule = processingRules.fetchRule(request.getEventType());

            Metadata processedMetaData = convertToProcessableData(request, rule);

            String cdc = inferCdcFromMetaData(processedMetaData, rule);

            metadataProducer.sendOutboundKafkaMessage(processedMetaData, cdc);

        } catch (Exception e) {
            log.error("Error while trying to process metadata");
            throw new ProcessingException("Error while trying to process metadata", e);
        }
    }

    private Metadata convertToProcessableData(MetadataRequest request, String rule) {
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

    private String inferCdcFromMetaData(Metadata metadata, String rule) {
        // business logic on how to infer cdc or lineage
        return generateCDC(metadata);
    }

    public String generateCDC(Metadata metadata) {
        try {
            Map<String,String> newMetadata = convertToStringMap(metadata.getMetaData());
            Map<String,String> oldMetadata = fetchPreviousMetadata(metadata);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> cdcEvent = new HashMap<>();

                // Determine operation type
                boolean isInsert = (oldMetadata == null || oldMetadata.isEmpty());
                String operation = isInsert ? "INSERT" : "UPDATE";

                // Extract common metadata fields
                String table = newMetadata.getOrDefault("table", "UNKNOWN");
                String primaryKey = newMetadata.getOrDefault("primaryKey", "UNKNOWN");
                String primaryKeyValue = newMetadata.getOrDefault("primaryKeyValue", "UNKNOWN");
                String user = newMetadata.getOrDefault("user", "system");
                String version = oldMetadata.getOrDefault("VERSION","0");

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
        // call db or cache of db to get previous state of the data
        return Map.of(
                "operation", "UPDATE",
                "table", "users",
                "primaryKey", "user_id",
                "primaryKeyValue", "123",
                "email", "old@example.com",
                "status", "active",
                "user", "admin"
        );
    }
}

