package com.traceintel.controller;

import com.traceintel.dto.dashboard.DashboardStatsResponse;
import com.traceintel.dto.dashboard.ScanActivityResponse;
import com.traceintel.dto.report.Count;
import com.traceintel.dto.scan.ScanEventResponse;
import com.traceintel.entity.ScanEvent;
import com.traceintel.enums.HoldStatus;
import com.traceintel.enums.LotStatus;
import com.traceintel.enums.ScanResult;
import com.traceintel.repository.MaterialLotRepository;
import com.traceintel.repository.ProductionBatchRepository;
import com.traceintel.repository.QualityHoldRepository;
import com.traceintel.repository.ScanEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductionBatchRepository batchRepository;
    private final ScanEventRepository scanEventRepository;
    private final QualityHoldRepository holdRepository;
    private final MaterialLotRepository lotRepository;

    public DashboardController(
            ProductionBatchRepository batchRepository,
            ScanEventRepository scanEventRepository,
            QualityHoldRepository holdRepository,
            MaterialLotRepository lotRepository
    ) {
        this.batchRepository = batchRepository;
        this.scanEventRepository = scanEventRepository;
        this.holdRepository = holdRepository;
        this.lotRepository = lotRepository;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        return new DashboardStatsResponse(
                batchRepository.count(),
                scanEventRepository.countByScannedAtGreaterThanEqual(today),
                scanEventRepository.countByResultAndScannedAtGreaterThanEqual(ScanResult.BLOCKED, today),
                lotRepository.countByStatus(LotStatus.ON_HOLD),
                holdRepository.countByStatus(HoldStatus.REQUESTED)
        );
    }

    @GetMapping("/scan-activity")
    public List<ScanActivityResponse> scanActivity() {
        LocalDate firstDay = LocalDate.now().minusDays(6);
        Map<LocalDate, List<ScanEvent>> eventsByDate = scanEventRepository.findByScannedAtGreaterThanEqual(firstDay.atStartOfDay()).stream()
                .collect(Collectors.groupingBy(event -> event.getScannedAt().toLocalDate()));
        return Stream.iterate(firstDay, date -> date.plusDays(1))
                .limit(7)
                .map(date -> {
                    List<ScanEvent> events = eventsByDate.getOrDefault(date, List.of());
                    long accepted = events.stream().filter(event -> event.getResult() == ScanResult.ACCEPTED).count();
                    return new ScanActivityResponse(date, accepted, events.size() - accepted);
                })
                .toList();
    }

    @GetMapping("/block-reasons")
    public List<Count> blockReasons() {
        return scanEventRepository.countBlockReasons();
    }

    @GetMapping("/recent-scans")
    public List<ScanEventResponse> recentScans() {
        return scanEventRepository.findTop20ByOrderByScannedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private ScanEventResponse toResponse(ScanEvent event) {
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
}
