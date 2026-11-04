package com.traceintel.entity;

import com.traceintel.enums.ShiftType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "production_batches")
public class ProductionBatch extends BaseEntity {
    @Column(nullable = false)
    private boolean demo;
    @Column(name = "batch_number", nullable = false, unique = true, length = 50)
    private String batchNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftType shift = ShiftType.DAY;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Column(nullable = false)
    private int quantity;
}
