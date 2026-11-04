package com.traceintel.dto.production;

import com.traceintel.enums.LotStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LotResponse(
        Long id, String lotNumber, String supplier, String part, LocalDate receivedDate,
        LocalDate expiryDate, boolean expired, LotStatus status, BigDecimal quantity, BigDecimal remaining,
        String unit, boolean demo, List<CanMake> canMake
) {
    /** One product this lot's material goes into, and how many pieces the remaining material is enough for. */
    public record CanMake(Long partId, String part, BigDecimal perPiece, long pieces) {
    }
}
