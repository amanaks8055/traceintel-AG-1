package com.traceintel.controller;

import com.traceintel.dto.production.LotDecisionRequest;
import com.traceintel.dto.production.LotRequest;
import com.traceintel.dto.production.SerialRequest;
import com.traceintel.dto.shipment.ShipmentItemRequest;
import com.traceintel.dto.user.UserUpdateRequest;
import com.traceintel.entity.BatchMaterialConsumption;
import com.traceintel.entity.BomItem;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.Part;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.Role;
import com.traceintel.entity.SerialNumber;
import com.traceintel.entity.Shipment;
import com.traceintel.entity.ShipmentItem;
import com.traceintel.entity.Supplier;
import com.traceintel.entity.User;
import com.traceintel.enums.LotStatus;
import com.traceintel.enums.RoleType;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ConflictException;
import com.traceintel.repository.BatchMaterialConsumptionRepository;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.CustomerRepository;
import com.traceintel.repository.MachineRepository;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.RoleRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.ShipmentItemRepository;
import com.traceintel.repository.ShipmentRepository;
import com.traceintel.repository.SupplierRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.security.CustomUserDetailsService;
import com.traceintel.service.AuditService;
import com.traceintel.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessRulesTest {

    @Mock private UserRepository users;
    @Mock private RoleRepository roles;
    @Mock private PartRepository parts;
    @Mock private MachineRepository machines;
    @Mock private SupplierRepository suppliers;
    @Mock private CustomerRepository customers;
    @Mock private MaterialLotRepository lots;
    @Mock private ProductionBatchRepository batches;
    @Mock private SerialNumberRepository serials;
    @Mock private BatchMaterialConsumptionRepository consumption;
    @Mock private BomItemRepository bom;
    @Mock private ShipmentRepository shipments;
    @Mock private ShipmentItemRepository shipmentItems;
    @Mock private AuditService auditService;
    @Mock private PasswordResetService passwordResetService;
    @Mock private UserDetails principal;

    @Test
    void lastActiveAdminCannotBeDisabled() {
        User admin = admin();
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        when(users.countByRoleNameAndActiveTrue(RoleType.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> userController().update(1L, update(RoleType.ADMIN, false), null));
        assertTrue(admin.isActive());
    }

    @Test
    void lastActiveAdminCannotLoseTheRole() {
        when(users.findById(1L)).thenReturn(Optional.of(admin()));
        when(users.countByRoleNameAndActiveTrue(RoleType.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> userController().update(1L, update(RoleType.OPERATOR, true), null));
    }

    @Test
    void lotCannotExpireBeforeItIsReceived() {
        LotRequest request = new LotRequest("LOT-1", 1L, null, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 1, 1), BigDecimal.TEN, null);

        assertThrows(BadRequestException.class, () -> lotController().receive(request, null));
    }

    @Test
    void heldLotCanBeRejected() {
        MaterialLot lot = lot(LotStatus.ON_HOLD);
        when(lots.findById(1L)).thenReturn(Optional.of(lot));

        var response = lotController().reject(1L, new LotDecisionRequest("Failed the retest twice"), principal);

        assertEquals(LotStatus.REJECTED, response.status());
        assertEquals(LotStatus.REJECTED, lot.getStatus());
    }

    @Test
    void okLotCannotBeRejectedDirectly() {
        when(lots.findById(1L)).thenReturn(Optional.of(lot(LotStatus.OK)));

        assertThrows(ConflictException.class, () ->
                lotController().reject(1L, new LotDecisionRequest("Skipping the two-person hold"), principal));
    }

    @Test
    void rejectedLotCannotBeReleased() {
        when(lots.findById(1L)).thenReturn(Optional.of(lot(LotStatus.REJECTED)));

        assertThrows(ConflictException.class, () ->
                lotController().release(1L, new LotDecisionRequest("Trying to bring it back"), principal));
    }

    @Test
    void batchCannotHaveMoreSerialsThanItsQuantity() {
        when(batches.findById(1L)).thenReturn(Optional.of(batch(5)));
        when(serials.countByBatchId(1L)).thenReturn(4L);

        assertThrows(ConflictException.class, () -> batchController().generate(1L, new SerialRequest(2), null));
    }

    @Test
    void serialsNeedALinkedLot() {
        when(batches.findById(1L)).thenReturn(Optional.of(batch(10)));
        when(consumption.findByBatchId(1L)).thenReturn(List.of());

        assertThrows(ConflictException.class, () -> batchController().generate(1L, new SerialRequest(1), null));
    }

    @Test
    void serialsCannotUseMoreMaterialThanLinked() {
        when(batches.findById(1L)).thenReturn(Optional.of(batch(10)));
        when(serials.countByBatchId(1L)).thenReturn(2L);
        when(consumption.findByBatchId(1L)).thenReturn(List.of(used(steelLot(), "10")));
        when(bom.findByParentPartId(10L)).thenReturn(List.of(bomItem("2")));

        ConflictException error = assertThrows(ConflictException.class, () -> batchController().generate(1L, new SerialRequest(4), null));
        assertEquals("6 pieces need 12 kg of Steel Sheet, but only 10 kg is linked to batch B-1. Link more material first", error.getMessage());
    }

    @Test
    void serialsAreMadeWhenLinkedMaterialIsEnough() {
        when(batches.findById(1L)).thenReturn(Optional.of(batch(10)));
        when(consumption.findByBatchId(1L)).thenReturn(List.of(used(steelLot(), "10")));
        when(bom.findByParentPartId(10L)).thenReturn(List.of(bomItem("2")));
        when(serials.save(any(SerialNumber.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(5, batchController().generate(1L, new SerialRequest(5), principal).size());
    }

    @Test
    void lotShowsHowManyPiecesItCanMake() {
        MaterialLot lot = steelLot();
        lot.setQuantity(new BigDecimal("100"));
        when(lots.findAll(any(Sort.class))).thenReturn(List.of(lot));
        when(consumption.findByLotId(lot.getId())).thenReturn(List.of(used(lot, "21")));
        when(bom.findByChildPartId(20L)).thenReturn(List.of(bomItem("2")));

        var canMake = lotController().list().get(0).canMake();

        assertEquals(1, canMake.size());
        assertEquals("Door Handle Assembly", canMake.get(0).part());
        assertEquals(39, canMake.get(0).pieces());
    }

    @Test
    void batchCannotShipMoreThanItsQuantity() {
        ShipmentController controller = new ShipmentController(shipments, shipmentItems, customers, batches, consumption, auditService);
        ShipmentItem shippedBefore = new ShipmentItem();
        shippedBefore.setQuantity(8);
        when(shipments.findById(1L)).thenReturn(Optional.of(new Shipment()));
        when(batches.findById(2L)).thenReturn(Optional.of(batch(10)));
        when(shipmentItems.findByBatchId(2L)).thenReturn(List.of(shippedBefore));

        assertThrows(ConflictException.class, () -> controller.addItem(1L, new ShipmentItemRequest(2L, 3), null));
    }

    @Test
    void loginWorksWithEmailInAnyCase() {
        User admin = admin();
        when(users.findByUsername("ADMIN@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(users.findFirstByEmailIgnoreCase("ADMIN@EXAMPLE.COM")).thenReturn(Optional.of(admin));

        var details = new CustomUserDetailsService(users).loadUserByUsername("ADMIN@EXAMPLE.COM");

        assertEquals("admin", details.getUsername());
        assertFalse(details.getAuthorities().isEmpty());
    }

    private LotController lotController() {
        return new LotController(lots, suppliers, parts, consumption, bom, auditService);
    }

    private BatchController batchController() {
        return new BatchController(batches, parts, machines, users, serials, consumption, bom, auditService);
    }

    private Part part(Long id, String name) {
        Part part = new Part();
        part.setId(id);
        part.setName(name);
        part.setPartNumber("P-" + id);
        return part;
    }

    private MaterialLot steelLot() {
        MaterialLot lot = lot(LotStatus.OK);
        lot.setId(7L);
        lot.setPart(part(20L, "Steel Sheet"));
        lot.setUnit("kg");
        return lot;
    }

    private BatchMaterialConsumption used(MaterialLot lot, String quantity) {
        BatchMaterialConsumption use = new BatchMaterialConsumption();
        use.setLot(lot);
        use.setQuantityUsed(new BigDecimal(quantity));
        return use;
    }

    private BomItem bomItem(String perPiece) {
        BomItem item = new BomItem();
        item.setParentPart(part(10L, "Door Handle Assembly"));
        item.setChildPart(part(20L, "Steel Sheet"));
        item.setQuantity(new BigDecimal(perPiece));
        return item;
    }

    private MaterialLot lot(LotStatus status) {
        MaterialLot lot = new MaterialLot();
        lot.setSupplier(new Supplier());
        lot.setQuantity(BigDecimal.TEN);
        lot.setStatus(status);
        return lot;
    }

    private UserController userController() {
        return new UserController(users, roles, null, auditService, passwordResetService);
    }

    private UserUpdateRequest update(RoleType role, boolean active) {
        return new UserUpdateRequest("Admin", "admin@example.com", role, active);
    }

    private User admin() {
        Role role = new Role();
        role.setName(RoleType.ADMIN);
        User user = new User();
        user.setUsername("admin");
        user.setPasswordHash("hash");
        user.setRole(role);
        return user;
    }

    private ProductionBatch batch(int quantity) {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(1L);
        batch.setBatchNumber("B-1");
        batch.setQuantity(quantity);
        batch.setPart(part(10L, "Door Handle Assembly"));
        return batch;
    }
}
