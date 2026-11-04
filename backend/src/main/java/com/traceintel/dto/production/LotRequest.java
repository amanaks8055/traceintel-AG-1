package com.traceintel.dto.production;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LotRequest(
        @NotBlank @Size(max = 50) String lotNumber,
        @NotNull @Positive Long supplierId,
        @NotNull @Positive Long partId,
        @NotNull LocalDate receivedDate,
        LocalDate expiryDate,
        @NotNull @Positive BigDecimal quantity,
        @Size(max = 20) String unit
) {
}
