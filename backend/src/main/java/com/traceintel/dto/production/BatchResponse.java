package com.traceintel.dto.production;

import com.traceintel.enums.ShiftType;

import java.time.LocalDateTime;
import java.util.List;

public record BatchResponse(
        Long id, String batchNumber, Long partId, String part, String machine, String operator, ShiftType shift,
        LocalDateTime startTime, int quantity, List<String> lots, long serialCount, boolean demo, boolean hasBom
) {
}
