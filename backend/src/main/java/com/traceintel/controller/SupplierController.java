package com.traceintel.controller;

import com.traceintel.dto.master.ContactRequest;
import com.traceintel.dto.master.ContactResponse;
import com.traceintel.entity.Supplier;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.SupplierRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@Transactional
public class SupplierController {

    private final SupplierRepository repository;
    private final AuditService auditService;

    public SupplierController(SupplierRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<ContactResponse> list() {
        return repository.findAll(Sort.by("name")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(@Valid @RequestBody ContactRequest request, @AuthenticationPrincipal UserDetails user) {
        Supplier supplier = repository.save(apply(new Supplier(), request));
        auditService.log(user.getUsername(), "SUPPLIER_CREATED", "SUPPLIER", supplier.getId(), supplier.getCode());
        return toResponse(supplier);
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable Long id, @Valid @RequestBody ContactRequest request, @AuthenticationPrincipal UserDetails user) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        apply(supplier, request);
        auditService.log(user.getUsername(), "SUPPLIER_UPDATED", "SUPPLIER", id, supplier.getCode());
        return toResponse(supplier);
    }

    private Supplier apply(Supplier supplier, ContactRequest request) {
        supplier.setCode(request.code());
        supplier.setName(request.name());
        supplier.setContactEmail(request.contactEmail());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        return supplier;
    }

    private ContactResponse toResponse(Supplier supplier) {
        return new ContactResponse(supplier.getId(), supplier.getCode(), supplier.getName(),
                supplier.getContactEmail(), supplier.getPhone(), supplier.getAddress());
    }
}
