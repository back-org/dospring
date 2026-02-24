package dospring.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * JPA converter that encrypts/decrypts String values using AES-256-GCM.
 *
 * <p>Design notes:
 * <ul>
 *   <li>AES-GCM provides confidentiality + integrity.</li>
 *   <li>We generate a random 12-byte IV per value (recommended for GCM).</li>
 *   <li>We store: base64( IV || ciphertext+tag ).</li>
 *   <li>If encryption key is not configured, the converter becomes a no-op (dev-friendly).</li>
 * </ul>
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

  private static final String PREFIX = "enc:";
  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;

  private static volatile SecretKey SECRET_KEY; // set by CryptoKeyProvider
  private static volatile boolean ENABLED = false;

  private static final SecureRandom RNG = new SecureRandom();

  static void enableEncryption(SecretKey key) {
    SECRET_KEY = key;
    ENABLED = true;
  }

  static void disableEncryption() {
    SECRET_KEY = null;
    ENABLED = false;
  }

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (!ENABLED || attribute == null || attribute.isBlank()) {
      return attribute;
    }

    try {
      byte[] iv = new byte[IV_LEN];
      RNG.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(TAG_BITS, iv));
      byte[] ct = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

      ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
      bb.put(iv);
      bb.put(ct);

      return PREFIX + Base64.getEncoder().encodeToString(bb.array());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt value", e);
    }
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (!ENABLED || dbData == null || dbData.isBlank()) {
      return dbData;
    }

    // Backward compatibility: only decrypt values that we encrypted.
    if (!dbData.startsWith(PREFIX)) {
      return dbData;
    }

    try {
      byte[] raw = Base64.getDecoder().decode(dbData.substring(PREFIX.length()));
      ByteBuffer bb = ByteBuffer.wrap(raw);

      byte[] iv = new byte[IV_LEN];
      bb.get(iv);
      byte[] ct = new byte[bb.remaining()];
      bb.get(ct);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(TAG_BITS, iv));
      byte[] pt = cipher.doFinal(ct);

      return new String(pt, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to decrypt value", e);
    }
  }
}
