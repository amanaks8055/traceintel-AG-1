package com.traceintel.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username is already registered: " + username);
    }
}
