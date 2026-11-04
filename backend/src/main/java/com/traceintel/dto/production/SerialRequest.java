package com.traceintel.dto.production;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SerialRequest(@Min(1) @Max(500) int count) {
}
