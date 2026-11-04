package com.traceintel.dto.scan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ScanRequest(
        @NotBlank @Size(max = 100) String serialInput,
        @NotNull @Positive Long batchId,
        @Size(max = 50) String station,
        boolean manualEntry,
        @Positive Long approvedBy,
        String approverPassword
) {
}
