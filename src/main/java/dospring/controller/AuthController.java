package com.java.dospring.controller;

import com.java.dospring.payload.request.LoginRequest;
import com.java.dospring.payload.request.LogoutDeviceRequest;
import com.java.dospring.payload.request.LogoutRequest;
import com.java.dospring.payload.request.RefreshRequest;
import com.java.dospring.payload.request.SignUpRequest;
import com.java.dospring.payload.response.AuthResponse;
import com.java.dospring.payload.response.MessageResponse;
import com.java.dospring.security.service.UserDetailsImpl;
import com.java.dospring.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
