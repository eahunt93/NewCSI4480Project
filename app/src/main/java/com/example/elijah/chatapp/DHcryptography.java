package com.example.elijah.chatapp;

import android.os.Build;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by elija on 3/27/2019.
 */

public class DHcryptography {




    //https://developer.android.com/guide/topics/security/cryptography
    //Used and edited code from the android developer website
    public static String encrypt(String strToEncrypt, SecretKey secret) {
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
            return Base64.encodeToString(cipherText, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }





    public static String decrypt(String strToDecrypt, SecretKey secret) {
        try
        {
            byte[] bytes = android.util.Base64.decode(strToDecrypt, Base64.DEFAULT);
            //recommended to use AES/GCM/PKCS5PADDING on the android developer website.
            //https://developer.android.com/guide/topics/security/cryptography
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secret);
            return  new String(cipher.doFinal(bytes), "UTF-8");
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
    //returns to a string of bytes to the private key can be saved.
    public static String savePrivateKey(PrivateKey priv) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec spec = fact.getKeySpec(priv,
                PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = android.util.Base64.encodeToString(packed, Base64.DEFAULT);

        Arrays.fill(packed, (byte) 0);
        return key64;
    }
    //returns public key key to formatted string to be stored
    public static String savePublicKey(PublicKey publ) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("EC");
        X509EncodedKeySpec spec = fact.getKeySpec(publ,
                X509EncodedKeySpec.class);
        return android.util.Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
    }

    //returns private key to proper forman to be stored
    public static PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException {
        byte[] clear = android.util.Base64.decode(key64,Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("EC");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }


    //gets public key from storage and returns it to the priginal public key
    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
        byte[] data = android.util.Base64.decode(stored,Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePublic(spec);
    }


    //geenerates the key pair.
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static KeyPair generateKeypair(){
        KeyPairGenerator kpg = null;

        try {
            //EC with bouncy castle
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        ECGenParameterSpec ecsp;
        //picking the curve to generate keys from
        ecsp = new ECGenParameterSpec("secp256k1");
        try {
            kpg.initialize(ecsp);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        final KeyPair kpU = kpg.genKeyPair();
        return kpU;
    }

    //generate the shared secret key.
    public static SecretKey generateAESKey(PrivateKey mprivatekey, PublicKey theirPublickey){
        KeyAgreement ecdhU = null;
        try {
            //using elliptical curve diffie hellman
            ecdhU = KeyAgreement.getInstance("ECDH");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            //Using my private key
            ecdhU.init(mprivatekey);
            Log.e("idk", ecdhU.toString());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            //using their public key
            ecdhU.doPhase(theirPublickey,true);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //Create the shared secret.
        SecretKey uAesKey = new SecretKeySpec(ecdhU.generateSecret(),0,16,"AES");

        Log.e("AES SEcret Key", uAesKey.toString());
        return uAesKey;

    }






}
