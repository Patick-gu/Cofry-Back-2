package org.example.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class BoletoCodeService {
    public String generateBoletoCode(String bankCode, String walletCode, String ourNumber, 
                                     LocalDate dueDate, BigDecimal amount) {
        validateInputs(bankCode, walletCode, ourNumber, dueDate, amount);
        String normalizedBankCode = normalizeAndValidateBankCode(bankCode);
        String normalizedWalletCode = normalizeAndValidateWalletCode(walletCode);
        String normalizedOurNumber = normalizeAndValidateOurNumber(ourNumber);
        String currencyCode = "9"; 
        String dueDateFactor = calculateDueDateFactor(dueDate);
        String documentValue = formatDocumentValue(amount);
        String ourNumberPart1 = normalizedOurNumber.substring(0, Math.min(15, normalizedOurNumber.length()));
        String ourNumberPart2 = normalizedOurNumber.length() > 15 
            ? normalizedOurNumber.substring(15) 
            : "";
        ourNumberPart1 = padLeft(ourNumberPart1, 15, '0');
        ourNumberPart2 = padLeft(ourNumberPart2, 8, '0');
        String lineWithoutDV = normalizedBankCode + 
                               currencyCode + 
                               normalizedWalletCode + 
                               ourNumberPart1;
        int checkDigit = calculateCheckDigit(lineWithoutDV);
        String boletoCode = normalizedBankCode +      
                           currencyCode +              
                           normalizedWalletCode +      
                           ourNumberPart1 +            
                           checkDigit +                
                           dueDateFactor +             
                           documentValue +             
                           ourNumberPart2;             
        if (boletoCode.length() != 48) {
            throw new IllegalArgumentException("Linha digitável gerada deve ter exatamente 48 dígitos. Gerada: " + boletoCode.length());
        }
        return boletoCode;
    }
    private void validateInputs(String bankCode, String walletCode, String ourNumber, 
                               LocalDate dueDate, BigDecimal amount) {
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (walletCode == null || walletCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da carteira é obrigatório");
        }
        if (ourNumber == null || ourNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Nosso número é obrigatório");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do boleto deve ser maior que zero");
        }
    }
    private String normalizeAndValidateBankCode(String bankCode) {
        String normalized = bankCode.replaceAll("[^0-9]", "");
        if (normalized.length() != 3) {
            throw new IllegalArgumentException("Código do banco deve ter exatamente 3 dígitos");
        }
        return normalized;
    }
    private String normalizeAndValidateWalletCode(String walletCode) {
        String normalized = walletCode.replaceAll("[^0-9]", "");
        if (normalized.length() == 0 || normalized.length() > 5) {
            throw new IllegalArgumentException("Código da carteira deve ter entre 1 e 5 dígitos");
        }
        return padLeft(normalized, 5, '0');
    }
    private String normalizeAndValidateOurNumber(String ourNumber) {
        String normalized = ourNumber.replaceAll("[^0-9]", "");
        if (normalized.length() == 0 || normalized.length() > 23) {
            throw new IllegalArgumentException("Nosso número deve ter entre 1 e 23 dígitos");
        }
        return normalized;
    }
    private String calculateDueDateFactor(LocalDate dueDate) {
        LocalDate baseDate = LocalDate.of(1997, 10, 7);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(baseDate, dueDate);
        if (daysBetween < 0) {
            daysBetween = 0;
        } else if (daysBetween > 99999) {
            daysBetween = 99999;
        }
        return padLeft(String.valueOf(daysBetween), 5, '0');
    }
    private String formatDocumentValue(BigDecimal amount) {
        long cents = amount.multiply(new BigDecimal("100")).longValue();
        if (cents > 9999999999L) {
            throw new IllegalArgumentException("Valor do boleto excede o limite máximo (R$ 99.999.999,99)");
        }
        return padLeft(String.valueOf(cents), 10, '0');
    }
    private int calculateCheckDigit(String line) {
        int sum = 0;
        int multiplier = 2;
        for (int i = line.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(line.charAt(i));
            int product = digit * multiplier;
            if (product > 9) {
                product = (product / 10) + (product % 10);
            }
            sum += product;
            multiplier = (multiplier == 2) ? 1 : 2;
        }
        int remainder = sum % 10;
        int checkDigit = (remainder == 0) ? 0 : (10 - remainder);
        return checkDigit;
    }
    private String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() + str.length() < length) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
}