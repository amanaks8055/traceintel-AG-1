package com.traceintel.repository;

import com.traceintel.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @Override
    @EntityGraph(attributePaths = "customer")
    List<Shipment> findAll();
    @Override
    @EntityGraph(attributePaths = "customer")
    Optional<Shipment> findById(Long id);
}
