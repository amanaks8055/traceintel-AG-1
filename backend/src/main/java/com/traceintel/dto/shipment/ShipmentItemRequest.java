package com.traceintel.dto.shipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShipmentItemRequest(@NotNull @Positive Long batchId, @NotNull @Positive Integer quantity) {
}
