package com.traceintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    @Column(length = 30)
    private String phone;
    private String address;
}
