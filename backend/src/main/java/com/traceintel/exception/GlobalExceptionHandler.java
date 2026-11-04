package com.traceintel.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ExceptionHandler({
            ConflictException.class,
            InvalidApprovalException.class,
            HoldAlreadyResolvedException.class,
            DuplicateUsernameException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(RuntimeException exception) {
        return response(HttpStatus.CONFLICT, exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthOutage(InternalAuthenticationServiceException exception) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "ServiceUnavailable", "The database is not available. Please try again shortly.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleBadLogin(AuthenticationException exception) {
        return response(HttpStatus.UNAUTHORIZED, "AuthenticationFailed", "Incorrect username or password");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(DataIntegrityViolationException exception) {
        return response(HttpStatus.CONFLICT, "DataConflict", "This record already exists or is still in use");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, "BadRequest", exception.getMessage());
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRole(InvalidRoleException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiErrorResponse body = new ApiErrorResponse(
                "ValidationException",
                "Request validation failed",
                LocalDateTime.now(),
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return response(HttpStatus.BAD_REQUEST, "ValidationException", exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(error, message));
    }
}
