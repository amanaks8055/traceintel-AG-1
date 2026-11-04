package com.traceintel.dto.audit;

import java.util.List;

public record AuditPage(List<AuditLogResponse> rows, int page, int totalPages) {
}
