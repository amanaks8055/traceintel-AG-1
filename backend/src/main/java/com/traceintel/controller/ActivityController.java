package com.traceintel.controller;

import com.traceintel.dto.scan.ScanEventResponse;
import com.traceintel.dto.trace.TraceRunResponse;
import com.traceintel.entity.ScanEvent;
import com.traceintel.entity.TraceRun;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.ScanEventRepository;
import com.traceintel.repository.TraceRunRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ScanEventRepository scanEventRepository;
    private final TraceRunRepository traceRunRepository;

    public ActivityController(
            ScanEventRepository scanEventRepository,
            TraceRunRepository traceRunRepository
    ) {
        this.scanEventRepository = scanEventRepository;
        this.traceRunRepository = traceRunRepository;
    }

    @GetMapping("/scan/events")
    public List<ScanEventResponse> scanEvents() {
        return scanEventRepository.findTop20ByOrderByScannedAtDesc().stream()
                .map(this::toScanResponse)
                .toList();
    }

    @GetMapping("/scan/events/{id}")
    public ScanEventResponse scanEvent(@PathVariable Long id) {
        return scanEventRepository.findById(id)
                .map(this::toScanResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Scan event not found: " + id));
    }

    @GetMapping("/trace/history")
    public List<TraceRunResponse> traceHistory() {
        return traceRunRepository.findAll().stream()
                .map(this::toTraceResponse)
                .toList();
    }

    private ScanEventResponse toScanResponse(ScanEvent event) {
        return new ScanEventResponse(
                event.getId(),
                event.getSerialInput(),
                event.getSerialNumber() == null ? null : event.getSerialNumber().getPart().getName(),
                event.getBatch() == null ? null : event.getBatch().getBatchNumber(),
                event.getOperator().getFullName(),
                event.getStation(),
                event.getResult(),
                event.getReason(),
                event.isManualEntry(),
                event.getScannedAt()
        );
    }

    private TraceRunResponse toTraceResponse(TraceRun traceRun) {
        return new TraceRunResponse(
                traceRun.getId(),
                traceRun.getDirection(),
                traceRun.getInputValue(),
                traceRun.getLot() == null ? null : traceRun.getLot().getId(),
                traceRun.getLot() == null ? null : traceRun.getLot().getLotNumber(),
                traceRun.getDurationMs(),
                traceRun.getExecutedBy().getFullName(),
                traceRun.getExecutedAt()
        );
    }
}
