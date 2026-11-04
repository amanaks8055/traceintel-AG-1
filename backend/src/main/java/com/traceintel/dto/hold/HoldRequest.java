package com.traceintel.dto.hold;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HoldRequest(
        @NotNull @Positive Long lotId,
        @NotBlank @Size(min = 10, max = 500) String reason
) {
}
