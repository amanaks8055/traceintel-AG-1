package com.traceintel.security;

import com.traceintel.config.SecurityConfig;
import com.traceintel.controller.ActivityController;
import com.traceintel.controller.AuditController;
import com.traceintel.controller.AuthController;
import com.traceintel.controller.PartController;
import com.traceintel.controller.ScanController;
import com.traceintel.controller.TraceController;
import com.traceintel.entity.Role;
import com.traceintel.entity.User;
import com.traceintel.enums.RoleType;
import com.traceintel.repository.AuditLogRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.repository.RoleRepository;
import com.traceintel.repository.ScanEventRepository;
import com.traceintel.repository.TraceRunRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.AuditService;
import com.traceintel.service.PasswordResetService;
import com.traceintel.service.ScanService;
import com.traceintel.service.TraceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ActivityController.class, AuditController.class, AuthController.class,
        PartController.class, ScanController.class, TraceController.class
})
@Import(SecurityConfig.class)
class SecurityRulesTest {

    private static final String SIGNUP = "{\"name\":\"New User\",\"email\":\"new@example.com\",\"password\":\"Str0ngPass!\",\"role\":\"ADMIN\"}";
    private static final String SCAN = "{\"serialInput\":\"FD-100-00001\",\"batchId\":1}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;
    @MockitoBean private UserRepository users;
    @MockitoBean private RoleRepository roles;
    @MockitoBean private PartRepository parts;
    @MockitoBean private AuditLogRepository auditLogs;
    @MockitoBean private ScanEventRepository scanEvents;
    @MockitoBean private TraceRunRepository traceRuns;
    @MockitoBean private AuditService auditService;
    @MockitoBean private PasswordResetService passwordResetService;
    @MockitoBean private ScanService scanService;
    @MockitoBean private TraceService traceService;

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/parts").header("Authorization", "Bearer changed.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotOpenAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/parts")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    void qualityEngineerCannotOpenAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/parts")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCanScan() throws Exception {
        postJson("/api/scan/validate", SCAN).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void selfRegisteredUserCannotScan() throws Exception {
        postJson("/api/scan/validate", SCAN).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void scanWithMissingFieldsIsBadRequest() throws Exception {
        postJson("/api/scan/validate", "{}").andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    void qualityEngineerCanTrace() throws Exception {
        postJson("/api/trace/forward", "{\"lotId\":1}").andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotTrace() throws Exception {
        postJson("/api/trace/forward", "{\"lotId\":1}").andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void supervisorCannotReadTraceHistory() throws Exception {
        mockMvc.perform(get("/api/trace/history")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanReadAuditLog() throws Exception {
        when(auditLogs.findAll(any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/api/audit/logs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotReadAuditLog() throws Exception {
        mockMvc.perform(get("/api/audit/logs")).andExpect(status().isForbidden());
    }

    @Test
    void signupIgnoresRequestedRoleAndCreatesUserAccount() throws Exception {
        Role role = new Role();
        role.setName(RoleType.USER);
        when(roles.findByName(RoleType.USER)).thenReturn(Optional.of(role));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postJson("/api/auth/signup", SIGNUP).andExpect(status().isCreated());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals(RoleType.USER, saved.getValue().getRole().getName());
        assertEquals("new@example.com", saved.getValue().getUsername());
    }

    @Test
    void duplicateEmailIsConflict() throws Exception {
        when(users.existsByEmailIgnoreCase("new@example.com")).thenReturn(true);
        postJson("/api/auth/signup", SIGNUP).andExpect(status().isConflict());
    }

    @Test
    void invalidEmailIsBadRequest() throws Exception {
        postJson("/api/auth/signup", "{\"name\":\"X\",\"email\":\"not-an-email\",\"password\":\"Str0ngPass!\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongLoginIsUnauthorized() throws Exception {
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("missing"));
        postJson("/api/auth/login", "{\"username\":\"nobody\",\"password\":\"wrongpass\"}")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPasswordNeedsNoLogin() throws Exception {
        postJson("/api/auth/forgot-password", "{\"login\":\"nobody\"}").andExpect(status().isNoContent());
        verify(passwordResetService).request("nobody");
    }

    @Test
    void pingNeedsNoLogin() throws Exception {
        mockMvc.perform(get("/api/auth/ping")).andExpect(status().isOk());
    }

    @Test
    void shortNewPasswordIsBadRequest() throws Exception {
        postJson("/api/auth/reset-password", "{\"login\":\"operator1\",\"code\":\"ABCDE23456\",\"newPassword\":\"short\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void onlyAdminCanCreateResetCode() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/reset-code")).andExpect(status().isForbidden());
    }

    private ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
