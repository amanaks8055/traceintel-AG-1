package com.traceintel.repository;

import com.traceintel.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    List<ShipmentItem> findByBatchId(Long batchId);
    @EntityGraph(attributePaths = {"batch"})
    List<ShipmentItem> findByShipmentId(Long shipmentId);
}
