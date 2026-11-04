package com.traceintel.controller;

import com.traceintel.dto.master.Option;
import com.traceintel.dto.scan.MaterialLotScanRequest;
import com.traceintel.dto.scan.ScanRequest;
import com.traceintel.dto.scan.ScanResponse;
import com.traceintel.enums.RoleType;
import com.traceintel.repository.UserRepository;
import com.traceintel.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanService scanService;
    private final UserRepository userRepository;

    public ScanController(ScanService scanService, UserRepository userRepository) {
        this.scanService = scanService;
        this.userRepository = userRepository;
    }

    @GetMapping("/approvers")
    public List<Option> approvers() {
        return userRepository.findByRoleNameInAndActiveTrue(List.of(RoleType.SUPERVISOR, RoleType.ADMIN)).stream()
                .map(user -> new Option(user.getId(), user.getFullName()))
                .toList();
    }

    @PostMapping("/validate")
    public ScanResponse validate(
            @Valid @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return scanService.validateScan(request, user.getUsername());
    }

    @PostMapping("/material-lot")
    public void recordMaterialLot(
            @Valid @RequestBody MaterialLotScanRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        scanService.recordMaterialLotScan(request, user.getUsername());
    }
}
