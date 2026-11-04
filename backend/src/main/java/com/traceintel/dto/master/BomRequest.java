package com.traceintel.dto.master;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BomRequest(
        @NotNull @Positive Long parentPartId,
        @NotNull @Positive Long childPartId,
        @NotNull @Positive BigDecimal quantity
) {
}
