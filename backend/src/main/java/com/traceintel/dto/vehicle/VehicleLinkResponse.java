package com.traceintel.dto.vehicle;

import java.time.LocalDateTime;

public record VehicleLinkResponse(
        Long id, String vin, String batchNumber, String serial, String customer, LocalDateTime importedAt
) {
}
