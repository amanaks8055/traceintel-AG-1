package com.traceintel.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleLinkRequest(
        @NotBlank @Size(min = 17, max = 17) String vin,
        @Positive Long batchId,
        @Positive Long serialNumberId,
        @Positive Long customerId
) {
}
