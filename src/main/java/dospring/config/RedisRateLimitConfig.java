package com.java.dospring.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis-backed infrastructure for distributed rate limiting.
 *
 * <p>This configuration is optional: the application will still run without Redis.
 * When enabled, all instances of the application share the same buckets, so limits
 * are enforced globally (cluster-wide).
 *
 * <p>Enable with:
 * <pre>
 * app.redis.enabled=true
 * app.redis.host=localhost
 * app.redis.port=6379
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
public class RedisRateLimitConfig {

  @Bean(destroyMethod = "shutdown")
  public RedisClient redisClient(@Value("${app.redis.host}") String host,
                                 @Value("${app.redis.port}") int port,
                                 @Value("${app.redis.ssl:false}") boolean ssl) {
    return RedisClient.create(RedisURI.builder()
        .withHost(host)
        .withPort(port)
        .withSsl(ssl)
        .build());
  }

  @Bean(destroyMethod = "close")
  public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient client) {
    // We store bucket states as byte[] while keys are UTF-8 strings.
    return client.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
  }

  @Bean
  public ProxyManager<String> proxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
    // Expiration strategy: remove buckets after they would naturally refill to max.
    return LettuceBasedProxyManager.builderFor(redisConnection)
        .withExpirationStrategy(
            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
        .build();
  }

  /**
   * Example shared bucket configuration bean.
   *
   * <p>We keep this bean for future use (e.g., annotation-based limits).
   * Current implementation configures limits inside the {@code RateLimitFilter}.
   */
  @Bean
  public Supplier<BucketConfiguration> defaultBucketConfiguration() {
    return () -> BucketConfiguration.builder()
        .addLimit(Bandwidth.simple(200, Duration.ofMinutes(1)))
        .build();
  }
}
