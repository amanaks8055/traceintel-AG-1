package com.traceintel.dto.scan;

import com.traceintel.enums.ScanResult;

import java.time.LocalDateTime;

public record ScanEventResponse(
        Long id,
        String serialInput,
        String partName,
        String batchNumber,
        String operator,
        String station,
        ScanResult result,
        String reason,
        boolean manualEntry,
        LocalDateTime scannedAt
) {
}
