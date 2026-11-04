package com.traceintel.dto.hold;

import com.traceintel.entity.QualityHold;
import com.traceintel.enums.HoldStatus;
import com.traceintel.enums.LotStatus;

import java.time.LocalDateTime;

public record QualityHoldResponse(
        Long id, Long lotId, String lotNumber, LotStatus lotStatus, HoldStatus status,
        String requestedBy, String resolvedBy, String requestReason, String approvalNotes,
        LocalDateTime requestedAt, LocalDateTime resolvedAt
) {
    public static QualityHoldResponse from(QualityHold hold) {
        return new QualityHoldResponse(
                hold.getId(), hold.getLot().getId(), hold.getLot().getLotNumber(), hold.getLot().getStatus(),
                hold.getStatus(), hold.getRequestedBy().getFullName(),
                hold.getApprovedBy() == null ? null : hold.getApprovedBy().getFullName(),
                hold.getRequestReason(), hold.getApprovalNotes(), hold.getRequestedAt(), hold.getResolvedAt()
        );
    }
}
