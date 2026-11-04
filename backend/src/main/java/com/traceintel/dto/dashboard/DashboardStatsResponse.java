package com.traceintel.dto.dashboard;

public record DashboardStatsResponse(
        long totalBatches,
        long scansToday,
        long blockedToday,
        long lotsOnHold,
        long pendingHolds
) {
}
