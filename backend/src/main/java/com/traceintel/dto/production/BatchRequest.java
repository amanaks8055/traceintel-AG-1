package com.traceintel.dto.production;

import com.traceintel.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BatchRequest(
        @NotBlank @Size(max = 50) String batchNumber,
        @NotNull @Positive Long partId,
        @NotNull @Positive Long machineId,
        @NotNull @Positive Long operatorId,
        @NotNull ShiftType shift,
        @NotNull @Positive Integer quantity
) {
}
