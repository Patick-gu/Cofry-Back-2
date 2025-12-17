package org.example.utils;
public class ValidateCPF {
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return false;
        }
        if (isAllDigitsEqual(cleanCpf)) {
            return false;
        }
        int firstDigit = calculateDigit(cleanCpf, 9);
        int secondDigit = calculateDigit(cleanCpf, 10);
        return firstDigit == Character.getNumericValue(cleanCpf.charAt(9)) &&
               secondDigit == Character.getNumericValue(cleanCpf.charAt(10));
    }
    public static String format(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return null;
        }
        return cleanCpf.substring(0, 3) + "." +
               cleanCpf.substring(3, 6) + "." +
               cleanCpf.substring(6, 9) + "-" +
               cleanCpf.substring(9, 11);
    }
    public static String unformat(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return null;
        }
        return cleanCpf;
    }
    public static String validateAndFormat(String cpf) {
        if (isValid(cpf)) {
            return format(cpf);
        }
        return null;
    }
    private static boolean isAllDigitsEqual(String cpf) {
        if (cpf == null || cpf.length() < 2) {
            return false;
        }
        char firstDigit = cpf.charAt(0);
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != firstDigit) {
                return false;
            }
        }
        return true;
    }
    private static int calculateDigit(String cpf, int length) {
        int sum = 0;
        int weight = length + 1;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * weight;
            weight--;
        }
        int remainder = sum % 11;
        if (remainder < 2) {
            return 0;
        } else {
            return 11 - remainder;
        }
    }
}