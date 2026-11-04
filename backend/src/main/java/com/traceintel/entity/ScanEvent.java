package com.traceintel.entity;

import com.traceintel.enums.ScanResult;
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
@Table(name = "scan_events")
public class ScanEvent extends IdentifiableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_number_id")
    private SerialNumber serialNumber;
    @Column(name = "serial_input", nullable = false, length = 100)
    private String serialInput;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;
    @Column(length = 50)
    private String station;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanResult result;
    private String reason;
    @Column(name = "manual_entry")
    private boolean manualEntry;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;
}
