package com.java.dospring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.constants.ERole;
import com.java.dospring.model.Role;

/**
 * RoleRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
