package com.traceintel.controller;

import com.traceintel.dto.user.ResetCodeResponse;
import com.traceintel.dto.user.UserRequest;
import com.traceintel.dto.user.UserResponse;
import com.traceintel.dto.user.UserUpdateRequest;
import com.traceintel.entity.Role;
import com.traceintel.entity.User;
import com.traceintel.enums.RoleType;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.DuplicateUsernameException;
import com.traceintel.exception.InvalidRoleException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.RoleRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.AuditService;
import com.traceintel.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Transactional
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final PasswordResetService passwordResetService;

    public UserController(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            PasswordResetService passwordResetService
    ) {
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userRepository.findAll(Sort.by("fullName")).stream().map(UserResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request, @AuthenticationPrincipal UserDetails admin) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(role(request.role()));
        User saved = userRepository.save(user);
        auditService.log(admin.getUsername(), "USER_CREATED", "USER", saved.getId(), saved.getUsername());
        return UserResponse.from(saved);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request, @AuthenticationPrincipal UserDetails admin) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        boolean removesAdmin = user.isActive() && user.getRole().getName() == RoleType.ADMIN
                && (request.role() != RoleType.ADMIN || !request.active());
        if (removesAdmin && userRepository.countByRoleNameAndActiveTrue(RoleType.ADMIN) == 1) {
            throw new ConflictException("This is the last active admin, so it cannot be disabled or changed.");
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(role(request.role()));
        user.setActive(request.active());
        auditService.log(admin.getUsername(), "USER_UPDATED", "USER", id, request.role() + ", active=" + request.active());
        return UserResponse.from(user);
    }

    @PostMapping("/{id}/reset-code")
    public ResetCodeResponse resetCode(@PathVariable Long id, @AuthenticationPrincipal UserDetails admin) {
        return passwordResetService.createCode(id, admin.getUsername());
    }

    private Role role(RoleType type) {
        return roleRepository.findByName(type).orElseThrow(() -> new InvalidRoleException(type.name()));
    }
}
