package org.example.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
public class EncryptPassword {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    public static String encrypt(String password, String salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("Salt não pode ser nulo ou vazio");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao encriptar senha: algoritmo não encontrado", e);
        }
    }
    public static String[] encryptWithNewSalt(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        String salt = generateSalt();
        String hash = encrypt(password, salt);
        return new String[]{hash, salt};
    }
    public static boolean verify(String password, String storedHash, String salt) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (salt == null || salt.isEmpty()) {
            return false;
        }
        String calculatedHash = encrypt(password, salt);
        return calculatedHash.equals(storedHash);
    }
    @Deprecated
    public static String encryptSimple(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao encriptar senha: algoritmo não encontrado", e);
        }
    }
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; 
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}