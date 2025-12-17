package org.example.service;
import org.example.dao.CardDAO;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import java.util.List;
import java.util.Optional;
public class CardService {
    private final CardDAO cardDAO;
    public CardService() {
        this.cardDAO = new CardDAO();
    }
    public Card createCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cartão não pode ser nulo");
        }
        validateCard(card);
        UserService userService = new UserService();
        userService.getUserById(card.getUserId());
        if (card.getAccountId() != null) {
            AccountService accountService = new AccountService();
            accountService.getAccountById(card.getAccountId());
        }
        if (card.getCardType() == CardTypeEnum.CREDIT && card.getLimitAmount() == null) {
            throw new IllegalArgumentException("Cartão de crédito deve ter um limite definido");
        }
        return cardDAO.save(card);
    }
    public Card getCardById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return cardDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado com ID: " + id));
    }
    public List<Card> getAllCards() {
        return cardDAO.findAll();
    }
    public List<Card> getCardsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return cardDAO.findByUserId(userId);
    }
    public List<Card> getCardsByAccountId(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return cardDAO.findByAccountId(accountId);
    }
    public List<Card> getCardsByType(CardTypeEnum cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("Tipo do cartão não pode ser nulo");
        }
        return cardDAO.findByCardType(cardType);
    }
    public List<Card> getCardsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio");
        }
        return cardDAO.findByStatus(status);
    }
    public Card updateCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cartão não pode ser nulo");
        }
        if (card.getCardId() == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório para atualização");
        }
        Card existingCard = getCardById(card.getCardId());
        validateCard(card);
        return cardDAO.update(card);
    }
    public void deleteCard(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = cardDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Cartão não encontrado com ID: " + id);
        }
    }
    private void validateCard(Card card) {
        if (card.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (card.getCardNumber() == null || card.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número do cartão é obrigatório");
        }
        if (card.getCardHolderName() == null || card.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do portador é obrigatório");
        }
        if (card.getExpiryDate() == null) {
            throw new IllegalArgumentException("Data de expiração é obrigatória");
        }
        if (card.getCardType() == null) {
            throw new IllegalArgumentException("Tipo do cartão é obrigatório");
        }
        if (card.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Cartão expirado");
        }
        String cardNumber = card.getCardNumber();
        if (cardNumber != null && !cardNumber.contains("*")) {
            String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
            if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
                throw new IllegalArgumentException("Número do cartão inválido (deve ter entre 13 e 19 dígitos)");
            }
        }
    }
}