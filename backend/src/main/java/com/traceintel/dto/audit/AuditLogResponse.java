package com.traceintel.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id, String user, String action, String entityType, Long entityId, String details, LocalDateTime createdAt
) {
}
