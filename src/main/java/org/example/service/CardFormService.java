package org.example.service;
import org.example.dto.CardRequestDTO;
import org.example.dto.CardResponseDTO;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
public class CardFormService {
    private final CardService cardService;
    public CardFormService() {
        this.cardService = new CardService();
    }
    public CardResponseDTO createCardFromForm(CardRequestDTO cardDTO) {
        if (cardDTO == null) {
            throw new IllegalArgumentException("Card data cannot be null");
        }
        if (cardDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (cardDTO.getCardNumber() == null || cardDTO.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (cardDTO.getCardHolderName() == null || cardDTO.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        if (cardDTO.getExpiryDate() == null || cardDTO.getExpiryDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        if (cardDTO.getCardType() == null || cardDTO.getCardType().trim().isEmpty()) {
            throw new IllegalArgumentException("Card type is required");
        }
        CardTypeEnum cardType;
        try {
            cardType = CardTypeEnum.valueOf(cardDTO.getCardType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid card type. Available types: CREDIT, DEBIT, PREPAID");
        }
        LocalDate expiryDate = parseExpiryDate(cardDTO.getExpiryDate());
        String cleanCardNumber = cardDTO.getCardNumber().replaceAll("[^0-9]", "");
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number (must have between 13 and 19 digits)");
        }
        String maskedNumber = maskCardNumber(cleanCardNumber);
        BigDecimal limitAmount = null;
        if (cardType == CardTypeEnum.CREDIT) {
            if (cardDTO.getLimitAmount() == null || cardDTO.getLimitAmount().trim().isEmpty()) {
                throw new IllegalArgumentException("Limit amount is required for credit cards");
            }
            try {
                limitAmount = new BigDecimal(cardDTO.getLimitAmount());
                if (limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Limit amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid limit amount format");
            }
        }
        String brand = cardDTO.getBrand();
        if (brand == null || brand.trim().isEmpty()) {
            brand = detectCardBrand(cleanCardNumber);
        }
        Card card = new Card();
        card.setUserId(cardDTO.getUserId());
        card.setAccountId(cardDTO.getAccountId());
        card.setCardNumber(maskedNumber); 
        card.setCardHolderName(cardDTO.getCardHolderName().toUpperCase());
        card.setExpiryDate(expiryDate);
        card.setCvv(cardDTO.getCvv()); 
        card.setCardType(cardType);
        card.setBrand(brand);
        card.setLimitAmount(limitAmount);
        card.setCurrentBalance(BigDecimal.ZERO);
        card.setStatus("ACTIVE");
        Card savedCard = cardService.createCard(card);
        return convertToResponseDTO(savedCard);
    }
    private LocalDate parseExpiryDate(String expiryDateStr) {
        if (expiryDateStr == null || expiryDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        String trimmed = expiryDateStr.trim();
        if (trimmed.matches("\\d{2}/\\d{2}")) {
            String[] parts = trimmed.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            if (year < 100) {
                year += 2000;
            }
            LocalDate date = LocalDate.of(year, month, 1);
            return date.withDayOfMonth(date.lengthOfMonth());
        }
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/YY or YYYY-MM-DD");
        }
    }
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        String masked = "**** **** **** " + lastFour;
        return masked;
    }
    private String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "UNKNOWN";
        }
        String prefix = cardNumber.substring(0, 4);
        if (cardNumber.startsWith("4")) {
            return "Visa";
        }
        if (prefix.matches("5[1-5]\\d{2}") || (cardNumber.length() >= 4 && 
            (cardNumber.startsWith("2221") || cardNumber.startsWith("2720")))) {
            return "Mastercard";
        }
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "American Express";
        }
        if (prefix.matches("(4011|4312|4389|4514|4576|5041|5066|5090|6277|6362|6363|6504|6505|6506|6507|6508|6516|6550)")) {
            return "Elo";
        }
        return "UNKNOWN";
    }
    public CardResponseDTO updateCardFromForm(Integer cardId, CardRequestDTO cardDTO) {
        if (cardDTO == null) {
            throw new IllegalArgumentException("Dados do cartão não podem ser nulos");
        }
        if (cardId == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório");
        }
        org.example.service.CardService cardService = new org.example.service.CardService();
        org.example.model.Card existingCard = cardService.getCardById(cardId);
        if (cardDTO.getCardHolderName() != null && !cardDTO.getCardHolderName().trim().isEmpty()) {
            existingCard.setCardHolderName(cardDTO.getCardHolderName().toUpperCase());
        }
        if (cardDTO.getExpiryDate() != null && !cardDTO.getExpiryDate().trim().isEmpty()) {
            LocalDate expiryDate = parseExpiryDate(cardDTO.getExpiryDate());
            existingCard.setExpiryDate(expiryDate);
        }
        if (cardDTO.getCvv() != null) {
            existingCard.setCvv(cardDTO.getCvv());
        }
        if (cardDTO.getCardType() != null && !cardDTO.getCardType().trim().isEmpty()) {
            try {
                CardTypeEnum cardType = CardTypeEnum.valueOf(cardDTO.getCardType().toUpperCase());
                existingCard.setCardType(cardType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de cartão inválido. Tipos disponíveis: CREDIT, DEBIT, PREPAID");
            }
        }
        if (cardDTO.getBrand() != null) {
            existingCard.setBrand(cardDTO.getBrand());
        }
        if (cardDTO.getLimitAmount() != null && !cardDTO.getLimitAmount().trim().isEmpty()) {
            try {
                BigDecimal limitAmount = new BigDecimal(cardDTO.getLimitAmount());
                existingCard.setLimitAmount(limitAmount);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato de limite inválido");
            }
        }
        if (cardDTO.getStatus() != null) {
            existingCard.setStatus(cardDTO.getStatus());
        }
        org.example.model.Card updatedCard = cardService.updateCard(existingCard);
        return convertToResponseDTO(updatedCard);
    }
    public CardResponseDTO getCardById(Integer cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório");
        }
        org.example.service.CardService cardService = new org.example.service.CardService();
        org.example.model.Card card = cardService.getCardById(cardId);
        return convertToResponseDTO(card);
    }
    private CardResponseDTO convertToResponseDTO(Card card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setCardId(card.getCardId());
        dto.setUserId(card.getUserId());
        dto.setAccountId(card.getAccountId());
        dto.setCardNumber(card.getCardNumber()); 
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardType(card.getCardType() != null ? card.getCardType().name() : null);
        dto.setBrand(card.getBrand());
        dto.setStatus(card.getStatus());
        dto.setLimitAmount(card.getLimitAmount());
        dto.setCurrentBalance(card.getCurrentBalance());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }
}