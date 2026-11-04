package com.traceintel.dto.shipment;

public record ShipmentItemResponse(
        Long id,
        Long batchId,
        String batchNumber,
        int quantity
) {
}
