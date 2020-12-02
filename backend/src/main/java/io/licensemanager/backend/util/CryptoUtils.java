package io.licensemanager.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

public class CryptoUtils {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    private static final String ALGORITHM = "RSA";
    private static final Integer KEY_LENGTH = 2048;

    public static Optional<KeyPair> generateKeyPair() {
        logger.info("Generating new key pair for asymmetric files signing");
        KeyPair keyPair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(KEY_LENGTH, new SecureRandom());
            keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cannot generate key pair, reason - {}", e.getMessage());
        }

        return Optional.ofNullable(keyPair);
    }
}
