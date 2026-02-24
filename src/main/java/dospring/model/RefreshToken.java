package com.java.dospring.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

/**
 * Refresh token stored as SHA-256 hash (never store refresh token in cleartext).
 * Supports multi-device sessions and revocation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens", uniqueConstraints = {
    @UniqueConstraint(name = "uk_refresh_tokens_hash", columnNames = "token_hash")
}, indexes = {
    @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_device", columnList = "device_id")
})
public class RefreshToken extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
  private User user;

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash; // hex sha-256 (64 chars)

  @Column(name = "device_id", length = 120)
  private String deviceId;

  @Column(name = "user_agent", length = 300)
  private String userAgent;

  @Column(name = "ip_address", length = 60)
  private String ipAddress;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  public boolean isExpired(Instant now) {
    return expiresAt.isBefore(now);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }
}
