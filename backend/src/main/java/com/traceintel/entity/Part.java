package com.traceintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parts")
public class Part extends BaseEntity {
    @Column(name = "part_number", nullable = false, unique = true, length = 50)
    private String partNumber;
    @Column(nullable = false, length = 150)
    private String name;
    private String description;
    @Column(name = "cost_per_part", precision = 12, scale = 2)
    private BigDecimal costPerPart = BigDecimal.ZERO;
    @Column(name = "parts_per_vehicle", nullable = false)
    private int partsPerVehicle = 1;
    @Column(name = "scrap_cost", precision = 12, scale = 2)
    private BigDecimal scrapCost = BigDecimal.ZERO;
    @Column(name = "rework_cost", precision = 12, scale = 2)
    private BigDecimal reworkCost = BigDecimal.ZERO;
}
