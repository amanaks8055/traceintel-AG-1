package com.traceintel.dto.report;

import java.util.List;

public record ReportResponse(
        List<Count> blockReasons,
        List<Count> lotStatus,
        List<Count> holdStatus,
        List<Count> inspectionResults,
        List<Count> shipmentsByCustomer
) {
}
