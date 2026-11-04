package com.traceintel.repository;

import com.traceintel.entity.ProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    Optional<ProductionBatch> findByBatchNumber(String batchNumber);
}
