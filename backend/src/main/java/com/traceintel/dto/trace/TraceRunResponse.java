package com.traceintel.dto.trace;

import java.time.LocalDateTime;

public record TraceRunResponse(
        Long id,
        String direction,
        String inputValue,
        Long lotId,
        String lotNumber,
        long durationMs,
        String executedBy,
        LocalDateTime executedAt
) {
}
