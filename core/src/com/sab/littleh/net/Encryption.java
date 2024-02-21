package com.sab.littleh.net;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public final class Encryption {
    public static final String RSA = "RSA";
    public static final String AES = "AES";
    public static final String AES_CTR = "AES/CTR/NoPadding";

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(AES);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
