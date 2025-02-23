package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.model.rules.MetadataRule;
import com.lily.metadataProcessingService.processor.MetadataProcessor;
import com.lily.metadataProcessingService.processor.ProcessorRegistry;
import com.lily.metadataProcessingService.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreProcessingService {

    private final ProcessorRegistry processorRegistry;
    private final EncryptionUtil encryptionUtil;

    public Metadata convertToCommonDto(MetadataRequest request, MetadataRule rules) {
        String dataType = getInputMetadataType(request);
        MetadataProcessor processor = processorRegistry.getProcessor(dataType);
        return processor.process(request, rules);
    }

    private String getInputMetadataType(MetadataRequest request) {
        // checks the metadata type, verifies if file is json, excel, csv etc.
        // check content disposition or parse an initial chunk to get the details
        return "json";
    }

    public Metadata enrichMetaData(Metadata metadata, MetadataRule rule) {
        deDuplicateMetaData(metadata, rule);
        addMissingMetaData(metadata, rule);
        maskMetaData(metadata, rule);
        encryptMetaData(metadata, rule);
        return metadata;
    }

    private void deDuplicateMetaData(Metadata metadata, MetadataRule rule) {
        // based on rules check that there are no duplicate metadata
        // use hashmap to check if there are duplicate values
        // or use an external logic based on rule to identify
    }

    private void addMissingMetaData (Metadata metadata, MetadataRule rule) {
        // enrich metadata by adding missing fields
        // calculate fields based on current data
    }

    private void maskMetaData(Metadata metadata, MetadataRule rule) {
        // mask user sensitive data like name, id, email
    }

    private void encryptMetaData(Metadata metadata, MetadataRule rule) {
        // iterate over the metadata and check against rule if it needs to be encrypted
        String data = encryptionUtil.maskSensitiveData("someUser@email.com");
    }

    public boolean isSchemaValid(Metadata metaData, MetadataRule rule) {
        // validate schema based on header interpreted and rule
        return true;
    }

    public boolean isMessageTypeAllowedInEvent(MetadataRequest request, MetadataRule rule) {
        List<String> allowedFormats = Arrays.asList(rule.getAllowedInputFormats().split(","));
        // compare with current format
        return true;
    }
}
