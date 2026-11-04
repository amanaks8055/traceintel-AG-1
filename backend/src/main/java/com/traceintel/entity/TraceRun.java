package com.traceintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trace_runs")
public class TraceRun extends IdentifiableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private MaterialLot lot;
    @Column(nullable = false, length = 10)
    private String direction = "FORWARD";
    @Column(name = "input_value", length = 100)
    private String inputValue;
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;
    @Column(name = "duration_ms", nullable = false)
    private long durationMs;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "executed_by", nullable = false)
    private User executedBy;
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
