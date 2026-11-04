package com.traceintel.service;

import com.traceintel.entity.AuditLog;
import com.traceintel.entity.User;
import com.traceintel.repository.AuditLogRepository;
import com.traceintel.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public AuditLog log(String username, String action, String entityType, Long entityId, String details) {
        return record(userRepository.findByUsername(username).orElse(null), action, entityType, entityId, details);
    }

    public AuditLog record(User user, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        return auditLogRepository.save(auditLog);
    }
}
