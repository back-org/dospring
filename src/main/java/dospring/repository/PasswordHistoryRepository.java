package com.java.dospring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.model.PasswordHistory;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
  List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
