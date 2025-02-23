package com.lily.metadataProcessingService.repository;

import com.lily.metadataProcessingService.model.MetadataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataRepository extends JpaRepository<MetadataRecord, Long> {
    @Query("SELECT m FROM MetadataRecord m WHERE m.tenantId = :tenantId")
    List<MetadataRecord> findByTenantId(String tenantId);
}
