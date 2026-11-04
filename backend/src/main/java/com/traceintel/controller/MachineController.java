package com.traceintel.controller;

import com.traceintel.dto.master.MachineRequest;
import com.traceintel.dto.master.MachineResponse;
import com.traceintel.entity.Machine;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.MachineRepository;
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
@RequestMapping("/api/admin/machines")
@Transactional
public class MachineController {

    private final MachineRepository repository;
    private final AuditService auditService;

    public MachineController(MachineRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<MachineResponse> list() {
        return repository.findAll(Sort.by("code")).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponse create(@Valid @RequestBody MachineRequest request, @AuthenticationPrincipal UserDetails user) {
        Machine machine = repository.save(apply(new Machine(), request));
        auditService.log(user.getUsername(), "MACHINE_CREATED", "MACHINE", machine.getId(), machine.getCode());
        return toResponse(machine);
    }

    @PutMapping("/{id}")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineRequest request, @AuthenticationPrincipal UserDetails user) {
        Machine machine = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
        apply(machine, request);
        auditService.log(user.getUsername(), "MACHINE_UPDATED", "MACHINE", id, machine.getCode());
        return toResponse(machine);
    }

    private Machine apply(Machine machine, MachineRequest request) {
        machine.setCode(request.code());
        machine.setName(request.name());
        machine.setStation(request.station());
        machine.setLocation(request.location());
        machine.setActive(request.active());
        return machine;
    }

    private MachineResponse toResponse(Machine machine) {
        return new MachineResponse(machine.getId(), machine.getCode(), machine.getName(),
                machine.getStation(), machine.getLocation(), machine.isActive());
    }
}
