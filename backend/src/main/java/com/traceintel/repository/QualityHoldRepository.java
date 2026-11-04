package com.traceintel.repository;

import com.traceintel.entity.QualityHold;
import com.traceintel.enums.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface QualityHoldRepository extends JpaRepository<QualityHold, Long> {
    long countByStatus(HoldStatus status);
    boolean existsByLotIdAndStatus(Long lotId, HoldStatus status);
    @EntityGraph(attributePaths = {"lot", "requestedBy", "approvedBy"})
    List<QualityHold> findByStatus(HoldStatus status);
    @EntityGraph(attributePaths = {"lot", "requestedBy", "approvedBy"})
    List<QualityHold> findAll();
    @EntityGraph(attributePaths = {"lot", "requestedBy", "approvedBy"})
    Optional<QualityHold> findById(Long id);
}
