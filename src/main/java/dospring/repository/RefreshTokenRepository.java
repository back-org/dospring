package com.java.dospring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);
  List<RefreshToken> findAllByUserId(Long userId);
  List<RefreshToken> findAllByUserIdAndDeviceId(Long userId, String deviceId);
}
