package com.traceintel.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShipmentRequest(
        @NotBlank @Size(max = 50) String shipmentNumber,
        @NotNull @Positive Long customerId,
        @NotNull LocalDate shippedDate,
        @Size(max = 500) String notes
) {
}
