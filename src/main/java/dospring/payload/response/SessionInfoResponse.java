package com.java.dospring.payload.response;

import java.time.Instant;

/**
 * Minimal information about an authenticated session (refresh token record).
 *
 * <p>No refresh token value is ever returned by the API.
 */
public class SessionInfoResponse {

  private Long id;
  private String deviceId;
  private String ipAddress;
  private String userAgent;
  private Instant createdAt;
  private Instant lastUsedAt;
  private Instant expiresAt;

  public SessionInfoResponse() {
  }

  public SessionInfoResponse(Long id, String deviceId, String ipAddress, String userAgent,
                             Instant createdAt, Instant lastUsedAt, Instant expiresAt) {
    this.id = id;
    this.deviceId = deviceId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.createdAt = createdAt;
    this.lastUsedAt = lastUsedAt;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getLastUsedAt() {
    return lastUsedAt;
  }

  public void setLastUsedAt(Instant lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }
}
