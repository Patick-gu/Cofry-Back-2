package org.example.service;
import org.example.dao.TransactionDAO;
import org.example.dao.AccountDAO;
import org.example.dao.TransactionCategoryDAO;
import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final TransactionCategoryDAO categoryDAO;
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
        this.categoryDAO = new TransactionCategoryDAO();
    }
    public Transaction createTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        validateTransaction(transaction);
        accountDAO.findById(transaction.getSourceAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada com ID: " + transaction.getSourceAccountId()));
        if (transaction.getDestinationAccountId() != null) {
            accountDAO.findById(transaction.getDestinationAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada com ID: " + transaction.getDestinationAccountId()));
        }
        if (transaction.getCategoryId() != null) {
            categoryDAO.findById(transaction.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + transaction.getCategoryId()));
        }
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        validateSufficientBalance(transaction);
        return saveTransactionAndUpdateBalances(transaction);
    }
    private void validateSufficientBalance(Transaction transaction) {
        TransactionTypeEnum type = transaction.getTransactionType();
        if (type == TransactionTypeEnum.WITHDRAWAL || 
            type == TransactionTypeEnum.PAYMENT || 
            type == TransactionTypeEnum.TRANSFER) {
            org.example.model.Account sourceAccount = accountDAO.findById(transaction.getSourceAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));
            BigDecimal currentBalance = sourceAccount.getBalance();
            BigDecimal transactionAmount = transaction.getAmount();
            if (currentBalance.compareTo(transactionAmount) < 0) {
                throw new IllegalStateException(
                    String.format("Saldo insuficiente. Saldo atual: R$ %.2f, Valor necessário: R$ %.2f",
                        currentBalance.doubleValue(), transactionAmount.doubleValue())
                );
            }
        }
    }
    private Transaction saveTransactionAndUpdateBalances(Transaction transaction) {
        return org.example.persistence.JdbcUtil.executeInTransaction(conn -> {
            try {
                Transaction savedTransaction = saveTransactionInConnection(conn, transaction);
                updateAccountBalancesInConnection(conn, savedTransaction);
                return savedTransaction;
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao salvar transação e atualizar saldos: " + e.getMessage(), e);
            }
        });
    }
    private Transaction saveTransactionInConnection(java.sql.Connection conn, Transaction transaction) throws SQLException {
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(java.time.LocalDate.now());
        }
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(java.time.LocalDateTime.now());
        }
        String sql = "INSERT INTO transactions (source_account_id, destination_account_id, category_id, " +
                    "amount, transaction_type, description, transaction_date, is_recurring, " +
                    "installment_current, installment_total, created_at) " +
                    "VALUES (?, ?, ?, ?, ?::transaction_type_enum, ?, ?, ?, ?, ?, ?) RETURNING transaction_id";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getSourceAccountId());
            if (transaction.getDestinationAccountId() != null) {
                stmt.setInt(2, transaction.getDestinationAccountId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            if (transaction.getCategoryId() != null) {
                stmt.setInt(3, transaction.getCategoryId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getTransactionType().toString());
            stmt.setString(6, transaction.getDescription());
            stmt.setDate(7, java.sql.Date.valueOf(transaction.getTransactionDate()));
            stmt.setBoolean(8, transaction.getIsRecurring());
            if (transaction.getInstallmentCurrent() != null) {
                stmt.setInt(9, transaction.getInstallmentCurrent());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }
            if (transaction.getInstallmentTotal() != null) {
                stmt.setInt(10, transaction.getInstallmentTotal());
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }
            stmt.setTimestamp(11, java.sql.Timestamp.valueOf(transaction.getCreatedAt()));
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    transaction.setTransactionId(rs.getInt("transaction_id"));
                }
            }
            return transaction;
        }
    }
    private void updateAccountBalancesInConnection(java.sql.Connection conn, Transaction transaction) throws SQLException {
        BigDecimal amount = transaction.getAmount();
        TransactionTypeEnum type = transaction.getTransactionType();
        Integer sourceAccountId = transaction.getSourceAccountId();
        Integer destinationAccountId = transaction.getDestinationAccountId();
        String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ? AND status = 'ACTIVE'";
        switch (type) {
            case DEPOSIT:
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBigDecimal(1, amount);
                    stmt.setInt(2, sourceAccountId);
                    stmt.executeUpdate();
                }
                break;
            case WITHDRAWAL:
            case PAYMENT:
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBigDecimal(1, amount.negate());
                    stmt.setInt(2, sourceAccountId);
                    stmt.executeUpdate();
                }
                break;
            case TRANSFER:
                if (destinationAccountId != null) {
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setBigDecimal(1, amount.negate());
                        stmt.setInt(2, sourceAccountId);
                        stmt.executeUpdate();
                    }
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setBigDecimal(1, amount);
                        stmt.setInt(2, destinationAccountId);
                        stmt.executeUpdate();
                    }
                }
                break;
        }
    }
    public Transaction getTransactionById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return transactionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada com ID: " + id));
    }
    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }
    public List<Transaction> getTransactionsBySourceAccount(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return transactionDAO.findBySourceAccountId(accountId);
    }
    public List<Transaction> getTransactionsByDestinationAccount(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return transactionDAO.findByDestinationAccountId(accountId);
    }
    public List<Transaction> getTransactionsByType(TransactionTypeEnum type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo não pode ser nulo");
        }
        return transactionDAO.findByType(type);
    }
    public List<Transaction> getTransactionsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        return transactionDAO.findByCategoryId(categoryId);
    }
    public List<Transaction> getTransactionsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return transactionDAO.findByUserId(userId);
    }
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datas inicial e final são obrigatórias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        return transactionDAO.findByDateRange(startDate, endDate);
    }
    public List<Transaction> getRecurringTransactions() {
        return transactionDAO.findRecurringTransactions();
    }
    public Transaction updateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        if (transaction.getTransactionId() == null) {
            throw new IllegalArgumentException("ID da transação é obrigatório para atualização");
        }
        getTransactionById(transaction.getTransactionId());
        validateTransaction(transaction);
        if (transaction.getSourceAccountId() != null) {
            accountDAO.findById(transaction.getSourceAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada com ID: " + transaction.getSourceAccountId()));
        }
        if (transaction.getDestinationAccountId() != null) {
            accountDAO.findById(transaction.getDestinationAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada com ID: " + transaction.getDestinationAccountId()));
        }
        if (transaction.getCategoryId() != null) {
            categoryDAO.findById(transaction.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + transaction.getCategoryId()));
        }
        return transactionDAO.update(transaction);
    }
    public void deleteTransaction(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = transactionDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Transação não encontrada com ID: " + id);
        }
    }
    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
        if (transaction.getTransactionType() == null) {
            throw new IllegalArgumentException("Tipo da transação é obrigatório");
        }
        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
    }
}