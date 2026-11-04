package com.traceintel.repository;

import com.traceintel.entity.TraceRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface TraceRunRepository extends JpaRepository<TraceRun, Long> {
    @EntityGraph(attributePaths = {"lot", "executedBy"})
    List<TraceRun> findAll();
}
