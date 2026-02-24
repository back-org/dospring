package dospring.repository;

import java.util.List;
import java.util.Optional;

import java.time.Instant;

import dospring.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



/**
 * RefreshTokenRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);
  List<RefreshToken> findAllByUserId(Long userId);
  List<RefreshToken> findAllByUserIdAndDeviceId(Long userId, String deviceId);

  /**
   * Lists non-revoked, non-expired refresh tokens (active sessions).
   */
  @Query("select rt from RefreshToken rt where rt.user.id = :userId and rt.revokedAt is null and rt.expiresAt > :now")
  List<RefreshToken> findActiveSessions(@Param("userId") Long userId, @Param("now") Instant now);
}
