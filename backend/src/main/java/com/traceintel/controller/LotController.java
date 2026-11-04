package com.traceintel.controller;

import com.traceintel.dto.production.LotDecisionRequest;
import com.traceintel.dto.production.LotRequest;
import com.traceintel.dto.production.LotResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.MaterialLot;
import com.traceintel.enums.LotStatus;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.repository.SupplierRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/lots")
@Transactional
public class LotController {

    private final MaterialLotRepository lotRepository;
    private final SupplierRepository supplierRepository;
    private final PartRepository partRepository;
    private final BatchMaterialConsumptionRepository consumptionRepository;
    private final BomItemRepository bomRepository;
    private final AuditService auditService;

    public LotController(
            MaterialLotRepository lotRepository,
            SupplierRepository supplierRepository,
            PartRepository partRepository,
            BatchMaterialConsumptionRepository consumptionRepository,
            BomItemRepository bomRepository,
            AuditService auditService
    ) {
        this.consumptionRepository = consumptionRepository;
        this.bomRepository = bomRepository;
        this.lotRepository = lotRepository;
        this.supplierRepository = supplierRepository;
        this.partRepository = partRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<LotResponse> list() {
        return lotRepository.findAll(Sort.by(Sort.Direction.DESC, "receivedDate")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public LotResponse receive(@Valid @RequestBody LotRequest request, @AuthenticationPrincipal UserDetails user) {
        if (request.expiryDate() != null && request.expiryDate().isBefore(request.receivedDate())) {
            throw new BadRequestException("Expiry date cannot be before the received date");
        }
        if (lotRepository.findByLotNumber(request.lotNumber()).isPresent()) {
            throw new ConflictException("Lot number " + request.lotNumber() + " already exists. Use a new number");
        }
        MaterialLot lot = new MaterialLot();
        lot.setLotNumber(request.lotNumber());
        lot.setSupplier(supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId())));
        if (request.partId() != null) {
            lot.setPart(partRepository.findById(request.partId())
                    .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + request.partId())));
        }
        lot.setReceivedDate(request.receivedDate());
        lot.setExpiryDate(request.expiryDate());
        lot.setQuantity(request.quantity());
        if (request.unit() != null && !request.unit().isBlank()) {
            lot.setUnit(request.unit());
        }
        MaterialLot saved = lotRepository.save(lot);
        auditService.log(user.getUsername(), "LOT_RECEIVED", "MATERIAL_LOT", saved.getId(), saved.getLotNumber());
        return toResponse(saved);
    }

    @PutMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LotResponse release(@PathVariable Long id, @Valid @RequestBody LotDecisionRequest request, @AuthenticationPrincipal UserDetails user) {
        MaterialLot lot = heldLot(id);
        lot.setStatus(LotStatus.OK);
        auditService.log(user.getUsername(), "LOT_RELEASED", "MATERIAL_LOT", id, request.reason());
        return toResponse(lot);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LotResponse reject(@PathVariable Long id, @Valid @RequestBody LotDecisionRequest request, @AuthenticationPrincipal UserDetails user) {
        MaterialLot lot = heldLot(id);
        lot.setStatus(LotStatus.REJECTED);
        auditService.log(user.getUsername(), "LOT_REJECTED", "MATERIAL_LOT", id, request.reason());
        return toResponse(lot);
    }

    private MaterialLot heldLot(Long id) {
        MaterialLot lot = lotRepository.findById(id).orElseThrow(() -> new LotNotFoundException(id));
        if (lot.getStatus() != LotStatus.ON_HOLD) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " is not on hold");
        }
        return lot;
    }

    private LotResponse toResponse(MaterialLot lot) {
        BigDecimal remaining = consumptionRepository.findByLotId(lot.getId()).stream()
                .map(BatchMaterialConsumption::getQuantityUsed)
                .reduce(lot.getQuantity(), BigDecimal::subtract);
        return new LotResponse(lot.getId(), lot.getLotNumber(), lot.getSupplier().getName(),
                lot.getPart() == null ? null : lot.getPart().getName(), lot.getReceivedDate(), lot.getExpiryDate(),
                lot.isExpired(), lot.getStatus(), lot.getQuantity(), remaining, lot.getUnit(), lot.isDemo(),
                canMake(lot, remaining));
    }

    private List<LotResponse.CanMake> canMake(MaterialLot lot, BigDecimal remaining) {
        if (lot.getPart() == null) {
            return List.of();
        }
        BigDecimal left = remaining.max(BigDecimal.ZERO);
        return bomRepository.findByChildPartId(lot.getPart().getId()).stream()
                .filter(item -> item.getQuantity().signum() > 0)
                .map(item -> new LotResponse.CanMake(item.getParentPart().getId(), item.getParentPart().getName(),
                        item.getQuantity(), left.divide(item.getQuantity(), 0, RoundingMode.DOWN).longValue()))
                .toList();
    }
}
