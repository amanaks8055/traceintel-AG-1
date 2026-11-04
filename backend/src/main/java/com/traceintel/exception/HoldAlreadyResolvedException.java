package com.traceintel.exception;

public class HoldAlreadyResolvedException extends RuntimeException {
    public HoldAlreadyResolvedException(Long id) {
        super("Quality hold is already resolved: " + id);
    }
}
