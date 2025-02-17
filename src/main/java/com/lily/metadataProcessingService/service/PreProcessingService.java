package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreProcessingService {

    EncryptionUtil encryptionUtil;

    public Metadata convertToCommonDto(MetadataRequest request, String rules) {
        String dataType = getInputMetadataType(request);
        switch (dataType) {
            case "json": {
                // call service for json parsing
                break;
            }
            case "csv": {
                // call service for json parsing
            }
            default : {
                // call a generic parsing service to get file details
            }
        }
        return new Metadata();
    }

    private String getInputMetadataType(MetadataRequest request) {
        // checks the metadata type, verifies if file is json, excel, csv etc
        // check content disposition or parse a initial chunk to get the details
        return "json";
    }

    public Metadata enrichMetaData(Metadata metadata, String rule) {
        deDuplicateMetaData(metadata, rule);
        addMissingMetaData(metadata, rule);
        maskMetaData(metadata, rule);
        encryptMetaData(metadata, rule);
        return metadata;
    }

    private void deDuplicateMetaData(Metadata metadata, String rule) {
        // based on rules check that there are no duplicate metadata
        // use hashmap to check if there are duplicate values
        // or use an external logic based on rule to identify
    }

    private void addMissingMetaData (Metadata metadata, String rule) {
        // enrich metadata by adding missing fields
        // calculate fields based on current data
    }

    private void maskMetaData(Metadata metadata, String rule) {
        // mask user sensitive data like name, id, email
    }

    private void encryptMetaData(Metadata metadata, String rule) {
        // iterate over the metadata and check against rule if it needs to be encrypted
        String data = encryptionUtil.maskSensitiveData("someUser@email.com");
    }

    public boolean isSchemaValid(Metadata metaData, String rule) {
        // validate schema based on header interpreted and rule
        return true;
    }

    public boolean isMessageTypeAllowedInEvent(MetadataRequest request, String rule) {
        // get format of metadata and match against rule if its allowed
        // check by content disposition, file name or by processing the data partially to understand type
        return rule.equals(request.getEventType());
    }
}
