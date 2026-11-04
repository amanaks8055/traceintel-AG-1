package com.traceintel.controller;

import com.traceintel.dto.shipment.ShipmentItemRequest;
import com.traceintel.dto.shipment.ShipmentItemResponse;
import com.traceintel.dto.shipment.ShipmentRequest;
import com.traceintel.dto.shipment.ShipmentResponse;
import com.traceintel.entity.Shipment;
import com.traceintel.entity.ShipmentItem;
import com.traceintel.enums.LotStatus;
import com.traceintel.exception.BatchNotFoundException;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.CustomerRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.ShipmentItemRepository;
import com.traceintel.repository.ShipmentRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@Transactional
public class ShipmentController {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductionBatchRepository batchRepository;
    private final BatchMaterialConsumptionRepository consumptionRepository;
    private final AuditService auditService;

    public ShipmentController(
            ShipmentRepository shipmentRepository,
            ShipmentItemRepository shipmentItemRepository,
            CustomerRepository customerRepository,
            ProductionBatchRepository batchRepository,
            BatchMaterialConsumptionRepository consumptionRepository,
            AuditService auditService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.customerRepository = customerRepository;
        this.batchRepository = batchRepository;
        this.consumptionRepository = consumptionRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<ShipmentResponse> list() {
        return shipmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ShipmentResponse get(@PathVariable Long id) {
        return toResponse(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ShipmentResponse create(@Valid @RequestBody ShipmentRequest request, @AuthenticationPrincipal UserDetails user) {
        Shipment shipment = shipmentRepository.save(apply(new Shipment(), request));
        auditService.log(user.getUsername(), "SHIPMENT_CREATED", "SHIPMENT", shipment.getId(), shipment.getShipmentNumber());
        return toResponse(shipment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ShipmentResponse update(@PathVariable Long id, @Valid @RequestBody ShipmentRequest request, @AuthenticationPrincipal UserDetails user) {
        Shipment shipment = apply(find(id), request);
        auditService.log(user.getUsername(), "SHIPMENT_UPDATED", "SHIPMENT", id, shipment.getShipmentNumber());
        return toResponse(shipment);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse addItem(@PathVariable Long id, @Valid @RequestBody ShipmentItemRequest request, @AuthenticationPrincipal UserDetails user) {
        Shipment shipment = find(id);
        ShipmentItem item = new ShipmentItem();
        item.setShipment(shipment);
        item.setBatch(batchRepository.findById(request.batchId()).orElseThrow(() -> new BatchNotFoundException(request.batchId())));
        consumptionRepository.findByBatchId(request.batchId()).stream()
                .map(consumption -> consumption.getLot())
                .filter(lot -> lot.getStatus() != LotStatus.OK)
                .findFirst()
                .ifPresent(lot -> {
                    throw new ConflictException("Batch uses lot " + lot.getLotNumber() + ", which is " + lot.getStatus() + ", so it cannot ship");
                });
        int shipped = shipmentItemRepository.findByBatchId(request.batchId()).stream().mapToInt(ShipmentItem::getQuantity).sum();
        int left = item.getBatch().getQuantity() - shipped;
        if (request.quantity() > left) {
            throw new ConflictException("Batch " + item.getBatch().getBatchNumber() + " has only " + left + " pieces left to ship");
        }
        item.setQuantity(request.quantity());
        shipmentItemRepository.save(item);
        auditService.log(user.getUsername(), "SHIPMENT_ITEM_ADDED", "SHIPMENT", id, item.getBatch().getBatchNumber());
        return toResponse(shipment);
    }

    private Shipment find(Long id) {
        return shipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    private Shipment apply(Shipment shipment, ShipmentRequest request) {
        shipment.setShipmentNumber(request.shipmentNumber());
        shipment.setCustomer(customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId())));
        shipment.setShippedDate(request.shippedDate());
        shipment.setNotes(request.notes());
        return shipment;
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        List<ShipmentItemResponse> items = shipmentItemRepository.findByShipmentId(shipment.getId()).stream()
                .map(item -> new ShipmentItemResponse(item.getId(), item.getBatch().getId(), item.getBatch().getBatchNumber(), item.getQuantity()))
                .toList();
        return new ShipmentResponse(shipment.getId(), shipment.getShipmentNumber(), shipment.getCustomer().getCode(),
                shipment.getCustomer().getName(), shipment.getShippedDate(), shipment.getNotes(), items, shipment.isDemo());
    }
}
