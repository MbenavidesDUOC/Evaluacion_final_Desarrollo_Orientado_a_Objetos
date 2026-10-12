package cl.biblioteca.service;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordService {
  private PasswordService() {}

  public static String hash(String clave) {
    byte[] salt = new byte[12];
    new SecureRandom().nextBytes(salt);
    return "pbkdf2$60000$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(clave, salt));
  }

  private static byte[] derive(String clave, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), salt, 60000, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    } finally {
      spec.clearPassword();
    }
  }

  public static boolean verificar(String clave, String saved) {
    if (saved == null) return false;
    if (!saved.startsWith("pbkdf2$"))
      return MessageDigest.isEqual(
          clave.getBytes(java.nio.charset.StandardCharsets.UTF_8),
          saved.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    try {
      String[] p = saved.split("\\$");
      return p.length == 4
          && "60000".equals(p[1])
          && MessageDigest.isEqual(
              derive(clave, Base64.getDecoder().decode(p[2])), Base64.getDecoder().decode(p[3]));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
