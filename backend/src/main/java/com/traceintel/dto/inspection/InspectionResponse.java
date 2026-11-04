package com.traceintel.dto.inspection;

import java.time.LocalDateTime;

public record InspectionResponse(
        Long id, String lotNumber, String inspector, String result, String notes, LocalDateTime inspectedAt
) {
}
