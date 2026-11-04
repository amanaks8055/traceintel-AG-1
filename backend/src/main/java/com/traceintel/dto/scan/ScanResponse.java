package com.traceintel.dto.scan;

import com.traceintel.enums.ScanResult;

import java.time.LocalDateTime;

public record ScanResponse(
        ScanResult result,
        String serial,
        String partName,
        String batchNumber,
        String reason,
        String message,
        Long scanEventId,
        LocalDateTime timestamp
) {
}
