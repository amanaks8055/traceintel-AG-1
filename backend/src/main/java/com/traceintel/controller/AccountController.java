package com.traceintel.controller;

import com.traceintel.dto.user.ChangePasswordRequest;
import com.traceintel.dto.user.ProfileRequest;
import com.traceintel.dto.user.UserResponse;
import com.traceintel.entity.User;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@Transactional
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @GetMapping
    public UserResponse get(@AuthenticationPrincipal UserDetails principal) {
        return UserResponse.from(find(principal));
    }

    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody ProfileRequest request, @AuthenticationPrincipal UserDetails principal) {
        User user = find(principal);
        user.setFullName(request.fullName().trim());
        return UserResponse.from(user);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails principal) {
        User user = find(principal);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        auditService.log(user.getUsername(), "PASSWORD_CHANGED", "USER", user.getId(), null);
    }

    private User find(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
