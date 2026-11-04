package com.traceintel.dto.master;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name,
        @Email @Size(max = 100) String contactEmail,
        @Size(max = 30) String phone,
        @Size(max = 500) String address
) {
}
