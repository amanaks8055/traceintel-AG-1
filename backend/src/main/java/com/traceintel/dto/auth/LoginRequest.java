package com.traceintel.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(max = 100) String password
) {
}
