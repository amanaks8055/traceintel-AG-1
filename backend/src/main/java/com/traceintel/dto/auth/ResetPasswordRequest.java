package com.traceintel.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 100) String login,
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(min = 8, max = 100) String newPassword
) {
}
