package com.traceintel.repository;

import com.traceintel.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    Optional<SerialNumber> findBySerial(String serial);
    boolean existsBySerial(String serial);
    long countByPartId(Long partId);
    long countByBatchId(Long batchId);
    List<SerialNumber> findByBatchId(Long batchId);
}
