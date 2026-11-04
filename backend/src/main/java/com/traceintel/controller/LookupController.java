package com.traceintel.controller;

import com.traceintel.dto.master.Option;
import com.traceintel.repository.CustomerRepository;
import com.traceintel.repository.MachineRepository;
import com.traceintel.repository.PartRepository;
import com.traceintel.repository.SupplierRepository;
import com.traceintel.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
@Transactional(readOnly = true)
public class LookupController {

    private final PartRepository partRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    public LookupController(
            PartRepository partRepository,
            SupplierRepository supplierRepository,
            CustomerRepository customerRepository,
            MachineRepository machineRepository,
            UserRepository userRepository
    ) {
        this.partRepository = partRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/parts")
    public List<Option> parts() {
        return partRepository.findAll(Sort.by("partNumber")).stream()
                .map(part -> new Option(part.getId(), part.getPartNumber() + " - " + part.getName())).toList();
    }

    @GetMapping("/suppliers")
    public List<Option> suppliers() {
        return supplierRepository.findAll(Sort.by("name")).stream()
                .map(supplier -> new Option(supplier.getId(), supplier.getName() + " (" + supplier.getCode() + ")")).toList();
    }

    @GetMapping("/customers")
    public List<Option> customers() {
        return customerRepository.findAll(Sort.by("name")).stream()
                .map(customer -> new Option(customer.getId(), customer.getName())).toList();
    }

    @GetMapping("/machines")
    public List<Option> machines() {
        return machineRepository.findAll(Sort.by("code")).stream()
                .filter(machine -> machine.isActive())
                .map(machine -> new Option(machine.getId(), machine.getCode() + " - " + machine.getName())).toList();
    }

    @GetMapping("/operators")
    public List<Option> operators() {
        return userRepository.findAll(Sort.by("fullName")).stream()
                .filter(user -> user.isActive())
                .map(user -> new Option(user.getId(), user.getFullName())).toList();
    }
}
