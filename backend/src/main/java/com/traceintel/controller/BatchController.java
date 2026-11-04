package com.traceintel.controller;

import com.traceintel.dto.production.BatchRequest;
import com.traceintel.dto.production.BatchResponse;
import com.traceintel.dto.production.SerialRequest;
import com.traceintel.dto.production.SerialResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.BomItem;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.SerialNumber;
import com.traceintel.exception.BatchNotFoundException;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.MachineRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.UserRepository;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@Transactional
public class BatchController {

    private final ProductionBatchRepository batchRepository;
    private final PartRepository partRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;
    private final SerialNumberRepository serialRepository;
    private final BatchMaterialConsumptionRepository consumptionRepository;
    private final BomItemRepository bomRepository;
    private final AuditService auditService;

    public BatchController(
            ProductionBatchRepository batchRepository,
            PartRepository partRepository,
            MachineRepository machineRepository,
            UserRepository userRepository,
            SerialNumberRepository serialRepository,
            BatchMaterialConsumptionRepository consumptionRepository,
            BomItemRepository bomRepository,
            AuditService auditService
    ) {
        this.bomRepository = bomRepository;
        this.batchRepository = batchRepository;
        this.partRepository = partRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
        this.serialRepository = serialRepository;
        this.consumptionRepository = consumptionRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<BatchResponse> list() {
        return batchRepository.findAll(Sort.by(Sort.Direction.DESC, "startTime")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public BatchResponse start(@Valid @RequestBody BatchRequest request, @AuthenticationPrincipal UserDetails user) {
        if (batchRepository.findByBatchNumber(request.batchNumber()).isPresent()) {
            throw new ConflictException("Batch number " + request.batchNumber() + " already exists. Use a new number");
        }
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(request.batchNumber());
        batch.setPart(partRepository.findById(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + request.partId())));
        batch.setMachine(machineRepository.findById(request.machineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + request.machineId())));
        batch.setOperator(userRepository.findById(request.operatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + request.operatorId())));
        batch.setShift(request.shift());
        batch.setQuantity(request.quantity());
        batch.setStartTime(LocalDateTime.now());
        ProductionBatch saved = batchRepository.save(batch);
        auditService.log(user.getUsername(), "BATCH_STARTED", "PRODUCTION_BATCH", saved.getId(), saved.getBatchNumber());
        return toResponse(saved);
    }

    @GetMapping("/{id}/serials")
    public List<SerialResponse> serials(@PathVariable Long id) {
        return serialRepository.findByBatchId(id).stream().map(this::toSerial).toList();
    }

    @PostMapping("/{id}/serials")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public List<SerialResponse> generate(
            @PathVariable Long id,
            @Valid @RequestBody SerialRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        ProductionBatch batch = batchRepository.findById(id).orElseThrow(() -> new BatchNotFoundException(id));
        long made = serialRepository.countByBatchId(id);
        long room = batch.getQuantity() - made;
        if (request.count() > room) {
            throw new ConflictException("Batch " + batch.getBatchNumber() + " has room for only " + room + " more serials");
        }
        checkMaterial(batch, made + request.count());
        String partNumber = batch.getPart().getPartNumber();
        long next = serialRepository.countByPartId(batch.getPart().getId()) + 1;
        List<SerialResponse> created = new ArrayList<>();
        while (created.size() < request.count()) {
            String value = "%s-%05d".formatted(partNumber, next++);
            if (serialRepository.existsBySerial(value)) {
                continue;
            }
            SerialNumber serial = new SerialNumber();
            serial.setSerial(value);
            serial.setPart(batch.getPart());
            serial.setBatch(batch);
            serial.setProducedAt(LocalDateTime.now());
            created.add(toSerial(serialRepository.save(serial)));
        }
        auditService.log(user.getUsername(), "SERIALS_GENERATED", "PRODUCTION_BATCH", id, request.count() + " serials");
        return created;
    }

    private void checkMaterial(ProductionBatch batch, long pieces) {
        List<BatchMaterialConsumption> linked = consumptionRepository.findByBatchId(batch.getId());
        if (linked.isEmpty()) {
            throw new ConflictException("Link a material lot to batch " + batch.getBatchNumber() + " before making serials");
        }
        for (BomItem item : bomRepository.findByParentPartId(batch.getPart().getId())) {
            List<BatchMaterialConsumption> lots = linked.stream()
                    .filter(use -> use.getLot().getPart() != null && use.getLot().getPart().getId().equals(item.getChildPart().getId()))
                    .toList();
            if (lots.isEmpty()) {
                continue;
            }
            BigDecimal total = lots.stream().map(BatchMaterialConsumption::getQuantityUsed).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal needed = item.getQuantity().multiply(BigDecimal.valueOf(pieces));
            if (total.compareTo(needed) < 0) {
                String unit = lots.get(0).getLot().getUnit();
                throw new ConflictException(pieces + " pieces need " + needed.stripTrailingZeros().toPlainString() + " " + unit
                        + " of " + item.getChildPart().getName() + ", but only " + total.stripTrailingZeros().toPlainString()
                        + " " + unit + " is linked to batch " + batch.getBatchNumber() + ". Link more material first");
            }
        }
    }

    private SerialResponse toSerial(SerialNumber serial) {
        return new SerialResponse(serial.getId(), serial.getSerial(), serial.getPart().getName());
    }

    private BatchResponse toResponse(ProductionBatch batch) {
        List<String> lots = consumptionRepository.findByBatchId(batch.getId()).stream()
                .map(item -> item.getLot().getLotNumber()).toList();
        return new BatchResponse(batch.getId(), batch.getBatchNumber(), batch.getPart().getId(), batch.getPart().getName(),
                batch.getMachine().getCode(), batch.getOperator().getFullName(), batch.getShift(),
                batch.getStartTime(), batch.getQuantity(), lots, serialRepository.countByBatchId(batch.getId()), batch.isDemo(),
                bomRepository.existsByParentPartId(batch.getPart().getId()));
    }
}
