package com.lily.metadataProcessingService.repository;

import com.lily.metadataProcessingService.model.MetadataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetadataRepository extends JpaRepository<MetadataRecord, Long> {
}
