package com.traceintel.controller;

import com.traceintel.dto.report.Count;
import com.traceintel.dto.report.ReportResponse;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Transactional(readOnly = true)
public class ReportController {

    private final EntityManager entityManager;

    public ReportController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping("/summary")
    public ReportResponse summary() {
        return new ReportResponse(
                counts("select e.reason, count(e) from ScanEvent e where e.reason is not null group by e.reason order by count(e) desc"),
                counts("select cast(l.status as string), count(l) from MaterialLot l group by l.status"),
                counts("select cast(h.status as string), count(h) from QualityHold h group by h.status"),
                counts("select i.result, count(i) from QualityInspection i group by i.result"),
                counts("select s.customer.name, count(s) from Shipment s group by s.customer.name order by count(s) desc")
        );
    }

    private List<Count> counts(String query) {
        return entityManager.createQuery(query, Object[].class).getResultList().stream()
                .map(row -> new Count(String.valueOf(row[0]), (Long) row[1]))
                .toList();
    }
}
