package org.example.utils;
import java.util.regex.Pattern;
public class Validations {
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String LOGIN_PATTERN = 
        "^[a-zA-Z0-9_]{3,20}$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern loginPattern = Pattern.compile(LOGIN_PATTERN);
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String cleanEmail = email.trim();
        if (cleanEmail.length() > 254) {
            return false;
        }
        if (!emailPattern.matcher(cleanEmail).matches()) {
            return false;
        }
        if (cleanEmail.startsWith(".") || cleanEmail.endsWith(".") ||
            cleanEmail.startsWith("-") || cleanEmail.endsWith("-") ||
            cleanEmail.startsWith("@") || cleanEmail.endsWith("@")) {
            return false;
        }
        if (cleanEmail.contains("..")) {
            return false;
        }
        int atIndex = cleanEmail.indexOf('@');
        if (atIndex < 1 || atIndex >= cleanEmail.length() - 1) {
            return false;
        }
        String domain = cleanEmail.substring(atIndex + 1);
        if (!domain.contains(".")) {
            return false;
        }
        return true;
    }
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }
    }
    public static boolean isValidLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        String cleanLogin = login.trim();
        if (!loginPattern.matcher(cleanLogin).matches()) {
            return false;
        }
        if (cleanLogin.startsWith("_") || cleanLogin.endsWith("_")) {
            return false;
        }
        if (cleanLogin.matches("^[0-9]+$")) {
            return false;
        }
        return true;
    }
    public static void validateLogin(String login) {
        if (!isValidLogin(login)) {
            throw new IllegalArgumentException(
                "Login inválido. Deve ter entre 3 e 20 caracteres, " +
                "contendo apenas letras, números e underscore, e não pode começar ou terminar com underscore: " + login
            );
        }
    }
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
    public static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                "Senha inválida. Deve ter no mínimo 8 caracteres."
            );
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Senha inválida. Deve conter pelo menos uma letra maiúscula, " +
                "uma letra minúscula, um número e um caractere especial."
            );
        }
    }
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 11;
    }
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Telefone inválido: " + phone);
        }
    }
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " não pode ser nulo ou vazio");
        }
    }
    public static void validatePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser um número positivo");
        }
    }
    public static void validatePositive(java.math.BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser um número positivo");
        }
    }
}