package com.omer.secure_chatapp.Activities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {

    private static final String ALGORITHM = "AES";
    private static final String ENCODING = "UTF-8";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String text, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes(ENCODING));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String encryptedText, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, ENCODING);
    }
}