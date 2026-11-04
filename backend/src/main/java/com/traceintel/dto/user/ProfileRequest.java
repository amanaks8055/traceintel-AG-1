package com.traceintel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(@NotBlank @Size(max = 100) String fullName) {
}
