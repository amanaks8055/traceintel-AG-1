package com.traceintel.dto.scan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MaterialLotScanRequest(
        @NotNull @Positive Long batchId,
        @NotNull @Positive Long lotId,
        @NotNull @Positive BigDecimal quantity
) {
}
