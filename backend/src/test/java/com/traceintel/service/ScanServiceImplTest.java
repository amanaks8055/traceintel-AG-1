package com.traceintel.service;

import com.traceintel.dto.scan.MaterialLotScanRequest;
import com.traceintel.dto.scan.ScanRequest;
import com.traceintel.dto.scan.ScanResponse;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.Part;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.Role;
import com.traceintel.entity.ScanEvent;
import com.traceintel.entity.SerialNumber;
import com.traceintel.entity.User;
import com.traceintel.enums.LotStatus;
import com.traceintel.enums.RoleType;
import com.traceintel.enums.ScanResult;
import com.traceintel.exception.ConflictException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.ScanEventRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.impl.ScanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceImplTest {

    @Mock private SerialNumberRepository serialNumbers;
    @Mock private ProductionBatchRepository batches;
    @Mock private BatchMaterialConsumptionRepository consumptions;
    @Mock private MaterialLotRepository lots;
    @Mock private ScanEventRepository scans;
    @Mock private UserRepository users;
    @Mock private AuditService auditService;
    @Mock private BomItemRepository bom;
    @Mock private PasswordEncoder encoder;

    private ScanServiceImpl service;
    private User operator;
    private ProductionBatch batch;
    private SerialNumber serial;
    private MaterialLot lot;

    @BeforeEach
    void setUp() {
        service = new ScanServiceImpl(serialNumbers, batches, consumptions, lots, scans, users, auditService, bom, encoder);
        operator = new User();
        operator.setId(1L);
        operator.setUsername("operator1");
        batch = new ProductionBatch();
        batch.setId(3L);
        batch.setBatchNumber("B-2026-003");
        Part batchPart = new Part();
        batchPart.setId(10L);
        batchPart.setName("Door Handle Assembly");
        batch.setPart(batchPart);
        serial = new SerialNumber();
        serial.setId(42L);
        serial.setSerial("MTH-DH-001-00042");
        serial.setPart(batchPart);
        lot = new MaterialLot();
        lot.setId(5L);
        lot.setLotNumber("LOT-STL-2026-005");
        lot.setStatus(LotStatus.OK);
        Part steel = new Part();
        steel.setId(20L);
        steel.setName("Steel Sheet");
        lot.setPart(steel);
    }

    @Test
    void validScanIsAcceptedAndAudited() {
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.of(serial));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));
        when(scans.existsBySerialNumberIdAndBatchIdAndResult(42L, 3L, ScanResult.ACCEPTED)).thenReturn(false);
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> {
            ScanEvent event = invocation.getArgument(0);
            event.setId(100L);
            return event;
        });

        ScanResponse response = service.validateScan(
                new ScanRequest(serial.getSerial(), 3L, "Assembly-A1", false, null, null),
                "operator1"
        );

        assertEquals(ScanResult.ACCEPTED, response.result());
        verify(auditService).record(operator, "SCAN_ACCEPTED", "SERIAL_NUMBER", 42L, serial.getSerial());
    }

    @Test
    void heldLotBlocksScan() {
        lot.setStatus(LotStatus.ON_HOLD);
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.of(serial));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanResponse response = service.validateScan(
                new ScanRequest(serial.getSerial(), 3L, "Assembly-A1", false, null, null),
                "operator1"
        );

        assertEquals(ScanResult.BLOCKED, response.result());
        assertEquals("LOT_ON_HOLD", response.reason());
        verify(auditService).record(operator, "SCAN_BLOCKED", "SERIAL_NUMBER", 42L, "LOT_ON_HOLD");
    }

    @Test
    void partOutsideBomIsBlocked() {
        Part otherPart = new Part();
        otherPart.setId(20L);
        otherPart.setName("Wiring Harness");
        serial.setPart(otherPart);
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.of(serial));
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));
        when(bom.existsByParentPartIdAndChildPartId(10L, 20L)).thenReturn(false);
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanResponse response = service.validateScan(
                new ScanRequest(serial.getSerial(), 3L, "Assembly-A1", false, null, null),
                "operator1"
        );

        assertEquals("WRONG_PART", response.reason());
    }

    @Test
    void manualEntryWithWrongApproverPasswordIsBlocked() {
        User supervisor = new User();
        Role role = new Role();
        role.setName(RoleType.SUPERVISOR);
        supervisor.setRole(role);
        supervisor.setPasswordHash("hash");
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.of(serial));
        when(users.findById(4L)).thenReturn(Optional.of(supervisor));
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanResponse response = service.validateScan(
                new ScanRequest(serial.getSerial(), 3L, "Assembly-A1", true, 4L, "wrong"),
                "operator1"
        );

        assertEquals("MANUAL_ENTRY_NOT_APPROVED", response.reason());
    }

    @Test
    void repeatedAcceptedSerialIsBlocked() {
        stubScan();
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));
        when(scans.existsBySerialNumberIdAndBatchIdAndResult(42L, 3L, ScanResult.ACCEPTED)).thenReturn(true);

        assertEquals("DUPLICATE_SERIAL", scan(false, null, null).reason());
    }

    @Test
    void unknownSerialIsBlockedAndRecorded() {
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.empty());
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("SERIAL_NOT_FOUND", scan(false, null, null).reason());
        verify(scans).save(any(ScanEvent.class));
    }

    @Test
    void manualEntryWithoutApproverIsBlocked() {
        stubScan();

        assertEquals("MANUAL_ENTRY_NOT_APPROVED", scan(true, null, null).reason());
    }

    @Test
    void manualEntryApprovedBySupervisorIsAccepted() {
        User supervisor = new User();
        Role role = new Role();
        role.setName(RoleType.SUPERVISOR);
        supervisor.setRole(role);
        supervisor.setPasswordHash("hash");
        stubScan();
        when(users.findById(4L)).thenReturn(Optional.of(supervisor));
        when(encoder.matches("right", "hash")).thenReturn(true);
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));

        assertEquals(ScanResult.ACCEPTED, scan(true, 4L, "right").result());
    }

    @Test
    void rejectedLotBlocksScan() {
        lot.setStatus(LotStatus.REJECTED);
        stubScan();
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));

        assertEquals("LOT_REJECTED", scan(false, null, null).reason());
    }

    @Test
    void bomComponentIsAccepted() {
        Part component = new Part();
        component.setId(20L);
        component.setName("Steel Sheet");
        serial.setPart(component);
        stubScan();
        when(consumptions.findByBatchId(3L)).thenReturn(List.of(consumption(lot)));
        when(bom.existsByParentPartIdAndChildPartId(10L, 20L)).thenReturn(true);

        assertEquals(ScanResult.ACCEPTED, scan(false, null, null).result());
    }

    @Test
    void heldLotCannotBeLinkedToBatch() {
        lot.setStatus(LotStatus.ON_HOLD);
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(lots.findById(5L)).thenReturn(Optional.of(lot));

        assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
    }

    @Test
    void expiredLotCannotBeLinkedToBatch() {
        lot.setExpiryDate(LocalDate.now().minusDays(1));
        stubLink();

        assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
    }

    @Test
    void lotWithoutMaterialCannotBeLinked() {
        lot.setPart(null);
        stubLink();

        assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
    }

    @Test
    void materialNotInBomCannotBeLinked() {
        lot.setQuantity(BigDecimal.TEN);
        stubLink();
        when(bom.existsByParentPartIdAndChildPartId(10L, 20L)).thenReturn(false);
        when(bom.existsByParentPartId(10L)).thenReturn(true);

        ConflictException error = assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
        assertEquals("Steel Sheet is not in the bill of materials for Door Handle Assembly", error.getMessage());
    }

    @Test
    void productItselfCannotBeLinkedAsMaterial() {
        lot.setPart(batch.getPart());
        stubLink();

        ConflictException error = assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
        assertEquals("Material lot LOT-STL-2026-005 has Door Handle Assembly itself as its material. "
                + "Choose a lot of the raw material Door Handle Assembly is made from", error.getMessage());
    }

    @Test
    void productWithoutBomTellsTheAdminToAddIt() {
        stubLink();

        ConflictException error = assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1"));
        assertEquals("Door Handle Assembly has no bill of materials yet. "
                + "An admin must add its materials on the Bill of materials page first", error.getMessage());
    }

    @Test
    void lotCanBeLinkedOnItsExpiryDate() {
        lot.setExpiryDate(LocalDate.now());
        lot.setQuantity(BigDecimal.TEN);
        stubLink();
        stubInBom();

        service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, BigDecimal.ONE), "operator1");

        verify(consumptions).save(any(BatchMaterialConsumption.class));
    }

    @Test
    void batchCannotUseMoreThanWhatIsLeftInLot() {
        lot.setQuantity(BigDecimal.TEN);
        BatchMaterialConsumption earlier = consumption(lot);
        earlier.setQuantityUsed(new BigDecimal("8"));
        stubLink();
        stubInBom();
        when(consumptions.findByLotId(5L)).thenReturn(List.of(earlier));

        assertThrows(ConflictException.class, () ->
                service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, new BigDecimal("3")), "operator1"));
    }

    @Test
    void linkingSameLotAgainAddsToTheBatchTotal() {
        lot.setQuantity(BigDecimal.TEN);
        BatchMaterialConsumption earlier = consumption(lot);
        earlier.setQuantityUsed(new BigDecimal("4"));
        stubLink();
        stubInBom();
        when(consumptions.findByLotId(5L)).thenReturn(List.of(earlier));

        service.recordMaterialLotScan(new MaterialLotScanRequest(3L, 5L, new BigDecimal("3")), "operator1");

        assertEquals(new BigDecimal("7"), earlier.getQuantityUsed());
        verify(consumptions).save(earlier);
    }

    private void stubLink() {
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(lots.findById(5L)).thenReturn(Optional.of(lot));
    }

    private void stubInBom() {
        when(bom.existsByParentPartIdAndChildPartId(10L, 20L)).thenReturn(true);
    }

    private void stubScan() {
        when(users.findByUsername("operator1")).thenReturn(Optional.of(operator));
        when(batches.findById(3L)).thenReturn(Optional.of(batch));
        when(serialNumbers.findBySerial(serial.getSerial())).thenReturn(Optional.of(serial));
        when(scans.save(any(ScanEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ScanResponse scan(boolean manual, Long approver, String password) {
        return service.validateScan(new ScanRequest(serial.getSerial(), 3L, "Assembly-A1", manual, approver, password), "operator1");
    }

    private BatchMaterialConsumption consumption(MaterialLot materialLot) {
        BatchMaterialConsumption consumption = new BatchMaterialConsumption();
        consumption.setLot(materialLot);
        consumption.setBatch(batch);
        return consumption;
    }
}
