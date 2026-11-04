package com.traceintel.service.impl;

import com.traceintel.dto.trace.BackwardTraceRequest;
import com.traceintel.dto.trace.BackwardTraceResponse;
import com.traceintel.dto.trace.ForwardTraceRequest;
import com.traceintel.dto.trace.TraceResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.Part;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.SerialNumber;
import com.traceintel.entity.ShipmentItem;
import com.traceintel.entity.TraceRun;
import com.traceintel.entity.User;
import com.traceintel.entity.VehicleLink;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.ShipmentItemRepository;
import com.traceintel.repository.TraceRunRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.repository.VehicleLinkRepository;
import com.traceintel.service.AuditService;
import com.traceintel.service.TraceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TraceServiceImpl implements TraceService {

    private final MaterialLotRepository lotRepository;
    private final BatchMaterialConsumptionRepository consumptionRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final VehicleLinkRepository vehicleLinkRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final TraceRunRepository traceRunRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public TraceServiceImpl(
            MaterialLotRepository lotRepository,
            BatchMaterialConsumptionRepository consumptionRepository,
            ShipmentItemRepository shipmentItemRepository,
            VehicleLinkRepository vehicleLinkRepository,
            SerialNumberRepository serialNumberRepository,
            TraceRunRepository traceRunRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.lotRepository = lotRepository;
        this.consumptionRepository = consumptionRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.vehicleLinkRepository = vehicleLinkRepository;
        this.serialNumberRepository = serialNumberRepository;
        this.traceRunRepository = traceRunRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public TraceResponse forwardTrace(ForwardTraceRequest request, String username) {
        long startedAt = System.nanoTime();
        MaterialLot lot = lotRepository.findById(request.lotId())
                .orElseThrow(() -> new LotNotFoundException(request.lotId()));
        List<Long> batchIds = new ArrayList<>();
        List<Long> shippedBatchIds = new ArrayList<>();
        List<Long> inFactoryBatchIds = new ArrayList<>();
        List<TraceResponse.BatchSummary> batches = new ArrayList<>();
        Set<TraceResponse.ShipmentSummary> shipments = new LinkedHashSet<>();
        Set<String> vins = new LinkedHashSet<>();
        int affectedParts = 0;
        int estimatedVehicles = 0;
        BigDecimal partsCost = BigDecimal.ZERO;
        BigDecimal scrapCost = BigDecimal.ZERO;
        BigDecimal reworkCost = BigDecimal.ZERO;

        for (BatchMaterialConsumption consumption : consumptionRepository.findByLotId(lot.getId())) {
            ProductionBatch batch = consumption.getBatch();
            Part part = batch.getPart();
            int quantity = batch.getQuantity();
            List<ShipmentItem> items = shipmentItemRepository.findByBatchId(batch.getId());
            boolean shipped = !items.isEmpty();
            batchIds.add(batch.getId());
            (shipped ? shippedBatchIds : inFactoryBatchIds).add(batch.getId());
            batches.add(new TraceResponse.BatchSummary(batch.getId(), batch.getBatchNumber(), part.getName(), quantity, shipped));
            items.forEach(item -> shipments.add(new TraceResponse.ShipmentSummary(
                    item.getShipment().getShipmentNumber(),
                    item.getShipment().getCustomer().getName(),
                    item.getShipment().getShippedDate()
            )));
            vehicleLinkRepository.findByBatchId(batch.getId()).forEach(link -> vins.add(link.getVin()));

            BigDecimal count = BigDecimal.valueOf(quantity);
            affectedParts += quantity;
            estimatedVehicles += part.getPartsPerVehicle() <= 0 ? 0 : quantity / part.getPartsPerVehicle();
            partsCost = partsCost.add(part.getCostPerPart().multiply(count));
            scrapCost = scrapCost.add(part.getScrapCost().multiply(count));
            reworkCost = reworkCost.add(part.getReworkCost().multiply(count));
        }

        BigDecimal estimatedLoss = partsCost.add(scrapCost).add(reworkCost);
        long durationMs = Math.max(1, (System.nanoTime() - startedAt) / 1_000_000);
        User user = findUser(username);
        saveRun(lot, "FORWARD", lot.getLotNumber(),
                "{\"batchCount\":" + batchIds.size() + ",\"vinCount\":" + vins.size() + "}", durationMs, user);
        auditService.record(user, "TRACE_RUN", "MATERIAL_LOT", lot.getId(), "Forward trace");

        return new TraceResponse(
                lot.getId(), lot.getLotNumber(), lot.getSupplier().getName(), batchIds, shippedBatchIds, inFactoryBatchIds,
                batches, List.copyOf(shipments),
                shipments.stream().map(TraceResponse.ShipmentSummary::shipmentNumber).distinct().toList(),
                shipments.stream().map(TraceResponse.ShipmentSummary::customer).distinct().toList(),
                List.copyOf(vins), affectedParts, estimatedVehicles,
                partsCost, scrapCost, reworkCost, estimatedLoss, durationMs
        );
    }

    @Override
    @Transactional
    public BackwardTraceResponse backwardTrace(BackwardTraceRequest request, String username) {
        long startedAt = System.nanoTime();
        String input = request.input().trim();
        List<VehicleLink> links = vehicleLinkRepository.findAllByVin(input.toUpperCase()).stream()
                .filter(item -> item.getBatch() != null)
                .toList();
        VehicleLink link = links.stream().findFirst().orElse(null);
        SerialNumber serial = link != null ? link.getSerialNumber()
                : serialNumberRepository.findBySerial(input).orElse(null);
        ProductionBatch batch = link != null ? link.getBatch() : serial != null ? serial.getBatch() : null;
        if (batch == null) {
            throw new ResourceNotFoundException("No production record is linked to: " + input);
        }
        if (link == null) {
            link = vehicleLinkRepository.findAllBySerialNumberId(serial.getId()).stream().findFirst().orElse(null);
        }
        List<ProductionBatch> batches = links.isEmpty()
                ? List.of(batch)
                : links.stream().map(VehicleLink::getBatch).distinct().toList();
        List<MaterialLot> lots = batches.stream()
                .flatMap(item -> consumptionRepository.findByBatchId(item.getId()).stream())
                .map(BatchMaterialConsumption::getLot)
                .distinct()
                .toList();
        if (lots.isEmpty()) {
            throw new ResourceNotFoundException("No material lot linked to batch: " + batch.getBatchNumber());
        }
        long durationMs = Math.max(1, (System.nanoTime() - startedAt) / 1_000_000);
        saveRun(lots.get(0), "BACKWARD", input, "{\"lotCount\":" + lots.size() + "}", durationMs, findUser(username));
        return new BackwardTraceResponse(
                input,
                link == null ? null : link.getVin(),
                serial == null ? null : serial.getSerial(),
                link == null || link.getCustomer() == null ? null : link.getCustomer().getName(),
                batch.getBatchNumber(),
                batches.stream().map(ProductionBatch::getBatchNumber).toList(),
                batch.getPart().getName(),
                lots.get(0).getLotNumber(),
                lots.get(0).getSupplier().getName(),
                lots.stream().map(lot -> new BackwardTraceResponse.LotSummary(
                        lot.getLotNumber(), lot.getSupplier().getName(), lot.getStatus())).toList(),
                durationMs
        );
    }

    private void saveRun(MaterialLot lot, String direction, String input, String resultJson, long durationMs, User user) {
        TraceRun traceRun = new TraceRun();
        traceRun.setLot(lot);
        traceRun.setDirection(direction);
        traceRun.setInputValue(input);
        traceRun.setResultJson(resultJson);
        traceRun.setDurationMs(durationMs);
        traceRun.setExecutedBy(user);
        traceRun.setExecutedAt(LocalDateTime.now());
        traceRunRepository.save(traceRun);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trace user not found: " + username));
    }
}
