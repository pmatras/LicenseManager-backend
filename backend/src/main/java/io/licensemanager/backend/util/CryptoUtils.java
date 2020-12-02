package io.licensemanager.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Optional;

public class CryptoUtils {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int RSA_KEY_LENGTH = 1024;
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int AES_KEY_LENGTH = 128;
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final short BITS_IN_BYTE = 8;

    public static Optional<KeyPair> generateKeyPair() {
        logger.info("Generating new key pair for asymmetric files signing");
        KeyPair keyPair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
            generator.initialize(RSA_KEY_LENGTH, new SecureRandom());
            keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cannot generate key pair, reason - {}", e.getMessage());
        }

        return Optional.ofNullable(keyPair);
    }

    private static Key generateAESKeyFromRSAKey(final Key rsaKey) {
        logger.info("generating AES key");
        byte[] aesKeyBytes = new byte[AES_KEY_LENGTH / BITS_IN_BYTE];
        byte[] rsaKeyBytes = rsaKey.getEncoded();
        int aesOffset = rsaKeyBytes.length / aesKeyBytes.length;
        for (int i = 0; i < aesKeyBytes.length; ++i) {
            aesKeyBytes[i] = rsaKeyBytes[i * aesOffset];
        }

        return new SecretKeySpec(aesKeyBytes, SYMMETRIC_ALGORITHM);
    }

    public static byte[] encrypt(final String plainText, final PublicKey publicKey) {
        logger.info("Encrypting content with public key");
        byte[] encrypted = new byte[0];
        try {
            Cipher encryptCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, generateAESKeyFromRSAKey(publicKey));
            encrypted = encryptCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Failed to encrypt content, reason - {}", e.getMessage());
        }

        return encrypted;
    }

    public static String decrypt(byte[] encrypted, PublicKey publicKey) {
        logger.info("Decrypting content with public key");
        String decrypted = "";
        try {
            Cipher decryptCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, generateAESKeyFromRSAKey(publicKey));
            decrypted = new String(decryptCipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException e) {
            logger.error("Failed to decrypt content, reason - {}", e.getMessage());
        }

        return decrypted;
    }

    public static String signContent(byte[] encrypted, PrivateKey privateKey) {
        logger.info("Signing content with private key");
        String contentSignature = "";
        try {
            Signature privateSignature = Signature.getInstance(SIGNING_ALGORITHM);
            privateSignature.initSign(privateKey);
            privateSignature.update(encrypted);
            byte[] signature = privateSignature.sign();
            contentSignature = Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            logger.error("Failed to sign content, reason - {}", e.getMessage());
        }

        return contentSignature;
    }

    public static boolean verifySign(byte[] encrypted, String signature, PublicKey publicKey) {
        logger.info("Verifying content's signature with public key");
        try {
            Signature publicSignature = Signature.getInstance(SIGNING_ALGORITHM);
            publicSignature.initVerify(publicKey);
            publicSignature.update(encrypted);
            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            return publicSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            logger.error("Failed to verify content's signature, reason - {}", e.getMessage());
        }

        return false;
    }
}
