package com.traceintel.entity;

import com.traceintel.enums.LotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "material_lots")
public class MaterialLot extends BaseEntity {
    @Column(nullable = false)
    private boolean demo;
    @Column(name = "lot_number", nullable = false, unique = true, length = 50)
    private String lotNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;
    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotStatus status = LotStatus.OK;
    @Column(precision = 12, scale = 2)
    private BigDecimal quantity;
    @Column(length = 20)
    private String unit = "pcs";

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
