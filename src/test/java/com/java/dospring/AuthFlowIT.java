package com.java.dospring;

import com.java.dospring.payload.request.LoginRequest;
import com.java.dospring.payload.request.RefreshRequest;
import com.java.dospring.payload.request.SignUpRequest;
import com.java.dospring.payload.response.AuthResponse;
import com.java.dospring.payload.response.MessageResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("dospring")
      .withUsername("dospring")
      .withPassword("dospring");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.profiles.active", () -> "prod");
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("app.jwt.secret", () -> "change-me-in-env-please-change-me-in-env-please");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  @LocalServerPort
  int port;

  @Autowired
  TestRestTemplate rest;

  @Test
  void fullAuthFlow_register_login_refresh_logoutAll() {
    String base = "http://localhost:" + port;

    // Register
    SignUpRequest signUp = new SignUpRequest();
    signUp.setUsername("user1");
    signUp.setEmail("user1@mail.com");
    signUp.setPassword("StrongPwd#1234");

    ResponseEntity<MessageResponse> reg = rest.postForEntity(base + "/api/auth/register", signUp, MessageResponse.class);
    assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Login
    LoginRequest login = new LoginRequest();
    login.setUsername("user1");
    login.setPassword("StrongPwd#1234");

    HttpHeaders loginHeaders = new HttpHeaders();
    loginHeaders.add("X-Device-Id", "laptop-1");

    ResponseEntity<AuthResponse> loginResp = rest.exchange(
        base + "/api/auth/login",
        HttpMethod.POST,
        new HttpEntity<>(login, loginHeaders),
        AuthResponse.class
    );

    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResp.getBody()).isNotNull();
    String access = loginResp.getBody().getAccessToken();
    String refresh = loginResp.getBody().getRefreshToken();
    assertThat(access).isNotBlank();
    assertThat(refresh).isNotBlank();

    // Refresh
    RefreshRequest rr = new RefreshRequest();
    rr.setRefreshToken(refresh);
    rr.setDeviceId("laptop-1");

    ResponseEntity<AuthResponse> refreshed = rest.postForEntity(base + "/api/auth/refresh", rr, AuthResponse.class);
    assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(refreshed.getBody()).isNotNull();
    String refresh2 = refreshed.getBody().getRefreshToken();
    assertThat(refresh2).isNotBlank();
    assertThat(refresh2).isNotEqualTo(refresh);

    // Logout all
    HttpHeaders h = new HttpHeaders();
    h.setBearerAuth(access);

    ResponseEntity<MessageResponse> logoutAll = rest.exchange(
        base + "/api/auth/logout-all",
        HttpMethod.POST,
        new HttpEntity<>(null, h),
        MessageResponse.class
    );
    assertThat(logoutAll.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Old refresh2 should now fail
    RefreshRequest rr2 = new RefreshRequest();
    rr2.setRefreshToken(refresh2);
    rr2.setDeviceId("laptop-1");

    ResponseEntity<String> shouldFail = rest.postForEntity(base + "/api/auth/refresh", rr2, String.class);
    assertThat(shouldFail.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
  }
}
