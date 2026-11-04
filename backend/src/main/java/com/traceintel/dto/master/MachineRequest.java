package com.traceintel.dto.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MachineRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String station,
        @Size(max = 100) String location,
        boolean active
) {
}
