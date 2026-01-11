package org.example.dao;

import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import org.example.persistence.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransactionDAO {
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        
        Object transactionIdObj = rs.getObject("transaction_id");
        if (transactionIdObj != null) {
            transaction.setTransactionId(UUID.fromString(transactionIdObj.toString()));
        }
        
        Object sourceAccountIdObj = rs.getObject("source_account_id");
        if (sourceAccountIdObj != null) {
            transaction.setSourceAccountId(UUID.fromString(sourceAccountIdObj.toString()));
        }
        
        Object destAccountIdObj = rs.getObject("destination_account_id");
        if (destAccountIdObj != null) {
            transaction.setDestinationAccountId(UUID.fromString(destAccountIdObj.toString()));
        }
        
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            transaction.setCategoryId(categoryId);
        }
        
        BigDecimal amount = rs.getBigDecimal("amount");
        transaction.setAmount(amount != null ? amount : BigDecimal.ZERO);
        
        String transactionTypeStr = rs.getString("transaction_type");
        if (transactionTypeStr != null) {
            transaction.setTransactionType(TransactionTypeEnum.valueOf(transactionTypeStr));
        }
        
        transaction.setDescription(rs.getString("description"));
        
        Date transactionDate = rs.getDate("transaction_date");
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate.toLocalDate());
        }
        
        transaction.setIsRecurring(rs.getBoolean("is_recurring"));
        if (rs.wasNull()) {
            transaction.setIsRecurring(false);
        }
        
        int installmentCurrent = rs.getInt("installment_current");
        if (!rs.wasNull()) {
            transaction.setInstallmentCurrent(installmentCurrent);
        }
        
        int installmentTotal = rs.getInt("installment_total");
        if (!rs.wasNull()) {
            transaction.setInstallmentTotal(installmentTotal);
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return transaction;
    }
    
    private void prepareTransactionForSave(Transaction transaction) {
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
    }
    
    public Transaction save(Transaction transaction) {
        prepareTransactionForSave(transaction);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO transactions (transaction_id, source_account_id, destination_account_id, category_id, " +
                        "amount, transaction_type, description, transaction_date, is_recurring, " +
                        "installment_current, installment_total, created_at) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?, ?::transaction_type_enum, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, transaction.getTransactionId());
                stmt.setObject(2, transaction.getSourceAccountId());
                
                if (transaction.getDestinationAccountId() != null) {
                    stmt.setObject(3, transaction.getDestinationAccountId());
                } else {
                    stmt.setNull(3, Types.OTHER);
                }
                
                if (transaction.getCategoryId() != null) {
                    stmt.setInt(4, transaction.getCategoryId());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                stmt.setBigDecimal(5, transaction.getAmount());
                stmt.setString(6, transaction.getTransactionType().toString());
                stmt.setString(7, transaction.getDescription());
                stmt.setDate(8, Date.valueOf(transaction.getTransactionDate()));
                stmt.setBoolean(9, transaction.getIsRecurring());
                
                if (transaction.getInstallmentCurrent() != null) {
                    stmt.setInt(10, transaction.getInstallmentCurrent());
                } else {
                    stmt.setNull(10, Types.INTEGER);
                }
                
                if (transaction.getInstallmentTotal() != null) {
                    stmt.setInt(11, transaction.getInstallmentTotal());
                } else {
                    stmt.setNull(11, Types.INTEGER);
                }
                
                stmt.setTimestamp(12, Timestamp.valueOf(transaction.getCreatedAt()));
                
                stmt.executeUpdate();
                return transaction;
            }
        });
    }
    
    public Optional<Transaction> findById(UUID id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE transaction_id = ?::uuid";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToTransaction(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    public List<Transaction> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
        });
    }
    
    public List<Transaction> findBySourceAccountId(UUID accountId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE source_account_id = ?::uuid ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    public List<Transaction> findByDestinationAccountId(UUID accountId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE destination_account_id = ?::uuid ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    public List<Transaction> findByCategoryId(Integer categoryId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE category_id = ? ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    public List<Transaction> findByTransactionType(TransactionTypeEnum transactionType) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE transaction_type = ?::transaction_type_enum ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, transactionType.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    public List<Transaction> findByType(TransactionTypeEnum transactionType) {
        return findByTransactionType(transactionType);
    }
    
    public List<Transaction> findRecurring() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE is_recurring = true ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
        });
    }
    
    public List<Transaction> findRecurringTransactions() {
        return findRecurring();
    }
    
    public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transactions WHERE transaction_date >= ? AND transaction_date <= ? ORDER BY transaction_date DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    public Transaction update(Transaction transaction) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE transactions SET source_account_id = ?::uuid, destination_account_id = ?::uuid, " +
                        "category_id = ?, amount = ?, transaction_type = ?::transaction_type_enum, description = ?, " +
                        "transaction_date = ?, is_recurring = ?, installment_current = ?, " +
                        "installment_total = ? WHERE transaction_id = ?::uuid";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, transaction.getSourceAccountId());
                
                if (transaction.getDestinationAccountId() != null) {
                    stmt.setObject(2, transaction.getDestinationAccountId());
                } else {
                    stmt.setNull(2, Types.OTHER);
                }
                
                if (transaction.getCategoryId() != null) {
                    stmt.setInt(3, transaction.getCategoryId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                
                stmt.setBigDecimal(4, transaction.getAmount());
                stmt.setString(5, transaction.getTransactionType().toString());
                stmt.setString(6, transaction.getDescription());
                stmt.setDate(7, Date.valueOf(transaction.getTransactionDate()));
                stmt.setBoolean(8, transaction.getIsRecurring());
                
                if (transaction.getInstallmentCurrent() != null) {
                    stmt.setInt(9, transaction.getInstallmentCurrent());
                } else {
                    stmt.setNull(9, Types.INTEGER);
                }
                
                if (transaction.getInstallmentTotal() != null) {
                    stmt.setInt(10, transaction.getInstallmentTotal());
                } else {
                    stmt.setNull(10, Types.INTEGER);
                }
                
                stmt.setObject(11, transaction.getTransactionId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Transação não encontrada para atualização: " + transaction.getTransactionId());
                }
                return transaction;
            }
        });
    }
    
    public boolean delete(UUID id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM transactions WHERE transaction_id = ?::uuid";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    public boolean delete(Transaction transaction) {
        if (transaction != null && transaction.getTransactionId() != null) {
            return delete(transaction.getTransactionId());
        }
        return false;
    }
    
    public List<Transaction> findByUserId(UUID userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT DISTINCT t.* FROM transactions t " +
                        "INNER JOIN accounts a1 ON t.source_account_id = a1.account_id " +
                        "LEFT JOIN accounts a2 ON t.destination_account_id = a2.account_id " +
                        "WHERE a1.user_id = ?::uuid OR a2.user_id = ?::uuid " +
                        "ORDER BY t.transaction_date DESC, t.created_at DESC";
            List<Transaction> transactions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, userId);
                stmt.setObject(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
}
