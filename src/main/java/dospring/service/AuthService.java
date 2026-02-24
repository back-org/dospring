package com.java.dospring.service;

import com.java.dospring.constants.ERole;
import com.java.dospring.model.PasswordHistory;
import com.java.dospring.model.RefreshToken;
import com.java.dospring.model.Role;
import com.java.dospring.model.User;
import com.java.dospring.payload.request.LoginRequest;
import com.java.dospring.payload.request.RefreshRequest;
import com.java.dospring.payload.request.SignUpRequest;
import com.java.dospring.payload.response.AuthResponse;
import com.java.dospring.payload.response.SessionInfoResponse;
import com.java.dospring.repository.PasswordHistoryRepository;
import com.java.dospring.repository.RefreshTokenRepository;
import com.java.dospring.repository.RoleRepository;
import com.java.dospring.repository.UserRepository;
import com.java.dospring.security.jwt.JwtUtils;
import com.java.dospring.security.service.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enterprise auth service:
 * - Access JWT + DB-backed refresh tokens (hashed) with rotation/revocation
 * - Brute-force mitigation (lock account after N failures)
 * - Password policy + password reuse prevention (last N)
 */
@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordHistoryRepository passwordHistoryRepository;

  private final long accessTtlSeconds;
  private final long refreshTtlSeconds;
  private final int maxFailedAttempts;
  private final int lockMinutes;
  private final int pwdMinLength;
  private final int pwdHistory;

  private final SecureRandom secureRandom = new SecureRandom();

  public AuthService(AuthenticationManager authenticationManager,
                     UserRepository userRepository,
                     RoleRepository roleRepository,
                     PasswordEncoder passwordEncoder,
                     JwtUtils jwtUtils,
                     RefreshTokenRepository refreshTokenRepository,
                     PasswordHistoryRepository passwordHistoryRepository,
                     @Value("${app.jwt.access-ttl-seconds}") long accessTtlSeconds,
                     @Value("${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
                     @Value("${app.auth.max-failed-attempts}") int maxFailedAttempts,
                     @Value("${app.auth.lock-minutes}") int lockMinutes,
                     @Value("${app.auth.password.min-length}") int pwdMinLength,
                     @Value("${app.auth.password.history}") int pwdHistory) {

    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordHistoryRepository = passwordHistoryRepository;

    this.accessTtlSeconds = accessTtlSeconds;
    this.refreshTtlSeconds = refreshTtlSeconds;
    this.maxFailedAttempts = maxFailedAttempts;
    this.lockMinutes = lockMinutes;
    this.pwdMinLength = pwdMinLength;
    this.pwdHistory = pwdHistory;
  }

  @Transactional
  public void register(SignUpRequest req) {
    if (userRepository.existsByUsername(req.getUsername())) {
      throw new IllegalArgumentException("Username is already taken");
    }
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    validatePassword(req.getPassword());

    User user = User.builder()
        .username(req.getUsername())
        .email(req.getEmail())
        .password(passwordEncoder.encode(req.getPassword()))
        .passwordChangedAt(Instant.now())
        .build();

    Set<String> strRoles = req.getRoles();
    if (strRoles == null || strRoles.isEmpty()) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ROLE_USER).build()));
      user.getRoles().add(userRole);
    } else {
      for (String role : strRoles) {
        ERole er = switch (role.toLowerCase()) {
          case "admin" -> ERole.ROLE_ADMIN;
          case "attendee" -> ERole.ROLE_ATTENDEE;
          default -> ERole.ROLE_USER;
        };
        Role r = roleRepository.findByName(er)
            .orElseGet(() -> roleRepository.save(Role.builder().name(er).build()));
        user.getRoles().add(r);
      }
    }

    userRepository.save(user);

    // save password history entry
    passwordHistoryRepository.save(PasswordHistory.builder()
        .user(user)
        .passwordHash(user.getPassword())
        .build());
  }

  @Transactional
  public AuthResponse login(LoginRequest loginRequest, String deviceId, HttpServletRequest httpRequest) {
    User user = userRepository.findByUsername(loginRequest.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    Instant now = Instant.now();
    if (!user.isEnabled()) {
      throw new BadCredentialsException("Account disabled");
    }
    if (user.isLockedNow(now)) {
      throw new BadCredentialsException("Account locked until " + user.getLockUntil());
    }

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
      );

      // success: reset lock counters
      user.setFailedLoginAttempts(0);
      user.setLockUntil(null);
      user.setLastLoginAt(now);
      userRepository.save(user);

      UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles = principal.getAuthorities().stream()
          .map(a -> a.getAuthority())
          .sorted(Comparator.naturalOrder())
          .collect(Collectors.toList());

      String accessToken = jwtUtils.generateAccessToken(principal.getUsername(), roles);
      String refreshToken = mintRefreshToken(user, deviceId, httpRequest);

      return new AuthResponse(accessToken, accessTtlSeconds, refreshToken, principal.getUsername(), roles);

    } catch (Exception ex) {
      // failed: increment attempts
      int attempts = user.getFailedLoginAttempts() + 1;
      user.setFailedLoginAttempts(attempts);
      if (attempts >= maxFailedAttempts) {
        user.setLockUntil(now.plusSeconds(lockMinutes * 60L));
      }
      userRepository.save(user);
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
    String tokenHash = sha256Hex(request.getRefreshToken());
    RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    User user = existing.getUser();

    Instant now = Instant.now();
    if (existing.isRevoked() || existing.isExpired(now)) {
      throw new BadCredentialsException("Refresh token expired/revoked");
    }

    // device binding best-effort (optional)
    if (request.getDeviceId() != null && existing.getDeviceId() != null &&
        !request.getDeviceId().equals(existing.getDeviceId())) {
      throw new BadCredentialsException("Device mismatch");
    }

    // rotation: revoke old
    existing.setRevokedAt(now);
    existing.setLastUsedAt(now);
    refreshTokenRepository.save(existing);

    List<String> roles = user.getRoles().stream()
        .map(r -> r.getName().name())
        .sorted(Comparator.naturalOrder())
        .collect(Collectors.toList());

    String accessToken = jwtUtils.generateAccessToken(user.getUsername(), roles);
    String newRefresh = mintRefreshToken(user, request.getDeviceId(), httpRequest);

    return new AuthResponse(accessToken, accessTtlSeconds, newRefresh, user.getUsername(), roles);
  }

  @Transactional
  public void logout(String refreshToken) {
    String tokenHash = sha256Hex(refreshToken);
    refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
      if (rt.getRevokedAt() == null) {
        rt.setRevokedAt(Instant.now());
        refreshTokenRepository.save(rt);
      }
    });
  }

  @Transactional
  public void logoutAll(Long userId) {
    Instant now = Instant.now();
    List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
    for (RefreshToken t : tokens) {
      if (t.getRevokedAt() == null) {
        t.setRevokedAt(now);
      }
    }
    refreshTokenRepository.saveAll(tokens);
  }

  @Transactional
  public void logoutDevice(Long userId, String deviceId) {
    Instant now = Instant.now();
    List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdAndDeviceId(userId, deviceId);
    for (RefreshToken t : tokens) {
      if (t.getRevokedAt() == null) {
        t.setRevokedAt(now);
      }
    }
    refreshTokenRepository.saveAll(tokens);
  }

  /**
   * Lists active refresh-token sessions for the current user.
   *
   * <p>Never returns refresh token values.
   */
  @Transactional(readOnly = true)
  public List<SessionInfoResponse> listActiveSessions(Long userId) {
    Instant now = Instant.now();
    List<RefreshToken> sessions = refreshTokenRepository.findActiveSessions(userId, now);
    List<SessionInfoResponse> out = new ArrayList<>(sessions.size());
    for (RefreshToken rt : sessions) {
      out.add(new SessionInfoResponse(
          rt.getId(),
          rt.getDeviceId(),
          rt.getIpAddress(),
          rt.getUserAgent(),
          rt.getCreatedAt(),
          rt.getLastUsedAt(),
          rt.getExpiresAt()
      ));
    }
    return out;
  }

  /**
   * Revokes a refresh-token session record by its id.
   */
  @Transactional
  public void revokeSession(Long userId, Long sessionId) {
    RefreshToken rt = refreshTokenRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    if (!rt.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Session does not belong to current user");
    }
    if (rt.getRevokedAt() == null) {
      rt.setRevokedAt(Instant.now());
      refreshTokenRepository.save(rt);
    }
  }

  /**
   * Changes the user's password with policy + history enforcement.
   *
   * <p>Security: revokes all refresh tokens after successful change.
   */
  @Transactional
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new BadCredentialsException("Current password is invalid");
    }

    validatePassword(newPassword);
    enforcePasswordHistory(userId, newPassword);

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordChangedAt(Instant.now());
    userRepository.save(user);

    passwordHistoryRepository.save(PasswordHistory.builder()
        .user(user)
        .passwordHash(user.getPassword())
        .build());

    // Force re-login on all devices.
    logoutAll(userId);
  }

  private String mintRefreshToken(User user, String deviceId, HttpServletRequest request) {
    String token = randomToken();
    RefreshToken rt = RefreshToken.builder()
        .user(user)
        .tokenHash(sha256Hex(token))
        .deviceId(deviceId)
        .userAgent(trim(request.getHeader("User-Agent"), 300))
        .ipAddress(trim(request.getRemoteAddr(), 60))
        .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
        .lastUsedAt(Instant.now())
        .build();
    refreshTokenRepository.save(rt);
    return token;
  }

  private String randomToken() {
    byte[] bytes = new byte[64];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256Hex(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String trim(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  private void validatePassword(String password) {
    if (password == null || password.length() < pwdMinLength) {
      throw new IllegalArgumentException("Password must be at least " + pwdMinLength + " characters");
    }
    if (password.contains(" ")) {
      throw new IllegalArgumentException("Password must not contain spaces");
    }
    boolean upper = password.chars().anyMatch(Character::isUpperCase);
    boolean lower = password.chars().anyMatch(Character::isLowerCase);
    boolean digit = password.chars().anyMatch(Character::isDigit);
    boolean special = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    if (!(upper && lower && digit && special)) {
      throw new IllegalArgumentException("Password must contain upper, lower, digit and special character");
    }

    // very small blocklist (extend in production)
    List<String> common = List.of("password", "12345678", "qwerty", "admin", "letmein");
    String low = password.toLowerCase();
    if (common.stream().anyMatch(low::contains)) {
      throw new IllegalArgumentException("Password too common");
    }
  }

  /**
   * Prevents re-using the last N password hashes.
   */
  private void enforcePasswordHistory(Long userId, String newPasswordRaw) {
    // Repository returns last 5; property pwdHistory kept for future extension.
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    for (PasswordHistory h : history) {
      if (passwordEncoder.matches(newPasswordRaw, h.getPasswordHash())) {
        throw new IllegalArgumentException("Password was used recently. Choose a new one.");
      }
    }
  }
}
