package com.traceintel.dto.vehicle;

import java.util.List;

public record VinImportResponse(boolean dryRun, int valid, List<String> errors) {
}
