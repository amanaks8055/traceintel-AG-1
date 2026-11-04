package com.traceintel.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.traceintel.dto.vehicle.VehicleLinkRequest;
import com.traceintel.dto.vehicle.VehicleLinkResponse;
import com.traceintel.dto.vehicle.VinImportResponse;
import com.traceintel.entity.Customer;
import com.traceintel.entity.ProductionBatch;
import com.traceintel.entity.SerialNumber;
import com.traceintel.entity.VehicleLink;
import com.traceintel.exception.BadRequestException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.CustomerRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.repository.VehicleLinkRepository;
import com.traceintel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/vin")
@Transactional
public class VinController {

    private final VehicleLinkRepository linkRepository;
    private final ProductionBatchRepository batchRepository;
    private final SerialNumberRepository serialRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public VinController(
            VehicleLinkRepository linkRepository,
            ProductionBatchRepository batchRepository,
            SerialNumberRepository serialRepository,
            CustomerRepository customerRepository,
            AuditService auditService
    ) {
        this.linkRepository = linkRepository;
        this.batchRepository = batchRepository;
        this.serialRepository = serialRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @GetMapping("/links")
    public List<VehicleLinkResponse> links() {
        return linkRepository.findByBatchIsNotNullOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    @GetMapping("/unlinked")
    public List<VehicleLinkResponse> unlinked(@RequestParam(defaultValue = "") String search) {
        return linkRepository.findByBatchIsNullAndVinContainingIgnoreCaseOrderByVin(search, PageRequest.of(0, 25)).stream()
                .map(this::toResponse).toList();
    }

    @PostMapping("/links")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleLinkResponse link(@Valid @RequestBody VehicleLinkRequest request, @AuthenticationPrincipal UserDetails user) {
        SerialNumber serial = request.serialNumberId() == null ? null : serialRepository.findById(request.serialNumberId())
                .orElseThrow(() -> new ResourceNotFoundException("Serial not found: " + request.serialNumberId()));
        ProductionBatch batch = serial != null ? serial.getBatch() : request.batchId() == null ? null
                : batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + request.batchId()));
        if (batch == null) {
            throw new BadRequestException("Choose a batch or a serial");
        }
        Customer customer = request.customerId() == null ? null : customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId()));
        VehicleLink saved = save(request.vin().toUpperCase(), batch, serial, customer);
        auditService.log(user.getUsername(), "VIN_LINKED", "VEHICLE_LINK", saved.getId(), saved.getVin());
        return toResponse(saved);
    }

    @PostMapping("/import")
    public VinImportResponse importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @AuthenticationPrincipal UserDetails user
    ) {
        List<String[]> rows = read(file);
        List<String> errors = new ArrayList<>();
        int valid = 0;
        for (int index = 1; index < rows.size(); index++) {
            String[] row = rows.get(index);
            String problem = importRow(row, dryRun);
            if (problem == null) {
                valid++;
            } else {
                errors.add("Row " + (index + 1) + ": " + problem);
            }
        }
        if (!dryRun) {
            auditService.log(user.getUsername(), "VIN_IMPORTED", "VEHICLE_LINK", null, valid + " rows");
        }
        return new VinImportResponse(dryRun, valid, errors);
    }

    private String importRow(String[] row, boolean dryRun) {
        if (row.length < 4) {
            return "expected vin,batch_number,serial,customer_code";
        }
        String vin = row[0].trim().toUpperCase();
        if (vin.length() != 17) {
            return "VIN must be 17 characters";
        }
        ProductionBatch batch = batchRepository.findByBatchNumber(row[1].trim()).orElse(null);
        if (batch == null) {
            return "unknown batch " + row[1].trim();
        }
        SerialNumber serial = null;
        if (!row[2].isBlank()) {
            serial = serialRepository.findBySerial(row[2].trim()).orElse(null);
            if (serial == null) {
                return "unknown serial " + row[2].trim();
            }
        }
        Customer customer = customerRepository.findByCode(row[3].trim()).orElse(null);
        if (customer == null) {
            return "unknown customer " + row[3].trim();
        }
        if (!dryRun) {
            save(vin, batch, serial, customer);
        }
        return null;
    }

    private List<String[]> read(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.readAll();
        } catch (IOException | CsvException exception) {
            throw new BadRequestException("The file could not be read as CSV");
        }
    }

    private VehicleLink save(String vin, ProductionBatch batch, SerialNumber serial, Customer customer) {
        VehicleLink link = linkRepository.findAllByVin(vin).stream()
                .filter(existing -> existing.getBatch() == null)
                .findFirst()
                .orElseGet(VehicleLink::new);
        link.setVin(vin);
        link.setBatch(batch);
        link.setSerialNumber(serial);
        if (customer != null) {
            link.setCustomer(customer);
        }
        link.setImportedAt(LocalDateTime.now());
        return linkRepository.save(link);
    }

    private VehicleLinkResponse toResponse(VehicleLink link) {
        return new VehicleLinkResponse(link.getId(), link.getVin(),
                link.getBatch() == null ? null : link.getBatch().getBatchNumber(),
                link.getSerialNumber() == null ? null : link.getSerialNumber().getSerial(),
                link.getCustomer() == null ? null : link.getCustomer().getName(),
                link.getImportedAt());
    }
}
