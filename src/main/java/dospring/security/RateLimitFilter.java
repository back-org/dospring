package com.java.dospring.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enterprise V4+ rate limiting filter.
 *
 * <p>Goals:
 * <ul>
 *   <li>Mitigate brute-force on authentication endpoints.</li>
 *   <li>Keep the app runnable in dev without Redis.</li>
 *   <li>Enable distributed limits (multi-instance) when Redis is configured.</li>
 * </ul>
 *
 * <p>Implementation strategy:
 * <ul>
 *   <li>If a {@link ProxyManager} bean is available, buckets are stored in Redis and shared across instances.</li>
 *   <li>Otherwise, we fall back to a local in-memory bucket per client key.</li>
 * </ul>
 *
 * <p>Keying:
 * <ul>
 *   <li>Default key = client IP (best-effort, proxy headers can be added in WebConfig if needed).</li>
 *   <li>We also include the endpoint group (login/refresh) so limits can differ.</li>
 * </ul>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final ProxyManager<String> proxyManager; // nullable

  /**
   * Local fallback buckets when Redis is not configured.
   *
   * <p>NOTE: local buckets are per-instance. In production you should enable Redis for global limits.
   */
  private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

  /**
   * Default limits (can be refined per endpoint):
   * - /login: 5 req/min per IP
   * - /refresh: 10 req/min per IP
   */
  private final Supplier<BucketConfiguration> loginConfig = () -> BucketConfiguration.builder()
      .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
      .build();

  private final Supplier<BucketConfiguration> refreshConfig = () -> BucketConfiguration.builder()
      .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
      .build();

  public RateLimitFilter(ObjectProvider<ProxyManager<String>> proxyManagerProvider) {
    // If Redis is not configured, no ProxyManager bean exists.
    this.proxyManager = proxyManagerProvider.getIfAvailable();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !(path.startsWith("/api/auth/login") || path.startsWith("/api/auth/refresh"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String bucketGroup = path.contains("/login") ? "login" : "refresh";

    // Best-effort client identification.
    // In enterprise setups behind reverse proxies, prefer reading X-Forwarded-For in a controlled way.
    String clientIp = request.getRemoteAddr();

    String key = "dospring:" + bucketGroup + ":" + clientIp;

    Supplier<BucketConfiguration> cfg = bucketGroup.equals("login") ? loginConfig : refreshConfig;
    Bucket bucket = resolveBucket(key, cfg);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (probe.isConsumed()) {
      filterChain.doFilter(request, response);
      return;
    }

    // RFC-friendly retry-after information.
    long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    response.getWriter().write("{\"message\":\"Too many requests. Retry later.\",\"retryAfterSeconds\":" + retryAfterSeconds + "}");
  }

  private Bucket resolveBucket(String key, Supplier<BucketConfiguration> cfg) {
    if (proxyManager != null) {
      // Distributed bucket (Redis).
      return proxyManager.builder().build(key, cfg);
    }

    // Local in-memory fallback.
    // We intentionally do not try to introspect BucketConfiguration internals to keep compatibility.
    return localBuckets.computeIfAbsent(key, k -> {
      if (k.contains(":login:")) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();
      }
      return Bucket.builder()
          .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
          .build();
    });
  }
}
