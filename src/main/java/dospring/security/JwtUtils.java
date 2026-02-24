package dospring.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT helper (Access token only).
 *
 * Refresh tokens are handled via DB-backed opaque tokens (hashed) for revocation + rotation.
 */
@Component
public class JwtUtils {

  private final SecretKey key;
  private final String issuer;
  private final long accessTtlSeconds;

  public JwtUtils(@Value("${app.jwt.secret}") String secret,
                  @Value("${app.jwt.issuer}") String issuer,
                  @Value("${app.jwt.access-ttl-seconds}") long accessTtlSeconds) {

    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.accessTtlSeconds = accessTtlSeconds;
  }

  public String generateAccessToken(String username, List<String> roles) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(accessTtlSeconds);

    return Jwts.builder()
        .subject(username)
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("roles", roles)
        .claim("typ", "access")
        .signWith(key)
        .compact();
  }

  public Claims parseClaims(String token) throws JwtException {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public boolean validateAccessToken(String token) {
    try {
      Claims claims = parseClaims(token);
      return "access".equals(claims.get("typ", String.class));
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getUsername(String token) {
    return parseClaims(token).getSubject();
  }
}
