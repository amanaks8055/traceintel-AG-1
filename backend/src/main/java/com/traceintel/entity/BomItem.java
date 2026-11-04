package com.traceintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bom_items", uniqueConstraints = @UniqueConstraint(columnNames = {"parent_part_id", "child_part_id"}))
public class BomItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_part_id", nullable = false)
    private Part parentPart;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_part_id", nullable = false)
    private Part childPart;
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity = BigDecimal.ONE;
    @Column(nullable = false)
    private int level = 1;
}
