package com.traceintel.controller;

import com.traceintel.dto.part.PartRequest;
import com.traceintel.dto.part.PartResponse;
import com.traceintel.entity.Part;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.PartRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/parts")
@Transactional
public class PartController {

    private final PartRepository partRepository;
    private final AuditService auditService;

    public PartController(PartRepository partRepository, AuditService auditService) {
        this.partRepository = partRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<PartResponse> list() {
        return partRepository.findAll(Sort.by("partNumber")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse create(@Valid @RequestBody PartRequest request, @AuthenticationPrincipal UserDetails user) {
        Part part = partRepository.save(apply(new Part(), request));
        auditService.log(user.getUsername(), "PART_CREATED", "PART", part.getId(), part.getPartNumber());
        return toResponse(part);
    }

    @PutMapping("/{id}")
    public PartResponse update(@PathVariable Long id, @Valid @RequestBody PartRequest request, @AuthenticationPrincipal UserDetails user) {
        Part part = find(id);
        apply(part, request);
        auditService.log(user.getUsername(), "PART_UPDATED", "PART", id, part.getPartNumber());
        return toResponse(part);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Part part = find(id);
        partRepository.delete(part);
        partRepository.flush();
        auditService.log(user.getUsername(), "PART_DELETED", "PART", id, part.getPartNumber());
    }

    private Part find(Long id) {
        return partRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
    }

    private Part apply(Part part, PartRequest request) {
        part.setPartNumber(request.partNumber());
        part.setName(request.name());
        part.setDescription(request.description());
        part.setCostPerPart(orZero(request.costPerPart()));
        part.setPartsPerVehicle(request.partsPerVehicle() == null ? 1 : request.partsPerVehicle());
        part.setScrapCost(orZero(request.scrapCost()));
        part.setReworkCost(orZero(request.reworkCost()));
        return part;
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PartResponse toResponse(Part part) {
        return new PartResponse(part.getId(), part.getPartNumber(), part.getName(), part.getDescription(),
                part.getCostPerPart(), part.getPartsPerVehicle(), part.getScrapCost(), part.getReworkCost());
    }
}
