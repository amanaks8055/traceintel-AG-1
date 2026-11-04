package com.traceintel.dto.user;

import java.time.LocalDateTime;

public record ResetCodeResponse(String username, String code, LocalDateTime expiresAt) {
}
