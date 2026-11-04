package com.traceintel.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String error,
        String message,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    public static ApiErrorResponse of(String error, String message) {
        return new ApiErrorResponse(error, message, LocalDateTime.now(), Map.of());
    }
}
