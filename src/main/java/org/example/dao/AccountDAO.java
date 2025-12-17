package org.example.dao;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class AccountDAO {
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBankCode(getStringSafely(rs, "bank_code"));
        account.setBankName(getStringSafely(rs, "bank_name"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAgencyNumber(rs.getString("agency_number"));
        String accountTypeStr = rs.getString("account_type");
        if (accountTypeStr != null) {
            account.setAccountType(AccountTypeEnum.valueOf(accountTypeStr));
        }
        BigDecimal balance = rs.getBigDecimal("balance");
        account.setBalance(balance != null ? balance : BigDecimal.ZERO);
        account.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }
        return account;
    }
    private String getStringSafely(ResultSet rs, String columnName) throws SQLException {
        try {
            String value = rs.getString(columnName);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("não foi encontrado")) {
                return null;
            }
            throw e; 
        }
    }
    private void prepareAccountForSave(Account account) {
        if (account.getAccountType() == null) {
            account.setAccountType(AccountTypeEnum.CHECKING);
        }
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        if (account.getStatus() == null) {
            account.setStatus("ACTIVE");
        }
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
    }
    public Account save(Account account) {
        prepareAccountForSave(account);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO accounts (user_id, bank_code, bank_name, account_number, agency_number, account_type, " +
                        "balance, created_at, status) VALUES (?, ?, ?, ?, ?, ?::account_type_enum, ?, ?, ?) RETURNING account_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, account.getUserId());
                stmt.setString(2, account.getBankCode());
                stmt.setString(3, account.getBankName());
                stmt.setString(4, account.getAccountNumber());
                stmt.setString(5, account.getAgencyNumber());
                stmt.setString(6, account.getAccountType().toString());
                stmt.setBigDecimal(7, account.getBalance());
                stmt.setTimestamp(8, Timestamp.valueOf(account.getCreatedAt()));
                stmt.setString(9, account.getStatus());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        account.setAccountId(rs.getInt("account_id"));
                    }
                }
                return account;
            }
        });
    }
    public Optional<Account> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAccount(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Account> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts ORDER BY account_id";
            List<Account> accounts = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
                return accounts;
            }
        });
    }
    public List<Account> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY account_id";
            List<Account> accounts = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        accounts.add(mapResultSetToAccount(rs));
                    }
                    return accounts;
                }
            }
        });
    }
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, accountNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAccount(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Account> findByAccountType(AccountTypeEnum accountType) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts WHERE account_type = ?::account_type_enum ORDER BY account_id";
            List<Account> accounts = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, accountType.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        accounts.add(mapResultSetToAccount(rs));
                    }
                    return accounts;
                }
            }
        });
    }
    public List<Account> findByType(AccountTypeEnum accountType) {
        return findByAccountType(accountType);
    }
    public List<Account> findByStatus(String status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM accounts WHERE status = ? ORDER BY account_id";
            List<Account> accounts = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        accounts.add(mapResultSetToAccount(rs));
                    }
                    return accounts;
                }
            }
        });
    }
    public Account update(Account account) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE accounts SET user_id = ?, bank_code = ?, bank_name = ?, account_number = ?, agency_number = ?, " +
                        "account_type = ?::account_type_enum, balance = ?, status = ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, account.getUserId());
                stmt.setString(2, account.getBankCode());
                stmt.setString(3, account.getBankName());
                stmt.setString(4, account.getAccountNumber());
                stmt.setString(5, account.getAgencyNumber());
                stmt.setString(6, account.getAccountType().toString());
                stmt.setBigDecimal(7, account.getBalance());
                stmt.setString(8, account.getStatus());
                stmt.setInt(9, account.getAccountId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Conta não encontrada para atualização: " + account.getAccountId());
                }
                return account;
            }
        });
    }
    public boolean hasTransactions(Integer accountId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM transactions " +
                        "WHERE source_account_id = ? OR destination_account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                }
            }
        });
    }
    public boolean delete(Integer id) {
        if (hasTransactions(id)) {
            throw new IllegalStateException(
                "Não é possível remover a conta. Existem transações vinculadas a esta conta. " +
                "Remova as transações primeiro ou desative a conta em vez de removê-la."
            );
        }
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM accounts WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(Account account) {
        if (account != null && account.getAccountId() != null) {
            return delete(account.getAccountId());
        }
        return false;
    }
    public boolean updateBalance(Integer accountId, BigDecimal amount) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ? AND status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setInt(2, accountId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
}