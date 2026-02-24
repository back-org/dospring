package dospring.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedisRateLimitConfig {

  @Value("${app.redis.enabled:false}")
  private boolean redisEnabled;

  @Value("${spring.data.redis.url:redis://localhost:6379}")
  private String redisUrl;

  @Bean(destroyMethod = "shutdown")
  public RedisClient redisClient() {
    if (!redisEnabled) {
      log.info("Redis disabled - fallback to in-memory rate limiting");
      return null;
    }
    log.info("Initializing Redis client at {}", redisUrl);
    return RedisClient.create(redisUrl);
  }

  /**
   * IMPORTANT: Bucket4j Lettuce CAS attend byte[]/byte[].
   */
  @Bean(destroyMethod = "close")
  public StatefulRedisConnection<byte[], byte[]> redisConnectionBytes(RedisClient redisClient) {
    if (redisClient == null) return null;
    return redisClient.connect(new ByteArrayCodec());
  }

  @Bean
  public ProxyManager<byte[]> proxyManager(StatefulRedisConnection<byte[], byte[]> connection) {
    if (connection == null) return null;

    // ✅ Cette surcharge correspond exactement à Bucket4j
    return LettuceBasedProxyManager.builderFor(connection).build();
  }
}