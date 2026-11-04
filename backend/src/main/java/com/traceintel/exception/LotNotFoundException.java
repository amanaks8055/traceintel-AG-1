package com.traceintel.exception;

public class LotNotFoundException extends ResourceNotFoundException {
    public LotNotFoundException(Long id) {
        super("Material lot not found: " + id);
    }
}
