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
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;


public class PasswordHashingClient {
    private static final Logger LOGGER = LogManager.getLogger(PasswordHashingClient.class);
    private static final int SALT_LENGTH = 64;
    private static final int ITERATION_COUNT = SALT_LENGTH * 1024;
    private static final int KEY_LENGTH = SALT_LENGTH;
    private static final int KEY_LENGTH_IN_BITES = KEY_LENGTH * 8;
    private static final String SECRET_FACTORY_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    public static final SecretKeyFactory secretKeyFactory;

    static {
        try {
            secretKeyFactory = SecretKeyFactory.getInstance(SECRET_FACTORY_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to create key factory");
        }
    }

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public static String hashThePassword(byte[] salt, String password) throws PasswordHashingFailedException {
        LOGGER.info("Starting to hash the given password");
        if (salt.length != SALT_LENGTH) {
            LOGGER.error("Salt length is not valid: {}", salt.length);
            throw new InvalidSaltException("Salt length is not valid: " + salt.length);
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_IN_BITES);
        try {
            LOGGER.info("Generating secret factory.");

            LOGGER.info("Generating hash");
            byte[] hash = secretKeyFactory.generateSecret(spec).getEncoded();
            LOGGER.info("Hash generated");
            return BASE64_ENCODER.encodeToString(hash);
        } catch (InvalidKeySpecException e) {
            LOGGER.error("Failed to create the hash: {}", e.getMessage(), e);
            throw new PasswordHashingFailedException("Failed to hash the given password using salt: " + BASE64_ENCODER.encodeToString(salt));
        }
    }

    public static String hashThePassword(String salt, String password) throws PasswordHashingFailedException {
        byte[] byteSalt = stringToSalt(salt);
        return hashThePassword(byteSalt, password);
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

    public static String saltToString(byte[] salt) {
        return BASE64_ENCODER.encodeToString(salt);
    }

    public static byte[] stringToSalt(String salt) {
        return BASE64_DECODER.decode(salt);
    }

    public static void main(String[] args) throws PasswordHashingFailedException {
        String[] passwords = new String[]{"9999", "8888", "7777"};
        for (String s : passwords) {
            byte[] salt = getRandomSalt();
            String hashed_password = hashThePassword(salt, s);
//            System.out.println(Arrays.toString(salt));
//            System.out.println(Arrays.toString(BASE64_ENCODER.encode(salt)));
//            System.out.println(BASE64_ENCODER.encodeToString(salt));
//            System.out.println("------------");
            System.out.printf("Password: '%s', salt: '%s', hash: '%s'%n", s, BASE64_ENCODER.encodeToString(salt), hashed_password);
            BASE64_DECODER.decode(BASE64_ENCODER.encodeToString(salt));
            System.out.printf("salt: %s, \nsalt_back: %s", Arrays.toString(salt), Arrays.toString(stringToSalt(saltToString(salt))));
        }
    }
}
