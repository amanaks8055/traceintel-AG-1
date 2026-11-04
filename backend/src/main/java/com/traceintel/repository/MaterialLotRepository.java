package com.traceintel.repository;

import com.traceintel.entity.MaterialLot;
import com.traceintel.enums.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialLotRepository extends JpaRepository<MaterialLot, Long> {
    Optional<MaterialLot> findByLotNumber(String lotNumber);
    long countByStatus(LotStatus status);
}
