package com.traceintel.dto.trace;

import com.traceintel.enums.LotStatus;

import java.util.List;

public record BackwardTraceResponse(
        String input,
        String vin,
        String serial,
        String customer,
        String batchNumber,
        List<String> batches,
        String part,
        String lotNumber,
        String supplier,
        List<LotSummary> lots,
        long durationMs
) {
    public record LotSummary(String lotNumber, String supplier, LotStatus status) {
    }
}
