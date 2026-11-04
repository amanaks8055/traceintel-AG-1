package com.traceintel.service;

import com.traceintel.dto.user.ResetCodeResponse;
import com.traceintel.entity.User;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    // Left out 0, O, 1, I and L so a code is easy to read aloud.
    private static final String CODE_LETTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;
    private static final int CODE_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public void request(String login) {
        find(login).filter(User::isActive).ifPresent(user -> user.setPasswordResetRequestedAt(LocalDateTime.now()));
    }

    public ResetCodeResponse createCode(Long userId, String admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!user.isActive()) {
            throw new BadRequestException("Enable this account before resetting its password.");
        }
        String code = newCode();
        user.setPasswordResetCodeHash(passwordEncoder.encode(code));
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(CODE_MINUTES));
        user.setPasswordResetRequestedAt(null);
        auditService.log(admin, "PASSWORD_RESET_CODE_CREATED", "USER", user.getId(), user.getUsername());
        return new ResetCodeResponse(user.getUsername(), code, user.getPasswordResetExpiresAt());
    }

    public void reset(String login, String code, String newPassword) {
        User user = find(login).filter(candidate -> codeMatches(candidate, code))
                .orElseThrow(() -> new BadRequestException("The reset code is wrong or has expired."));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetCodeHash(null);
        user.setPasswordResetExpiresAt(null);
        user.setPasswordResetRequestedAt(null);
        auditService.record(user, "PASSWORD_RESET", "USER", user.getId(), user.getUsername());
    }

    private Optional<User> find(String login) {
        String value = login.trim();
        return userRepository.findByUsername(value).or(() -> userRepository.findFirstByEmailIgnoreCase(value));
    }

    private boolean codeMatches(User user, String code) {
        return user.isActive()
                && user.getPasswordResetCodeHash() != null
                && user.getPasswordResetExpiresAt().isAfter(LocalDateTime.now())
                && passwordEncoder.matches(code.trim().toUpperCase(), user.getPasswordResetCodeHash());
    }

    private String newCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_LETTERS.charAt(random.nextInt(CODE_LETTERS.length())));
        }
        return code.toString();
    }
}
