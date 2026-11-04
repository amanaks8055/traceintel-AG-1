package com.traceintel.service.impl;

import com.traceintel.dto.scan.MaterialLotScanRequest;
import com.traceintel.dto.scan.ScanRequest;
import com.traceintel.dto.scan.ScanResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.ScanEvent;
import com.traceintel.entity.SerialNumber;
import com.traceintel.entity.User;
import com.traceintel.enums.LotStatus;
import com.traceintel.enums.ScanResult;
import com.traceintel.enums.RoleType;
import com.traceintel.exception.BatchNotFoundException;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.ScanEventRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.AuditService;
import com.traceintel.service.ScanService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScanServiceImpl implements ScanService {

    private final SerialNumberRepository serialNumberRepository;
    private final ProductionBatchRepository batchRepository;
    private final BatchMaterialConsumptionRepository consumptionRepository;
    private final MaterialLotRepository lotRepository;
    private final ScanEventRepository scanEventRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final BomItemRepository bomRepository;
    private final PasswordEncoder passwordEncoder;

    public ScanServiceImpl(
            SerialNumberRepository serialNumberRepository,
            ProductionBatchRepository batchRepository,
            BatchMaterialConsumptionRepository consumptionRepository,
            MaterialLotRepository lotRepository,
            ScanEventRepository scanEventRepository,
            UserRepository userRepository,
            AuditService auditService,
            BomItemRepository bomRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.serialNumberRepository = serialNumberRepository;
        this.batchRepository = batchRepository;
        this.consumptionRepository = consumptionRepository;
        this.lotRepository = lotRepository;
        this.scanEventRepository = scanEventRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.bomRepository = bomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ScanResponse validateScan(ScanRequest request, String username) {
        User operator = findUser(username);
        ProductionBatch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new BatchNotFoundException(request.batchId()));
        SerialNumber serial = serialNumberRepository.findBySerial(request.serialInput())
                .orElse(null);

        if (serial == null) {
            return recordBlocked(request, batch, operator, "SERIAL_NOT_FOUND", "Serial number was not found");
        }

        String blockedReason = findBlockedReason(request, batch, serial);
        if (blockedReason != null) {
            return recordBlocked(request, batch, operator, blockedReason, blockedMessage(blockedReason, serial, batch));
        }

        ScanEvent event = createEvent(request, batch, operator, serial, ScanResult.ACCEPTED, null);
        ScanEvent saved = scanEventRepository.save(event);
        auditService.record(operator, "SCAN_ACCEPTED", "SERIAL_NUMBER", serial.getId(), request.serialInput());
        return new ScanResponse(
                ScanResult.ACCEPTED,
                serial.getSerial(),
                serial.getPart().getName(),
                batch.getBatchNumber(),
                null,
                "Part validated successfully",
                saved.getId(),
                saved.getScannedAt()
        );
    }

    @Override
    @Transactional
    public void recordMaterialLotScan(MaterialLotScanRequest request, String username) {
        User operator = findUser(username);
        ProductionBatch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new BatchNotFoundException(request.batchId()));
        MaterialLot lot = lotRepository.findById(request.lotId())
                .orElseThrow(() -> new LotNotFoundException(request.lotId()));
        if (lot.getStatus() != LotStatus.OK) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " is " + lot.getStatus() + " and cannot be used");
        }
        if (lot.isExpired()) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " expired on " + lot.getExpiryDate() + " and cannot be used");
        }
        if (lot.getPart() == null) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " has no material, so it cannot be checked against the bill of materials");
        }
        if (!bomRepository.existsByParentPartIdAndChildPartId(batch.getPart().getId(), lot.getPart().getId())) {
            throw new ConflictException(wrongMaterialMessage(lot, batch));
        }
        List<BatchMaterialConsumption> used = consumptionRepository.findByLotId(lot.getId());
        BigDecimal left = used.stream().map(BatchMaterialConsumption::getQuantityUsed)
                .reduce(lot.getQuantity(), BigDecimal::subtract);
        if (request.quantity().compareTo(left) > 0) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " has only " + left + " " + lot.getUnit() + " left");
        }
        BatchMaterialConsumption consumption = used.stream()
                .filter(item -> item.getBatch().getId().equals(batch.getId()))
                .findFirst()
                .orElseGet(() -> newConsumption(batch, lot));
        consumption.setQuantityUsed(consumption.getQuantityUsed().add(request.quantity()));
        consumptionRepository.save(consumption);
        auditService.record(operator, "MATERIAL_LOT_LINKED", "MATERIAL_LOT", lot.getId(),
                batch.getBatchNumber() + ", " + request.quantity() + " " + lot.getUnit());
    }

    private String wrongMaterialMessage(MaterialLot lot, ProductionBatch batch) {
        String product = batch.getPart().getName();
        if (lot.getPart().getId().equals(batch.getPart().getId())) {
            return "Material lot " + lot.getLotNumber() + " has " + product + " itself as its material. Choose a lot of the raw material " + product + " is made from";
        }
        if (!bomRepository.existsByParentPartId(batch.getPart().getId())) {
            return product + " has no bill of materials yet. An admin must add its materials on the Bill of materials page first";
        }
        return lot.getPart().getName() + " is not in the bill of materials for " + product;
    }

    private BatchMaterialConsumption newConsumption(ProductionBatch batch, MaterialLot lot) {
        BatchMaterialConsumption consumption = new BatchMaterialConsumption();
        consumption.setBatch(batch);
        consumption.setLot(lot);
        consumption.setQuantityUsed(BigDecimal.ZERO);
        consumption.setRecordedAt(LocalDateTime.now());
        return consumption;
    }

    private String findBlockedReason(ScanRequest request, ProductionBatch batch, SerialNumber serial) {
        if (request.manualEntry() && !approvedBySupervisor(request)) {
            return "MANUAL_ENTRY_NOT_APPROVED";
        }
        List<BatchMaterialConsumption> consumptions = consumptionRepository.findByBatchId(batch.getId());
        if (consumptions.stream().map(BatchMaterialConsumption::getLot)
                .anyMatch(lot -> lot.getStatus() == LotStatus.ON_HOLD)) {
            return "LOT_ON_HOLD";
        }
        if (consumptions.stream().map(BatchMaterialConsumption::getLot)
                .anyMatch(lot -> lot.getStatus() == LotStatus.REJECTED)) {
            return "LOT_REJECTED";
        }
        if (!belongsToBatch(serial, batch)) {
            return "WRONG_PART";
        }
        if (scanEventRepository.existsBySerialNumberIdAndBatchIdAndResult(serial.getId(), batch.getId(), ScanResult.ACCEPTED)) {
            return "DUPLICATE_SERIAL";
        }
        return null;
    }

    private boolean approvedBySupervisor(ScanRequest request) {
        if (request.approvedBy() == null || request.approverPassword() == null) {
            return false;
        }
        return userRepository.findById(request.approvedBy())
                .filter(user -> user.getRole().getName() == RoleType.SUPERVISOR || user.getRole().getName() == RoleType.ADMIN)
                .filter(user -> passwordEncoder.matches(request.approverPassword(), user.getPasswordHash()))
                .isPresent();
    }

    private boolean belongsToBatch(SerialNumber serial, ProductionBatch batch) {
        Long partId = serial.getPart().getId();
        Long batchPartId = batch.getPart().getId();
        return partId.equals(batchPartId) || bomRepository.existsByParentPartIdAndChildPartId(batchPartId, partId);
    }

    private ScanResponse recordBlocked(
            ScanRequest request,
            ProductionBatch batch,
            User operator,
            String reason,
            String message
    ) {
        SerialNumber serial = serialNumberRepository.findBySerial(request.serialInput()).orElse(null);
        ScanEvent event = createEvent(request, batch, operator, serial, ScanResult.BLOCKED, reason);
        ScanEvent saved = scanEventRepository.save(event);
        auditService.record(operator, "SCAN_BLOCKED", "SERIAL_NUMBER", serial == null ? null : serial.getId(), reason);
        return new ScanResponse(
                ScanResult.BLOCKED,
                request.serialInput(),
                serial == null ? null : serial.getPart().getName(),
                batch.getBatchNumber(),
                reason,
                message,
                saved.getId(),
                saved.getScannedAt()
        );
    }

    private ScanEvent createEvent(
            ScanRequest request,
            ProductionBatch batch,
            User operator,
            SerialNumber serial,
            ScanResult result,
            String reason
    ) {
        ScanEvent event = new ScanEvent();
        event.setSerialNumber(serial);
        event.setSerialInput(request.serialInput());
        event.setBatch(batch);
        event.setOperator(operator);
        event.setStation(request.station());
        event.setResult(result);
        event.setReason(reason);
        event.setManualEntry(request.manualEntry());
        event.setScannedAt(LocalDateTime.now());
        if (request.approvedBy() != null) {
            event.setApprovedBy(userRepository.findById(request.approvedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + request.approvedBy())));
        }
        return event;
    }

    private String blockedMessage(String reason, SerialNumber serial, ProductionBatch batch) {
        return switch (reason) {
            case "WRONG_PART" -> "Part '" + serial.getPart().getName() + "' is not in BOM for batch " + batch.getBatchNumber() + " (expected: " + batch.getPart().getName() + ")";
            case "LOT_ON_HOLD" -> "Material lot is ON HOLD";
            case "LOT_REJECTED" -> "Material lot is REJECTED";
            case "DUPLICATE_SERIAL" -> "Serial " + serial.getSerial() + " already scanned in batch " + batch.getBatchNumber();
            case "MANUAL_ENTRY_NOT_APPROVED" -> "Manual entry requires supervisor approval";
            default -> reason;
        };
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + username));
    }
}
