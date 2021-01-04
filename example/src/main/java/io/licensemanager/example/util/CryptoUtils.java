package io.licensemanager.example.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtils {

    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int AES_KEY_LENGTH = 128;
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final short BITS_IN_BYTE = 8;
    private static String PUBLIC_RSA_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxLnhVzumYgcrZ3IxlcPabHm++GJzJl0NgDGvW8eA3DbK/iemi9NHa6XTrkwMux7xZse+lOjCXa2kU8V9rc+7xQzyUyBOcXXeLdES8ftRBPZB1WIzYU9zhRjVpI/5yDLWmzTwRlmQ1Sc/sN6HLYwekmRcfOPeoo6aGzFnsVR56VwIDAQAB";

    private static PublicKey publicKey = generatePublicRSAKey();

    private static PublicKey generatePublicRSAKey() {
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_RSA_KEY));
            publicKey = keyFactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println(String.format("Failed to decode public RSA key, reason - %s", e.getMessage()));
        }

        return publicKey;
    }

    public static boolean verifySign(final byte[] encrypted, final String licenseKey) {
        try {
            Signature publicSignature = Signature.getInstance(SIGNING_ALGORITHM);
            publicSignature.initVerify(publicKey);
            publicSignature.update(encrypted);
            byte[] signatureBytes = Base64.getDecoder().decode(licenseKey);

            return publicSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            System.out.println(String.format("Failed to verify content's signature, reason - %s", e.getMessage()));
        }

        return false;
    }

    private static Key generateAESKeyFromRSAKey() {
        byte[] aesKeyBytes = new byte[AES_KEY_LENGTH / BITS_IN_BYTE];
        byte[] rsaKeyBytes = publicKey.getEncoded();
        int aesOffset = rsaKeyBytes.length / aesKeyBytes.length;
        for (int i = 0; i < aesKeyBytes.length; ++i) {
            aesKeyBytes[i] = rsaKeyBytes[i * aesOffset];
        }

        return new SecretKeySpec(aesKeyBytes, SYMMETRIC_ALGORITHM);
    }

    public static String decryptLicenseFile(final byte[] encrypted) {
        String decrypted = "";
        try {
            Cipher decryptCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, generateAESKeyFromRSAKey());
            decrypted = new String(decryptCipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException e) {
            System.out.println(String.format("Failed to decrypt content, reason - %s", e.getMessage()));
        }

        return decrypted;
    }

}
