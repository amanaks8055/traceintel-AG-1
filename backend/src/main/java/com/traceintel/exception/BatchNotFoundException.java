package com.traceintel.exception;

public class BatchNotFoundException extends ResourceNotFoundException {
    public BatchNotFoundException(Long id) {
        super("Production batch not found: " + id);
    }
}
