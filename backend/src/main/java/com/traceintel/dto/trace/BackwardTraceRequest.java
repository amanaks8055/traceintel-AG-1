package com.traceintel.dto.trace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BackwardTraceRequest(
        @NotBlank @Size(max = 100) String input
) {
}
