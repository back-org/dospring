package com.java.dospring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.model.User;

/**
 * UserRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
  Boolean existsByEmail(String email);
}
