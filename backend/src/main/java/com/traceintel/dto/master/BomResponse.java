package com.traceintel.dto.master;

import java.math.BigDecimal;

public record BomResponse(Long id, String partNumber, String name, BigDecimal quantity) {
}
