package dospring.crypto;

import jakarta.annotation.PostConstruct;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides the AES key used by {@link EncryptedStringConverter}.
 *
 * <p>Why a separate provider?
 * JPA {@code AttributeConverter}s are instantiated by JPA (not Spring), so we cannot
 * reliably {@code @Autowired} into the converter. We therefore initialize a static
 * key holder once at application startup.
 */
@Component
public class CryptoKeyProvider {

  /**
   * Base64-encoded 256-bit key (32 bytes).
   *
   * <p>Provide in environment:
   * <pre>
   * APP_CRYPTO_KEY_BASE64=...  (base64 of 32 random bytes)
   * </pre>
   */
  @Value("${app.crypto.key-base64:}")
  private String keyBase64;

  @PostConstruct
  public void init() {
    if (keyBase64 == null || keyBase64.isBlank()) {
      // In production we want deterministic encryption.
      // For local dev, encryption can be disabled by leaving key empty.
      EncryptedStringConverter.disableEncryption();
      return;
    }

    byte[] raw = Base64.getDecoder().decode(keyBase64.trim());
    if (raw.length != 32) {
      throw new IllegalStateException("app.crypto.key-base64 must decode to 32 bytes (AES-256)");
    }

    SecretKey key = new SecretKeySpec(raw, "AES");
    EncryptedStringConverter.enableEncryption(key);
  }
}
