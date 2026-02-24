package com.java.dospring.payload.request;

import jakarta.validation.constraints.NotBlank;

public class LogoutDeviceRequest {

  @NotBlank
  private String deviceId;

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
}
