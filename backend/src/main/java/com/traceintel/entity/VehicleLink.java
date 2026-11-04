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
@Table(name = "vehicle_links")
public class VehicleLink extends IdentifiableEntity {
    @Column(nullable = false, length = 17)
    private String vin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_number_id")
    private SerialNumber serialNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(name = "imported_at")
    private LocalDateTime importedAt;
}
