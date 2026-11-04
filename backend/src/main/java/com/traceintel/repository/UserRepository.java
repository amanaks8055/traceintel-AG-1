package com.traceintel.repository;

import com.traceintel.entity.User;
import com.traceintel.enums.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "role")
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRoleNameInAndActiveTrue(List<RoleType> roles);
    long countByRoleNameAndActiveTrue(RoleType role);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findFirstByEmailIgnoreCase(String email);
}
