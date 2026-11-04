package com.traceintel.dto.dashboard;

import java.time.LocalDate;

public record ScanActivityResponse(
        LocalDate date,
        long accepted,
        long blocked
) {
}
