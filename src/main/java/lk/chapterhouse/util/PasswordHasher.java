package lk.chapterhouse.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2WithHmacSHA256 password hashing.
 * Format stored: pbkdf2_sha256$iterations$saltB64$hashB64
 */
public class PasswordHasher {
    public static final String ALGORITHM = "pbkdf2_sha256";
    public static final int ITERATIONS = 210_000;
    public static final int SALT_BYTES = 16;
    public static final int KEY_BYTES = 32;
    public static final int MAX_PASSWORD_CHARS = 128;

    private final SecureRandom random = new SecureRandom();

    public String hash(char[] password) {
        validateLength(password);
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return ALGORITHM + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derived);
    }

    public boolean matches(char[] password, String stored) {
        if (stored == null || stored.isBlank()) {
            return false;
        }
        String[] parts = stored.split("\\$", -1);
        if (parts.length != 4 || !ALGORITHM.equals(parts[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = derive(password, salt, iterations);
        return MessageDigest.isEqual(expected, actual);
    }

    private static void validateLength(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length > MAX_PASSWORD_CHARS) {
            throw new IllegalArgumentException("Password is too long (max " + MAX_PASSWORD_CHARS + ").");
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BYTES * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Password hashing unavailable.", ex);
        }
    }
}
