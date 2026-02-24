package dospring.payload.request;

import jakarta.validation.constraints.NotNull;

/**
 * Revokes a specific refresh-token session by its database identifier.
 *
 * <p>This is useful for "logout this device" when you don't have the refresh token
 * (e.g., admin console or account security page).
 */
public class RevokeSessionRequest {

  @NotNull
  private Long sessionId;

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }
}
