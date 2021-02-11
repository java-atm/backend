package com.utils.security;

import com.utils.exceptions.security_exceptions.InvalidSaltException;
import com.utils.exceptions.security_exceptions.PasswordHashingFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;


public class PasswordHashingClient {
    private static final Logger LOGGER = LogManager.getLogger(PasswordHashingClient.class);
    private static final int SALT_LENGTH = 64;
    private static final int ITERATION_COUNT = SALT_LENGTH * 1024;
    private static final int KEY_LENGTH = SALT_LENGTH;
    private static final int KEY_LENGTH_IN_BITES = KEY_LENGTH * 8;
    private static final String SECRET_FACTORY_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public static String hashThePassword(byte[] salt, String password) throws PasswordHashingFailedException {
        LOGGER.info("Starting to hash the given password");
        if (salt.length != SALT_LENGTH) {
            LOGGER.error("Salt length is not valid: {}", salt.length);
            throw new InvalidSaltException("Salt length is not valid: " + salt.length);
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_IN_BITES);
        try {
            LOGGER.info("Generating secret factory.");
            SecretKeyFactory f = SecretKeyFactory.getInstance(SECRET_FACTORY_ALGORITHM_NAME);
            LOGGER.info("Generating hash");
            byte[] hash = f.generateSecret(spec).getEncoded();
            LOGGER.info("Hash generated");
            return BASE64_ENCODER.encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Failed to create the hash: {}", e.getMessage(), e);
            throw new PasswordHashingFailedException("Failed to hash the given password using salt: " + BASE64_ENCODER.encodeToString(salt));
        }
    }

    public static byte[] getRandomSalt() {
        LOGGER.info("Generating a random salt");
        byte[] salt = new byte[SALT_LENGTH];
        long timestamp = System.currentTimeMillis();
        LOGGER.info("Random seed: {}", timestamp);

        Random random = new Random(timestamp);
        random.nextBytes(salt);
        LOGGER.info("Salt generated");
        return salt;
    }

    public static void main(String[] args) throws PasswordHashingFailedException {
        byte[] salt = getRandomSalt();
        System.out.printf("salt: %s%n", BASE64_ENCODER.encodeToString(salt));
        String hash = hashThePassword(salt, "9999");
        System.out.println("hash: " + hash);
    }
}
