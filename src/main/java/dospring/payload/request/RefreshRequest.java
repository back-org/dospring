package com.java.dospring.payload.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh request.
 * deviceId is optional but recommended to support per-device logout.
 */
public class RefreshRequest {

  @NotBlank
  private String refreshToken;

  private String deviceId;

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
}
