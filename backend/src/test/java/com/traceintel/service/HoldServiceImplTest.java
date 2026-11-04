package com.traceintel.service;

import com.traceintel.dto.hold.HoldApprovalRequest;
import com.traceintel.dto.hold.HoldRequest;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.QualityHold;
import com.traceintel.entity.User;
import com.traceintel.enums.HoldStatus;
import com.traceintel.enums.LotStatus;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.HoldAlreadyResolvedException;
import com.traceintel.exception.InvalidApprovalException;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.QualityHoldRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.impl.HoldServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoldServiceImplTest {

    @Mock private QualityHoldRepository holds;
    @Mock private MaterialLotRepository lots;
    @Mock private UserRepository users;
    @Mock private AuditService auditService;

    @Test
    void lotAlreadyOnHoldCannotBeRequestedAgain() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        MaterialLot lot = lot(2L);
        lot.setStatus(LotStatus.ON_HOLD);
        when(lots.findById(2L)).thenReturn(Optional.of(lot));

        assertThrows(ConflictException.class, () ->
                service.requestHold(new HoldRequest(2L, "Failed hardness test"), "priya"));
    }

    @Test
    void lotWithWaitingRequestCannotBeRequestedAgain() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        when(lots.findById(2L)).thenReturn(Optional.of(lot(2L)));
        when(holds.existsByLotIdAndStatus(2L, HoldStatus.REQUESTED)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.requestHold(new HoldRequest(2L, "Failed hardness test"), "priya"));
    }

    @Test
    void requesterCannotApproveOwnHold() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        User requester = user(5L, "priya");
        MaterialLot lot = lot(2L);
        QualityHold hold = new QualityHold();
        hold.setId(1L);
        hold.setLot(lot);
        hold.setRequestedBy(requester);
        hold.setStatus(HoldStatus.REQUESTED);
        when(holds.findById(1L)).thenReturn(Optional.of(hold));
        when(users.findByUsername("priya")).thenReturn(Optional.of(requester));

        assertThrows(InvalidApprovalException.class, () ->
                service.approveHold(1L, new HoldApprovalRequest(true, "Not allowed"), "priya"));
    }

    @Test
    void differentUserApprovesAndPlacesLotOnHold() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        User requester = user(5L, "priya");
        User approver = user(6L, "vikram");
        MaterialLot lot = lot(2L);
        QualityHold hold = new QualityHold();
        hold.setId(1L);
        hold.setLot(lot);
        hold.setRequestedBy(requester);
        hold.setStatus(HoldStatus.REQUESTED);
        when(holds.findById(1L)).thenReturn(Optional.of(hold));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(approver));
        when(holds.save(any(QualityHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.approveHold(1L, new HoldApprovalRequest(true, "Approved"), "vikram");

        assertEquals(HoldStatus.APPROVED, response.status());
        assertEquals(LotStatus.ON_HOLD, lot.getStatus());
        verify(lots).save(lot);
        verify(auditService).record(approver, "HOLD_APPROVED", "QUALITY_HOLD", 1L, "Two-person approval completed");
    }

    @Test
    void rejectedHoldLeavesLotUntouched() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        User requester = user(5L, "priya");
        User approver = user(6L, "vikram");
        MaterialLot lot = lot(2L);
        QualityHold hold = new QualityHold();
        hold.setId(1L);
        hold.setLot(lot);
        hold.setRequestedBy(requester);
        hold.setStatus(HoldStatus.REQUESTED);
        when(holds.findById(1L)).thenReturn(Optional.of(hold));
        when(users.findByUsername("vikram")).thenReturn(Optional.of(approver));
        when(holds.save(any(QualityHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.approveHold(1L, new HoldApprovalRequest(false, "Not confirmed"), "vikram");

        assertEquals(HoldStatus.REJECTED, response.status());
        assertEquals(LotStatus.OK, lot.getStatus());
    }

    @Test
    void requestCreatesHoldInRequestedState() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        User requester = user(5L, "priya");
        when(lots.findById(2L)).thenReturn(Optional.of(lot(2L)));
        when(users.findByUsername("priya")).thenReturn(Optional.of(requester));
        when(holds.save(any(QualityHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.requestHold(new HoldRequest(2L, "Tensile strength below limit"), "priya");

        assertEquals(HoldStatus.REQUESTED, response.status());
    }

    @Test
    void resolvedHoldCannotBeDecidedAgain() {
        HoldServiceImpl service = new HoldServiceImpl(holds, lots, users, auditService);
        QualityHold hold = new QualityHold();
        hold.setId(1L);
        hold.setStatus(HoldStatus.APPROVED);
        when(holds.findById(1L)).thenReturn(Optional.of(hold));

        assertThrows(HoldAlreadyResolvedException.class, () ->
                service.approveHold(1L, new HoldApprovalRequest(true, "Again"), "vikram"));
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private MaterialLot lot(Long id) {
        MaterialLot lot = new MaterialLot();
        lot.setId(id);
        lot.setLotNumber("LOT-STL-2026-005");
        lot.setStatus(LotStatus.OK);
        return lot;
    }
}
