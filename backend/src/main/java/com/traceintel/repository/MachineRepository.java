package com.traceintel.repository;

import com.traceintel.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByCode(String code);
}
