package com.traceintel.dto.part;

import java.math.BigDecimal;

public record PartResponse(
        Long id,
        String partNumber,
        String name,
        String description,
        BigDecimal costPerPart,
        int partsPerVehicle,
        BigDecimal scrapCost,
        BigDecimal reworkCost
) {
}
