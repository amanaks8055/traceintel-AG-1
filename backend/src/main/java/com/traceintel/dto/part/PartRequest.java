package com.traceintel.dto.part;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PartRequest(
        @NotBlank @Size(max = 50) String partNumber,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @PositiveOrZero BigDecimal costPerPart,
        @Positive Integer partsPerVehicle,
        @PositiveOrZero BigDecimal scrapCost,
        @PositiveOrZero BigDecimal reworkCost
) {
}
