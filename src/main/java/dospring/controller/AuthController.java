package dospring.controller;

import dospring.payload.request.*;
import dospring.payload.response.AuthResponse;
import dospring.payload.response.MessageResponse;
import dospring.payload.response.SessionInfoResponse;
import dospring.service.AuthService;
import dospring.service.impl.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authentication API (Enterprise V4).
 *
 * Endpoints:
 * - POST /api/auth/register
 * - POST /api/auth/login
 * - POST /api/auth/refresh
 * - POST /api/auth/logout
 * - POST /api/auth/logout-all
 * - POST /api/auth/logout-device
 * - GET  /api/auth/sessions
 * - POST /api/auth/revoke-session
 * - POST /api/auth/change-password
 *
 * Backward compatibility:
 * - /signin -> /login
 * - /signup -> /register
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.security.cors-origins}", maxAge = 3600)
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping({"/register", "/signup"})
  public ResponseEntity<?> register(@Valid @RequestBody SignUpRequest request) {
    authService.register(request);
    return ResponseEntity.ok(new MessageResponse("User registered successfully"));
  }

  @PostMapping({"/login", "/signin"})
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                            HttpServletRequest httpServletRequest) {
    AuthResponse resp = authService.login(request, deviceId, httpServletRequest);
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                              HttpServletRequest httpServletRequest) {
    AuthResponse resp = authService.refresh(request, httpServletRequest);
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
    authService.logout(request.getRefreshToken());
    return ResponseEntity.ok(new MessageResponse("Logged out"));
  }

  @PostMapping("/logout-all")
  public ResponseEntity<?> logoutAll(@AuthenticationPrincipal UserDetailsImpl principal) {
    authService.logoutAll(principal.getId());
    return ResponseEntity.ok(new MessageResponse("Logged out from all devices"));
  }

  @PostMapping("/logout-device")
  public ResponseEntity<?> logoutDevice(@Valid @RequestBody LogoutDeviceRequest request,
                                        @AuthenticationPrincipal UserDetailsImpl principal) {
    authService.logoutDevice(principal.getId(), request.getDeviceId());
    return ResponseEntity.ok(new MessageResponse("Logged out from device"));
  }

  /**
   * Lists active sessions for the current user.
   *
   * <p>Does not return refresh token values.
   */
  @GetMapping("/sessions")
  public ResponseEntity<List<SessionInfoResponse>> sessions(@AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(authService.listActiveSessions(principal.getId()));
  }

  /**
   * Revokes a specific session by its ID (useful for account security pages).
   */
  @PostMapping("/revoke-session")
  public ResponseEntity<?> revokeSession(@Valid @RequestBody RevokeSessionRequest request,
                                         @AuthenticationPrincipal UserDetailsImpl principal) {
    authService.revokeSession(principal.getId(), request.getSessionId());
    return ResponseEntity.ok(new MessageResponse("Session revoked"));
  }

  /**
   * Changes the user's password.
   *
   * <p>Security behavior:
   * <ul>
   *   <li>Validates current password</li>
   *   <li>Enforces password policy + history</li>
   *   <li>Revokes all refresh tokens (forces re-login on all devices)</li>
   * </ul>
   */
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                          @AuthenticationPrincipal UserDetailsImpl principal) {
    authService.changePassword(principal.getId(), request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.ok(new MessageResponse("Password changed. Please login again."));
  }
}
