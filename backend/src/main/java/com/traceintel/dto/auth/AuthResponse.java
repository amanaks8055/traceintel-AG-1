package com.traceintel.dto.auth;

import com.traceintel.enums.RoleType;

public record AuthResponse(
        String token,
        RoleType role,
        String fullName,
        long expiresIn
) {
}
