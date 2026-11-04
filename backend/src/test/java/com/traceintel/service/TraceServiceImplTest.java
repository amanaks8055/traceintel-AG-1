package com.traceintel.service;

import com.traceintel.dto.trace.BackwardTraceRequest;
import com.traceintel.dto.trace.BackwardTraceResponse;
import com.traceintel.dto.trace.ForwardTraceRequest;
import com.traceintel.dto.trace.TraceResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.Customer;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.Machine;
import com.traceintel.entity.Part;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.SerialNumber;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.entity.Shipment;
import com.traceintel.entity.ShipmentItem;
import com.traceintel.entity.Supplier;
import com.traceintel.entity.User;
import com.traceintel.entity.VehicleLink;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.ShipmentItemRepository;
import com.traceintel.repository.TraceRunRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.repository.VehicleLinkRepository;
import com.traceintel.service.impl.TraceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceServiceImplTest {

    @Mock private MaterialLotRepository lots;
    @Mock private BatchMaterialConsumptionRepository consumptions;
    @Mock private ShipmentItemRepository shipmentItems;
    @Mock private VehicleLinkRepository vehicleLinks;
    @Mock private SerialNumberRepository serialNumbers;
    @Mock private TraceRunRepository traceRuns;
    @Mock private UserRepository users;
    @Mock private AuditService auditService;

    private TraceServiceImpl service;
    private User manager;
    private MaterialLot lot;
    private ProductionBatch shippedBatch;
    private ProductionBatch factoryBatch;

    @BeforeEach
    void setUp() {
        service = new TraceServiceImpl(lots, consumptions, shipmentItems, vehicleLinks, serialNumbers, traceRuns, users, auditService);
        manager = user(6L, "vikram");
        Part part = new Part();
        part.setId(1L);
        part.setName("Door Handle Assembly");
        part.setPartsPerVehicle(4);
        part.setCostPerPart(new BigDecimal("300"));
        part.setScrapCost(new BigDecimal("80"));
        part.setReworkCost(new BigDecimal("45"));
        Supplier supplier = new Supplier();
        supplier.setName("Tata Steel Ltd");
        lot = new MaterialLot();
        lot.setId(5L);
        lot.setLotNumber("LOT-STL-2026-005");
        lot.setPart(part);
        lot.setSupplier(supplier);
        shippedBatch = batch(3L, "B-2026-003", part, 500);
        factoryBatch = batch(7L, "B-2026-007", part, 300);
    }

    @Test
    void forwardTraceSeparatesBatchesAndCalculatesEstimates() {
        when(lots.findById(5L)).thenReturn(Optional.of(lot));
        when(consumptions.findByLotId(5L)).thenReturn(List.of(consumption(shippedBatch), consumption(factoryBatch)));
        Shipment shipment = new Shipment();
        shipment.setShipmentNumber("SH-2026-001");
        Customer customer = new Customer();
        customer.setName("Maruti Suzuki India Ltd");
        shipment.setCustomer(customer);
        ShipmentItem item = new ShipmentItem();
        item.setShipment(shipment);
        when(shipmentItems.findByBatchId(3L)).thenReturn(List.of(item));
        when(shipmentItems.findByBatchId(7L)).thenReturn(List.of());
        VehicleLink vin = new VehicleLink();
        vin.setVin("MA3FJEB1S00123456");
        when(vehicleLinks.findByBatchId(3L)).thenReturn(List.of(vin));
        when(vehicleLinks.findByBatchId(7L)).thenReturn(List.of());
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));

        TraceResponse response = service.forwardTrace(new ForwardTraceRequest(5L), "vikram");

        assertEquals(2, response.batchIds().size());
        assertEquals(List.of(3L), response.shippedBatchIds());
        assertEquals(List.of(7L), response.inFactoryBatchIds());
        assertEquals(200, response.estimatedVehicles());
        assertEquals(new BigDecimal("340000"), response.estimatedLoss());
        assertEquals(List.of("MA3FJEB1S00123456"), response.vins());
        verify(traceRuns).save(any());
        verify(auditService).record(manager, "TRACE_RUN", "MATERIAL_LOT", 5L, "Forward trace");
    }

    @Test
    void backwardTraceFromVinReturnsBatchAndSupplier() {
        when(vehicleLinks.findAllByVin("MA3FJEB1S00123456")).thenReturn(List.of(vehicleLink(shippedBatch)));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(shippedBatch)));

        BackwardTraceResponse response = service.backwardTrace(
                new BackwardTraceRequest("MA3FJEB1S00123456"),
                "vikram"
        );

        assertEquals("B-2026-003", response.batchNumber());
        assertEquals("LOT-STL-2026-005", response.lotNumber());
        assertTrue(response.durationMs() >= 1);
    }

    @Test
    void backwardTraceFromSerialFindsBatch() {
        SerialNumber serial = new SerialNumber();
        serial.setId(42L);
        serial.setSerial("MTH-DH-001-00042");
        serial.setBatch(shippedBatch);
        when(serialNumbers.findBySerial("MTH-DH-001-00042")).thenReturn(Optional.of(serial));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(shippedBatch)));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));

        BackwardTraceResponse response = service.backwardTrace(new BackwardTraceRequest("MTH-DH-001-00042"), "vikram");

        assertEquals("B-2026-003", response.batchNumber());
        verify(traceRuns).save(any());
    }

    @Test
    void vinLinkedToTwoBatchesListsBoth() {
        when(vehicleLinks.findAllByVin("MA3FJEB1S00123456")).thenReturn(List.of(vehicleLink(shippedBatch), vehicleLink(factoryBatch)));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(shippedBatch)));
        when(consumptions.findByBatchId(7L)).thenReturn(List.of(consumption(factoryBatch)));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));

        BackwardTraceResponse response = service.backwardTrace(new BackwardTraceRequest("MA3FJEB1S00123456"), "vikram");

        assertEquals(List.of("B-2026-003", "B-2026-007"), response.batches());
        assertEquals(1, response.lots().size());
    }

    @Test
    void vinWithNoProductionRecordIsNotFound() {
        when(vehicleLinks.findAllByVin("UNLINKEDVIN000000")).thenReturn(List.of(new VehicleLink()));

        assertThrows(ResourceNotFoundException.class, () ->
                service.backwardTrace(new BackwardTraceRequest("UNLINKEDVIN000000"), "vikram"));
    }

    @Test
    void unusedLotReturnsEmptyTrace() {
        when(lots.findById(5L)).thenReturn(Optional.of(lot));
        when(consumptions.findByLotId(5L)).thenReturn(List.of());
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));

        TraceResponse response = service.forwardTrace(new ForwardTraceRequest(5L), "vikram");

        assertEquals(0, response.affectedParts());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.estimatedLoss()));
    }

    @Test
    void unknownLotIsNotFound() {
        when(lots.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LotNotFoundException.class, () -> service.forwardTrace(new ForwardTraceRequest(99L), "vikram"));
    }

    @Test
    void lossSplitsIntoPartsScrapAndRework() {
        when(lots.findById(5L)).thenReturn(Optional.of(lot));
        when(consumptions.findByLotId(5L)).thenReturn(List.of(consumption(factoryBatch)));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(manager));

        TraceResponse response = service.forwardTrace(new ForwardTraceRequest(5L), "vikram");

        assertEquals(new BigDecimal("90000"), response.partsCost());
        assertEquals(new BigDecimal("24000"), response.scrapCost());
        assertEquals(new BigDecimal("13500"), response.reworkCost());
        assertEquals(75, response.estimatedVehicles());
    }

    private BatchMaterialConsumption consumption(ProductionBatch batch) {
        BatchMaterialConsumption item = new BatchMaterialConsumption();
        item.setBatch(batch);
        item.setLot(lot);
        return item;
    }

    private ProductionBatch batch(Long id, String number, Part part, int quantity) {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(id);
        batch.setBatchNumber(number);
        batch.setPart(part);
        batch.setQuantity(quantity);
        batch.setMachine(new Machine());
        batch.setOperator(manager);
        return batch;
    }

    private VehicleLink vehicleLink(ProductionBatch batch) {
        VehicleLink link = new VehicleLink();
        link.setVin("MA3FJEB1S00123456");
        link.setBatch(batch);
        return link;
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
