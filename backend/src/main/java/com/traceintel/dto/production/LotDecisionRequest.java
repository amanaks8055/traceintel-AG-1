package com.traceintel.dto.production;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LotDecisionRequest(@NotBlank @Size(min = 10, max = 500) String reason) {
}
