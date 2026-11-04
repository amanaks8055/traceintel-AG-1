package com.traceintel.dto.hold;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HoldApprovalRequest(
        @NotNull Boolean approved,
        @Size(max = 500) String notes
) {
}
