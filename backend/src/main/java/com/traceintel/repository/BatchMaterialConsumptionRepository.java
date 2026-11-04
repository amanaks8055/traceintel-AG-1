package com.traceintel.repository;

import com.traceintel.entity.BatchMaterialConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchMaterialConsumptionRepository extends JpaRepository<BatchMaterialConsumption, Long> {
    List<BatchMaterialConsumption> findByLotId(Long lotId);
    List<BatchMaterialConsumption> findByBatchId(Long batchId);
}
