package com.traceintel.controller;

import com.traceintel.dto.auth.AuthResponse;
import com.traceintel.dto.auth.ForgotPasswordRequest;
import com.traceintel.dto.auth.LoginRequest;
import com.traceintel.dto.auth.RegisterRequest;
import com.traceintel.dto.auth.ResetPasswordRequest;
import com.traceintel.dto.auth.SignupRequest;
import com.traceintel.entity.Role;
import com.traceintel.entity.User;
import com.traceintel.enums.RoleType;
import com.traceintel.exception.DuplicateUsernameException;
import com.traceintel.exception.InvalidRoleException;
import com.traceintel.repository.RoleRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.security.JwtTokenProvider;
import com.traceintel.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            PasswordResetService passwordResetService
    ) {
        this.passwordResetService = passwordResetService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        return toResponse(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email) || userRepository.existsByUsername(email)) {
            throw new DuplicateUsernameException(email);
        }
        Role role = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new InvalidRoleException(RoleType.USER.name()));
        User user = new User();
        user.setUsername(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.name().trim());
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(userRepository.save(user)));
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.request(request.login());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request.login(), request.code(), request.newPassword());
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new InvalidRoleException(request.role().name()));
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(role);
        user.setActive(true);
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                tokenProvider.generateToken(user),
                user.getRole().getName(),
                user.getFullName(),
                tokenProvider.getExpirationMs()
        );
    }
}
