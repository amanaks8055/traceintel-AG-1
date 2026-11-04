package com.traceintel.service.impl;

import com.traceintel.dto.hold.HoldApprovalRequest;
import com.traceintel.dto.hold.HoldRequest;
import com.traceintel.dto.hold.QualityHoldResponse;
import com.traceintel.entity.MaterialLot;
import com.traceintel.entity.QualityHold;
import com.traceintel.entity.User;
import com.traceintel.enums.HoldStatus;
import com.traceintel.enums.LotStatus;
import com.traceintel.exception.ConflictException;
import com.traceintel.exception.HoldAlreadyResolvedException;
import com.traceintel.exception.InvalidApprovalException;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.QualityHoldRepository;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.AuditService;
import com.traceintel.service.HoldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class HoldServiceImpl implements HoldService {

    private final QualityHoldRepository holdRepository;
    private final MaterialLotRepository lotRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public HoldServiceImpl(
            QualityHoldRepository holdRepository,
            MaterialLotRepository lotRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.holdRepository = holdRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public QualityHoldResponse requestHold(HoldRequest request, String username) {
        MaterialLot lot = lotRepository.findById(request.lotId())
                .orElseThrow(() -> new LotNotFoundException(request.lotId()));
        if (lot.getStatus() != LotStatus.OK) {
            throw new ConflictException("Material lot " + lot.getLotNumber() + " is already " + lot.getStatus());
        }
        if (holdRepository.existsByLotIdAndStatus(lot.getId(), HoldStatus.REQUESTED)) {
            throw new ConflictException("A hold request for lot " + lot.getLotNumber() + " is already waiting for a decision");
        }
        User requester = findUser(username);
        QualityHold hold = new QualityHold();
        hold.setLot(lot);
        hold.setRequestedBy(requester);
        hold.setRequestReason(request.reason());
        hold.setStatus(HoldStatus.REQUESTED);
        hold.setRequestedAt(LocalDateTime.now());
        QualityHold saved = holdRepository.save(hold);
        auditService.record(requester, "HOLD_REQUESTED", "MATERIAL_LOT", lot.getId(), request.reason());
        return QualityHoldResponse.from(saved);
    }

    @Override
    @Transactional
    public QualityHoldResponse approveHold(Long holdId, HoldApprovalRequest request, String username) {
        QualityHold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Quality hold not found: " + holdId));
        if (hold.getStatus() != HoldStatus.REQUESTED) {
            throw new HoldAlreadyResolvedException(holdId);
        }
        User approver = findUser(username);
        if (hold.getRequestedBy().getId().equals(approver.getId())) {
            throw new InvalidApprovalException("Requester and approver cannot be the same person");
        }
        if (request.approved()) {
            MaterialLot lot = hold.getLot();
            lot.setStatus(LotStatus.ON_HOLD);
            lotRepository.save(lot);
        }
        hold.setStatus(request.approved() ? HoldStatus.APPROVED : HoldStatus.REJECTED);
        hold.setApprovedBy(approver);
        hold.setApprovalNotes(request.notes());
        hold.setResolvedAt(LocalDateTime.now());
        QualityHold saved = holdRepository.save(hold);
        if (request.approved()) {
            auditService.record(approver, "HOLD_APPROVED", "QUALITY_HOLD", holdId, "Two-person approval completed");
        } else {
            auditService.record(approver, "HOLD_REJECTED", "QUALITY_HOLD", holdId, request.notes());
        }
        return QualityHoldResponse.from(saved);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Hold user not found: " + username));
    }
}
