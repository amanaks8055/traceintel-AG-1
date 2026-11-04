package com.traceintel.controller;

import com.traceintel.dto.audit.AuditLogResponse;
import com.traceintel.dto.audit.AuditPage;
import com.traceintel.entity.AuditLog;
import com.traceintel.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@Transactional(readOnly = true)
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    public AuditPage logs(@RequestParam(defaultValue = "0") int page) {
        Page<AuditLog> logs = auditLogRepository.findAll(PageRequest.of(Math.max(0, page), 25, Sort.by(Sort.Direction.DESC, "id")));
        return new AuditPage(logs.map(this::toResponse).getContent(), logs.getNumber(), logs.getTotalPages());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUser() == null ? null : log.getUser().getFullName(),
                log.getAction(), log.getEntityType(), log.getEntityId(), log.getDetails(), log.getCreatedAt());
    }
}
