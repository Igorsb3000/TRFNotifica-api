package ufrn.br.TRFNotifica.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class CriptografiaUtil {
    private static final Logger logger = LoggerFactory.getLogger(CriptografiaUtil.class);
    public static String decrypt(String toDecrypt, String key) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(toDecrypt));
            return new String(cipherText);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao descriptografar: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
    public static String encrypt(String toEncrypt, String key) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(toEncrypt.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao encriptar: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

}
