package com.traceintel.exception;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String role) {
        super("Role is not configured: " + role);
    }
}
