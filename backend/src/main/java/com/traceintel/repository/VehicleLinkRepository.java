package com.traceintel.repository;

import com.traceintel.entity.VehicleLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleLinkRepository extends JpaRepository<VehicleLink, Long> {
    List<VehicleLink> findAllByVin(String vin);
    List<VehicleLink> findByBatchIsNotNullOrderByIdDesc();
    List<VehicleLink> findByBatchIsNullAndVinContainingIgnoreCaseOrderByVin(String vin, Pageable page);
    List<VehicleLink> findByBatchId(Long batchId);
    List<VehicleLink> findAllBySerialNumberId(Long serialNumberId);
}
