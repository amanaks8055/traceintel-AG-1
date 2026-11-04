package com.traceintel.dto.trace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ForwardTraceRequest(
        @NotNull @Positive Long lotId
) {
}
