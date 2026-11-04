package com.traceintel.dto.inspection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InspectionRequest(
        @NotNull @Positive Long lotId,
        @NotNull @Pattern(regexp = "PASS|FAIL") String result,
        @Size(max = 500) String notes
) {
}
