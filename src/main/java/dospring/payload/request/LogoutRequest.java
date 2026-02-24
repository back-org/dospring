package dospring.payload.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LogoutRequest.
 *
 * <p>Enterprise V4+ documentation block.
 */
public class LogoutRequest {
  @NotBlank
  private String refreshToken;

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
