package com.traceintel.repository;

import com.traceintel.dto.report.Count;
import com.traceintel.entity.ScanEvent;
import com.traceintel.enums.ScanResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScanEventRepository extends JpaRepository<ScanEvent, Long> {
    boolean existsBySerialNumberIdAndBatchIdAndResult(Long serialNumberId, Long batchId, ScanResult result);
    long countByScannedAtGreaterThanEqual(LocalDateTime start);
    long countByResultAndScannedAtGreaterThanEqual(ScanResult result, LocalDateTime start);
    List<ScanEvent> findByScannedAtGreaterThanEqual(LocalDateTime start);
    @EntityGraph(attributePaths = {"serialNumber.part", "batch", "operator"})
    List<ScanEvent> findTop20ByOrderByScannedAtDesc();
    @EntityGraph(attributePaths = {"serialNumber.part", "batch", "operator"})
    Optional<ScanEvent> findById(Long id);
    @org.springframework.data.jpa.repository.Query("select new com.traceintel.dto.report.Count(e.reason, count(e)) from ScanEvent e where e.reason is not null group by e.reason order by count(e) desc")
    List<Count> countBlockReasons();
}
