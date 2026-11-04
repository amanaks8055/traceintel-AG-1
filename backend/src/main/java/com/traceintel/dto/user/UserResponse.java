package com.traceintel.dto.user;

import com.traceintel.entity.User;
import com.traceintel.enums.RoleType;

public record UserResponse(Long id, String username, String fullName, String email, RoleType role, boolean active, boolean resetRequested) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getRole().getName(), user.isActive(), user.getPasswordResetRequestedAt() != null);
    }
}
