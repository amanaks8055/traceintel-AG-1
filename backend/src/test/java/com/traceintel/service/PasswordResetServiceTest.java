package com.traceintel.service;

import com.traceintel.dto.user.ResetCodeResponse;
import com.traceintel.entity.Role;
import com.traceintel.entity.User;
import com.traceintel.exception.BadRequestException;
import com.traceintel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final String NEW_PASSWORD = "NewPass123";

    @Mock private UserRepository users;
    @Mock private AuditService auditService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, encoder, auditService);
        user = new User();
        user.setId(1L);
        user.setUsername("operator1");
        user.setPasswordHash(encoder.encode("OldPass123"));
        user.setRole(new Role());
    }

    @Test
    void requestMarksTheUserForTheAdmin() {
        when(users.findByUsername("operator1")).thenReturn(Optional.of(user));
        service.request(" operator1 ");
        assertNotNull(user.getPasswordResetRequestedAt());
    }

    @Test
    void requestFindsTheUserByEmail() {
        when(users.findByUsername("op@example.com")).thenReturn(Optional.empty());
        when(users.findFirstByEmailIgnoreCase("op@example.com")).thenReturn(Optional.of(user));
        service.request("op@example.com");
        assertNotNull(user.getPasswordResetRequestedAt());
    }

    @Test
    void requestForUnknownLoginGivesNoError() {
        when(users.findByUsername("nobody")).thenReturn(Optional.empty());
        when(users.findFirstByEmailIgnoreCase("nobody")).thenReturn(Optional.empty());
        service.request("nobody");
        assertNull(user.getPasswordResetRequestedAt());
    }

    @Test
    void codeIsStoredOnlyAsHash() {
        String code = createCode();
        assertEquals(10, code.length());
        assertFalse(code.equals(user.getPasswordResetCodeHash()));
        assertTrue(encoder.matches(code, user.getPasswordResetCodeHash()));
    }

    @Test
    void disabledUserGetsNoCode() {
        user.setActive(false);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> service.createCode(1L, "admin"));
    }

    @Test
    void correctCodeChangesThePassword() {
        String code = createCode();
        when(users.findByUsername("operator1")).thenReturn(Optional.of(user));
        service.reset("operator1", code.toLowerCase(), NEW_PASSWORD);
        assertTrue(encoder.matches(NEW_PASSWORD, user.getPasswordHash()));
    }

    @Test
    void codeWorksOnlyOnce() {
        String code = createCode();
        when(users.findByUsername("operator1")).thenReturn(Optional.of(user));
        service.reset("operator1", code, NEW_PASSWORD);
        assertThrows(BadRequestException.class, () -> service.reset("operator1", code, "Another123"));
        assertTrue(encoder.matches(NEW_PASSWORD, user.getPasswordHash()));
    }

    @Test
    void wrongCodeLeavesThePasswordAlone() {
        createCode();
        when(users.findByUsername("operator1")).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> service.reset("operator1", "WRONGCODE2", NEW_PASSWORD));
        assertTrue(encoder.matches("OldPass123", user.getPasswordHash()));
    }

    @Test
    void expiredCodeIsRefused() {
        String code = createCode();
        user.setPasswordResetExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(users.findByUsername("operator1")).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> service.reset("operator1", code, NEW_PASSWORD));
    }

    private String createCode() {
        when(users.findById(1L)).thenReturn(Optional.of(user));
        ResetCodeResponse response = service.createCode(1L, "admin");
        return response.code();
    }
}
