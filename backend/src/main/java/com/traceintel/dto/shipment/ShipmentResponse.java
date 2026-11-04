package com.traceintel.dto.shipment;

import java.time.LocalDate;
import java.util.List;

public record ShipmentResponse(
        Long id, String shipmentNumber, String customerCode, String customerName, LocalDate shippedDate,
        String notes, List<ShipmentItemResponse> items, boolean demo
) {
}
