package com.traceintel.repository;

import com.traceintel.entity.BomItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomItemRepository extends JpaRepository<BomItem, Long> {
    List<BomItem> findByParentPartId(Long parentPartId);
    List<BomItem> findByChildPartId(Long childPartId);
    boolean existsByParentPartIdAndChildPartId(Long parentPartId, Long childPartId);
    boolean existsByParentPartId(Long parentPartId);
}
