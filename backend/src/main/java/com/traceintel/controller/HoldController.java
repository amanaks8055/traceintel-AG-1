package com.traceintel.controller;

import com.traceintel.dto.hold.HoldApprovalRequest;
import com.traceintel.dto.hold.HoldRequest;
import com.traceintel.dto.hold.QualityHoldResponse;
import com.traceintel.entity.QualityHold;
import com.traceintel.enums.HoldStatus;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.QualityHoldRepository;
import com.traceintel.service.HoldService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holds")
@Validated
public class HoldController {

    private final HoldService holdService;
    private final QualityHoldRepository holdRepository;

    public HoldController(HoldService holdService, QualityHoldRepository holdRepository) {
        this.holdService = holdService;
        this.holdRepository = holdRepository;
    }

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'QUALITY_ENGINEER')")
    public QualityHoldResponse request(
            @Valid @RequestBody HoldRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return holdService.requestHold(request, user.getUsername());
    }

    @PutMapping("/{holdId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'QUALITY_ENGINEER')")
    public QualityHoldResponse approve(
            @PathVariable @Positive Long holdId,
            @Valid @RequestBody HoldApprovalRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return holdService.approveHold(holdId, request, user.getUsername());
    }

    @GetMapping
    public List<QualityHoldResponse> list(@RequestParam(required = false) HoldStatus status) {
        List<QualityHold> holds = status == null
                ? holdRepository.findAll()
                : holdRepository.findByStatus(status);
        return holds.stream().map(QualityHoldResponse::from).toList();
    }

    @GetMapping("/{holdId}")
    public QualityHoldResponse get(@PathVariable @Positive Long holdId) {
        return holdRepository.findById(holdId)
                .map(QualityHoldResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quality hold not found: " + holdId
                ));
    }

}
