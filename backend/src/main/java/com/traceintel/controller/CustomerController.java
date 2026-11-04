package com.traceintel.controller;

import com.traceintel.dto.master.ContactRequest;
import com.traceintel.dto.master.ContactResponse;
import com.traceintel.entity.Customer;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.CustomerRepository;
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
@RequestMapping("/api/admin/customers")
@Transactional
public class CustomerController {

    private final CustomerRepository repository;
    private final AuditService auditService;

    public CustomerController(CustomerRepository repository, AuditService auditService) {
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
        Customer customer = repository.save(apply(new Customer(), request));
        auditService.log(user.getUsername(), "CUSTOMER_CREATED", "CUSTOMER", customer.getId(), customer.getCode());
        return toResponse(customer);
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable Long id, @Valid @RequestBody ContactRequest request, @AuthenticationPrincipal UserDetails user) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        apply(customer, request);
        auditService.log(user.getUsername(), "CUSTOMER_UPDATED", "CUSTOMER", id, customer.getCode());
        return toResponse(customer);
    }

    private Customer apply(Customer customer, ContactRequest request) {
        customer.setCode(request.code());
        customer.setName(request.name());
        customer.setContactEmail(request.contactEmail());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        return customer;
    }

    private ContactResponse toResponse(Customer customer) {
        return new ContactResponse(customer.getId(), customer.getCode(), customer.getName(),
                customer.getContactEmail(), customer.getPhone(), customer.getAddress());
    }
}
