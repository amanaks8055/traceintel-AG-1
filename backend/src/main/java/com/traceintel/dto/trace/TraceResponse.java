package com.traceintel.dto.trace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TraceResponse(
        Long lotId,
        String lotNumber,
        String supplier,
        List<Long> batchIds,
        List<Long> shippedBatchIds,
        List<Long> inFactoryBatchIds,
        List<BatchSummary> batches,
        List<ShipmentSummary> shipments,
        List<String> shipmentNumbers,
        List<String> customers,
        List<String> vins,
        int affectedParts,
        int estimatedVehicles,
        BigDecimal partsCost,
        BigDecimal scrapCost,
        BigDecimal reworkCost,
        BigDecimal estimatedLoss,
        long durationMs
) {
    public record BatchSummary(Long id, String batchNumber, String part, int quantity, boolean shipped) {
    }

    public record ShipmentSummary(String shipmentNumber, String customer, LocalDate shippedDate) {
    }
}
