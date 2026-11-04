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

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment extends BaseEntity {
    @Column(nullable = false)
    private boolean demo;
    @Column(name = "shipment_number", nullable = false, unique = true, length = 50)
    private String shipmentNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "shipped_date", nullable = false)
    private LocalDate shippedDate;
    private String notes;
}
