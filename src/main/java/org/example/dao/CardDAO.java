package org.example.dao;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class CardDAO {
    private Card mapResultSetToCard(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setCardId(rs.getInt("card_id"));
        card.setUserId(rs.getInt("user_id"));
        int accountId = rs.getInt("account_id");
        if (!rs.wasNull()) {
            card.setAccountId(accountId);
        }
        card.setCardNumber(rs.getString("card_number"));
        card.setCardHolderName(rs.getString("card_holder_name"));
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            card.setExpiryDate(expiryDate.toLocalDate());
        }
        card.setCvv(rs.getString("cvv"));
        String cardTypeStr = rs.getString("card_type");
        if (cardTypeStr != null) {
            card.setCardType(CardTypeEnum.valueOf(cardTypeStr));
        }
        card.setBrand(rs.getString("brand"));
        card.setStatus(rs.getString("status"));
        BigDecimal limitAmount = rs.getBigDecimal("limit_amount");
        card.setLimitAmount(limitAmount != null ? limitAmount : null);
        BigDecimal currentBalance = rs.getBigDecimal("current_balance");
        card.setCurrentBalance(currentBalance != null ? currentBalance : BigDecimal.ZERO);
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            card.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            card.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return card;
    }
    private void prepareCardForSave(Card card) {
        if (card.getStatus() == null) {
            card.setStatus("ACTIVE");
        }
        if (card.getCurrentBalance() == null) {
            card.setCurrentBalance(BigDecimal.ZERO);
        }
        if (card.getCreatedAt() == null) {
            card.setCreatedAt(LocalDateTime.now());
        }
        if (card.getUpdatedAt() == null) {
            card.setUpdatedAt(LocalDateTime.now());
        }
    }
    public Card save(Card card) {
        prepareCardForSave(card);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO cards (user_id, account_id, card_number, card_holder_name, " +
                        "expiry_date, cvv, card_type, brand, status, limit_amount, current_balance, " +
                        "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?::card_type_enum, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING card_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, card.getUserId());
                if (card.getAccountId() != null) {
                    stmt.setInt(2, card.getAccountId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setString(3, card.getCardNumber());
                stmt.setString(4, card.getCardHolderName());
                stmt.setDate(5, Date.valueOf(card.getExpiryDate()));
                if (card.getCvv() != null) {
                    stmt.setString(6, card.getCvv());
                } else {
                    stmt.setNull(6, Types.VARCHAR);
                }
                stmt.setString(7, card.getCardType().toString());
                stmt.setString(8, card.getBrand());
                stmt.setString(9, card.getStatus());
                if (card.getLimitAmount() != null) {
                    stmt.setBigDecimal(10, card.getLimitAmount());
                } else {
                    stmt.setNull(10, Types.DECIMAL);
                }
                stmt.setBigDecimal(11, card.getCurrentBalance());
                stmt.setTimestamp(12, Timestamp.valueOf(card.getCreatedAt()));
                stmt.setTimestamp(13, Timestamp.valueOf(card.getUpdatedAt()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        card.setCardId(rs.getInt("card_id"));
                    }
                }
                return card;
            }
        });
    }
    public Optional<Card> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards WHERE card_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCard(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Card> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards ORDER BY card_id";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToCard(rs));
                }
                return cards;
            }
        });
    }
    public List<Card> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards WHERE user_id = ? ORDER BY created_at DESC";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        cards.add(mapResultSetToCard(rs));
                    }
                    return cards;
                }
            }
        });
    }
    public List<Card> findByAccountId(Integer accountId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards WHERE account_id = ? ORDER BY created_at DESC";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        cards.add(mapResultSetToCard(rs));
                    }
                    return cards;
                }
            }
        });
    }
    public List<Card> findByCardType(CardTypeEnum cardType) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards WHERE card_type = ?::card_type_enum ORDER BY created_at DESC";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cardType.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        cards.add(mapResultSetToCard(rs));
                    }
                    return cards;
                }
            }
        });
    }
    public List<Card> findByStatus(String status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM cards WHERE status = ? ORDER BY created_at DESC";
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        cards.add(mapResultSetToCard(rs));
                    }
                    return cards;
                }
            }
        });
    }
    public Card update(Card card) {
        card.setUpdatedAt(LocalDateTime.now());
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE cards SET user_id = ?, account_id = ?, card_number = ?, " +
                        "card_holder_name = ?, expiry_date = ?, cvv = ?, card_type = ?::card_type_enum, " +
                        "brand = ?, status = ?, limit_amount = ?, current_balance = ?, updated_at = ? " +
                        "WHERE card_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, card.getUserId());
                if (card.getAccountId() != null) {
                    stmt.setInt(2, card.getAccountId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setString(3, card.getCardNumber());
                stmt.setString(4, card.getCardHolderName());
                stmt.setDate(5, Date.valueOf(card.getExpiryDate()));
                if (card.getCvv() != null) {
                    stmt.setString(6, card.getCvv());
                } else {
                    stmt.setNull(6, Types.VARCHAR);
                }
                stmt.setString(7, card.getCardType().toString());
                stmt.setString(8, card.getBrand());
                stmt.setString(9, card.getStatus());
                if (card.getLimitAmount() != null) {
                    stmt.setBigDecimal(10, card.getLimitAmount());
                } else {
                    stmt.setNull(10, Types.DECIMAL);
                }
                stmt.setBigDecimal(11, card.getCurrentBalance());
                stmt.setTimestamp(12, Timestamp.valueOf(card.getUpdatedAt()));
                stmt.setInt(13, card.getCardId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Cartão não encontrado para atualização: " + card.getCardId());
                }
                return card;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM cards WHERE card_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(Card card) {
        if (card != null && card.getCardId() != null) {
            return delete(card.getCardId());
        }
        return false;
    }
}