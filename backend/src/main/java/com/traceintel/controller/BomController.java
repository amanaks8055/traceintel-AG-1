package com.traceintel.controller;

import com.traceintel.dto.master.BomRequest;
import com.traceintel.dto.master.BomResponse;
import com.traceintel.entity.BomItem;
import com.traceintel.entity.Part;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.BomItemRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bom")
@Transactional
public class BomController {

    private final BomItemRepository bomRepository;
    private final PartRepository partRepository;
    private final AuditService auditService;

    public BomController(BomItemRepository bomRepository, PartRepository partRepository, AuditService auditService) {
        this.bomRepository = bomRepository;
        this.partRepository = partRepository;
        this.auditService = auditService;
    }

    @GetMapping("/{partId}")
    public List<BomResponse> forPart(@PathVariable Long partId) {
        return bomRepository.findByParentPartId(partId).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BomResponse add(@Valid @RequestBody BomRequest request, @AuthenticationPrincipal UserDetails user) {
        if (request.parentPartId().equals(request.childPartId())) {
            throw new BadRequestException("A part cannot be a component of itself");
        }
        BomItem item = new BomItem();
        item.setParentPart(part(request.parentPartId()));
        item.setChildPart(part(request.childPartId()));
        item.setQuantity(request.quantity());
        BomItem saved = bomRepository.save(item);
        auditService.log(user.getUsername(), "BOM_ITEM_ADDED", "BOM_ITEM", saved.getId(), saved.getChildPart().getPartNumber());
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        BomItem item = bomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM item not found: " + id));
        bomRepository.delete(item);
        auditService.log(user.getUsername(), "BOM_ITEM_REMOVED", "BOM_ITEM", id, item.getChildPart().getPartNumber());
    }

    private Part part(Long id) {
        return partRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
    }

    private BomResponse toResponse(BomItem item) {
        return new BomResponse(item.getId(), item.getChildPart().getPartNumber(), item.getChildPart().getName(), item.getQuantity());
    }
}
