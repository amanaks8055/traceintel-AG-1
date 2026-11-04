package com.traceintel.entity;

import com.traceintel.enums.HoldStatus;
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
@Table(name = "quality_holds")
public class QualityHold extends IdentifiableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private MaterialLot lot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;
    @Column(name = "request_reason", nullable = false)
    private String requestReason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HoldStatus status = HoldStatus.REQUESTED;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    private String approvalNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
}
