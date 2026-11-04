package com.traceintel.repository;

import com.traceintel.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {
}
