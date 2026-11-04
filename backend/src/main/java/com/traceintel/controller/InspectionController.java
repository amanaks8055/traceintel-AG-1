package com.traceintel.controller;

import com.traceintel.dto.inspection.InspectionRequest;
import com.traceintel.dto.inspection.InspectionResponse;
import com.traceintel.entity.QualityInspection;
import com.traceintel.exception.LotNotFoundException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.QualityInspectionRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@Transactional
public class InspectionController {

    private final QualityInspectionRepository inspectionRepository;
    private final MaterialLotRepository lotRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public InspectionController(
            QualityInspectionRepository inspectionRepository,
            MaterialLotRepository lotRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.inspectionRepository = inspectionRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<InspectionResponse> list() {
        return inspectionRepository.findAll(Sort.by(Sort.Direction.DESC, "inspectedAt")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_ENGINEER')")
    public InspectionResponse record(@Valid @RequestBody InspectionRequest request, @AuthenticationPrincipal UserDetails user) {
        QualityInspection inspection = new QualityInspection();
        inspection.setLot(lotRepository.findById(request.lotId()).orElseThrow(() -> new LotNotFoundException(request.lotId())));
        inspection.setInspector(userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + user.getUsername())));
        inspection.setResult(request.result());
        inspection.setNotes(request.notes());
        inspection.setInspectedAt(LocalDateTime.now());
        QualityInspection saved = inspectionRepository.save(inspection);
        auditService.log(user.getUsername(), "INSPECTION_RECORDED", "MATERIAL_LOT", request.lotId(), request.result());
        return toResponse(saved);
    }

    private InspectionResponse toResponse(QualityInspection inspection) {
        return new InspectionResponse(inspection.getId(), inspection.getLot().getLotNumber(),
                inspection.getInspector().getFullName(), inspection.getResult(), inspection.getNotes(), inspection.getInspectedAt());
    }
}
