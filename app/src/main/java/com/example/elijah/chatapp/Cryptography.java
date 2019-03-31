package com.example.elijah.chatapp;

import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

/**
 * Created by elija on 3/3/2019.
 */

public class Cryptography {

    public static KeyPair generateKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
    public void generateKeyPairEC() throws Exception {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(128);
            KeyPair kp = kpg.generateKeyPair();
            PublicKey publickey = kp.getPublic();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(kp.getPrivate());

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt1(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");

        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));
        String str = new String(cipherText, "UTF-8");

        return Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }
    public static String decrypt1(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = android.util.Base64.decode(cipherText, Base64.DEFAULT);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), "UTF-8");
    }
    public static String decrypt2(String cipherText, PublicKey publicKey) throws Exception {
        byte[] bytes = android.util.Base64.decode(cipherText, Base64.DEFAULT);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, publicKey);

        return new String(decriptCipher.doFinal(bytes), "UTF-8");
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes("UTF-8"));

        byte[] signature = privateSignature.sign();

        return Base64.encodeToString(signature, Base64.NO_WRAP);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes("UTF-8"));

        byte[] signatureBytes = android.util.Base64.decode(signature, Base64.DEFAULT);

        return publicSignature.verify(signatureBytes);
    }
    public static String byteArrayToHexString(byte[] bytes)
    {
        StringBuffer buffer = new StringBuffer();
        for(int i=0; i<bytes.length; i++)
        {
            if(((int)bytes[i] & 0xff) < 0x10)
                buffer.append("0");
            buffer.append(Long.toString((int) bytes[i] & 0xff, 16));
        }
        return buffer.toString();
    }
    public static String savePrivateKey(PrivateKey priv) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec spec = fact.getKeySpec(priv,
                PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = android.util.Base64.encodeToString(packed, Base64.DEFAULT);

        Arrays.fill(packed, (byte) 0);
        return key64;
    }
    public static String savePublicKey(PublicKey publ) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("EC");
        X509EncodedKeySpec spec = fact.getKeySpec(publ,
                X509EncodedKeySpec.class);
        return android.util.Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
    }

    public static PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException {
        byte[] clear = android.util.Base64.decode(key64,Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("EC");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }


    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
        byte[] data = android.util.Base64.decode(stored,Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePublic(spec);
    }
    public static SecretKey generateSharedSecret(PrivateKey privateKey,
                                                 PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH", "BC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
