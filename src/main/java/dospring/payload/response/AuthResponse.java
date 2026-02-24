package dospring.payload.response;

import java.util.List;

/**
 * Authentication response (Access + Refresh).
 */
public class AuthResponse {

  private String tokenType = "Bearer";
  private String accessToken;
  private long accessTokenExpiresInSeconds;
  private String refreshToken;
  private List<String> roles;
  private String username;

  public AuthResponse() {}

  public AuthResponse(String accessToken, long accessTokenExpiresInSeconds, String refreshToken,
                      String username, List<String> roles) {
    this.accessToken = accessToken;
    this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
    this.refreshToken = refreshToken;
    this.username = username;
    this.roles = roles;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getAccessTokenExpiresInSeconds() {
    return accessTokenExpiresInSeconds;
  }

  public void setAccessTokenExpiresInSeconds(long accessTokenExpiresInSeconds) {
    this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
