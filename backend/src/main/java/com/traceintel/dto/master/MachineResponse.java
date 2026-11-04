package com.traceintel.dto.master;

public record MachineResponse(Long id, String code, String name, String station, String location, boolean active) {
}
