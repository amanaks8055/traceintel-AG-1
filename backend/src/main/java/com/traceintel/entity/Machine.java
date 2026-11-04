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
@Table(name = "machines")
public class Machine extends BaseEntity {
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 50)
    private String station;
    @Column(length = 100)
    private String location;
    @Column(nullable = false)
    private boolean active = true;
}
