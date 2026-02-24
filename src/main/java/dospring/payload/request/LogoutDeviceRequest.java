package dospring.payload.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LogoutDeviceRequest.
 *
 * <p>Enterprise V4+ documentation block.
 */
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
