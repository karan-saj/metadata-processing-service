package com.lily.metadataProcessingService.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "metadata")
@Data
public class MetadataRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String key;
    private String value;
    private String tenantId;
}
