package org.example.service;
import org.example.dao.AccountDAO;
import org.example.dao.TransactionDAO;
import org.example.dto.PixRequestDTO;
import org.example.dto.PixResponseDTO;
import org.example.model.Account;
import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public class PixService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final AccountService accountService;
    public PixService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.accountService = new AccountService();
    }
    public PixResponseDTO processPixTransfer(PixRequestDTO pixRequest) {
        if (pixRequest == null) {
            throw new IllegalArgumentException("Dados do PIX não podem ser nulos");
        }
        if (pixRequest.getSourceAccountId() == null) {
            throw new IllegalArgumentException("Conta de origem é obrigatória");
        }
        if (pixRequest.getAmount() == null || pixRequest.getAmount().trim().isEmpty()) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(pixRequest.getAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor deve ser maior que zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido");
        }
        Account sourceAccount = accountDAO.findById(pixRequest.getSourceAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));
        if (!"ACTIVE".equals(sourceAccount.getStatus())) {
            throw new IllegalArgumentException("Conta de origem não está ativa");
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente. Saldo disponível: " + sourceAccount.getBalance());
        }
        Integer destinationAccountIdTemp = pixRequest.getDestinationAccountId();
        if (destinationAccountIdTemp == null) {
            if (pixRequest.getDestinationUserId() == null) {
                throw new IllegalArgumentException("Conta de destino ou ID do usuário de destino é obrigatório");
            }
            List<Account> destinationAccounts = accountService.getAccountsByUserId(pixRequest.getDestinationUserId());
            if (destinationAccounts.isEmpty()) {
                throw new IllegalArgumentException("Usuário de destino não possui contas cadastradas");
            }
            Account activeAccount = destinationAccounts.stream()
                    .filter(acc -> "ACTIVE".equals(acc.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Usuário de destino não possui contas ativas"));
            destinationAccountIdTemp = activeAccount.getAccountId();
        }
        final Integer destinationAccountId = destinationAccountIdTemp;
        final BigDecimal finalAmount = amount;
        if (sourceAccount.getAccountId().equals(destinationAccountId)) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta");
        }
        Account destinationAccount = accountDAO.findById(destinationAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada"));
        if (!"ACTIVE".equals(destinationAccount.getStatus())) {
            throw new IllegalArgumentException("Conta de destino não está ativa");
        }
        return JdbcUtil.executeInTransaction(conn -> {
            String updateSourceSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND status = 'ACTIVE'";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSourceSql)) {
                stmt.setBigDecimal(1, finalAmount);
                stmt.setInt(2, sourceAccount.getAccountId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Falha ao atualizar saldo da conta de origem");
                }
            }
            String updateDestSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ? AND status = 'ACTIVE'";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateDestSql)) {
                stmt.setBigDecimal(1, finalAmount);
                stmt.setInt(2, destinationAccountId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Falha ao atualizar saldo da conta de destino");
                }
            }
            String descriptionTemp = pixRequest.getDescription();
            if (descriptionTemp == null || descriptionTemp.trim().isEmpty()) {
                descriptionTemp = "Transferência PIX para " + destinationAccount.getAccountNumber();
            }
            final String description = descriptionTemp;
            Transaction outgoingTransaction = new Transaction();
            outgoingTransaction.setSourceAccountId(sourceAccount.getAccountId());
            outgoingTransaction.setDestinationAccountId(destinationAccountId);
            outgoingTransaction.setAmount(finalAmount);
            outgoingTransaction.setTransactionType(TransactionTypeEnum.TRANSFER);
            outgoingTransaction.setDescription(description);
            outgoingTransaction.setTransactionDate(LocalDate.now());
            outgoingTransaction.setIsRecurring(false);
            outgoingTransaction.setCreatedAt(LocalDateTime.now());
            String insertOutgoingSql = "INSERT INTO transactions (source_account_id, destination_account_id, " +
                    "amount, transaction_type, description, transaction_date, is_recurring, created_at) " +
                    "VALUES (?, ?, ?, ?::transaction_type_enum, ?, ?, ?, ?) RETURNING transaction_id";
            Integer outgoingTransactionIdTemp;
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertOutgoingSql)) {
                stmt.setInt(1, outgoingTransaction.getSourceAccountId());
                stmt.setInt(2, outgoingTransaction.getDestinationAccountId());
                stmt.setBigDecimal(3, outgoingTransaction.getAmount());
                stmt.setString(4, outgoingTransaction.getTransactionType().toString());
                stmt.setString(5, outgoingTransaction.getDescription());
                stmt.setDate(6, java.sql.Date.valueOf(outgoingTransaction.getTransactionDate()));
                stmt.setBoolean(7, outgoingTransaction.getIsRecurring());
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(outgoingTransaction.getCreatedAt()));
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        outgoingTransactionIdTemp = rs.getInt("transaction_id");
                    } else {
                        throw new RuntimeException("Falha ao criar transação de saída");
                    }
                }
            }
            final Integer outgoingTransactionId = outgoingTransactionIdTemp;
            Transaction incomingTransaction = new Transaction();
            incomingTransaction.setSourceAccountId(destinationAccountId); 
            incomingTransaction.setDestinationAccountId(sourceAccount.getAccountId());
            incomingTransaction.setAmount(finalAmount);
            incomingTransaction.setTransactionType(TransactionTypeEnum.DEPOSIT);
            incomingTransaction.setDescription("Transferência PIX recebida de " + sourceAccount.getAccountNumber());
            incomingTransaction.setTransactionDate(LocalDate.now());
            incomingTransaction.setIsRecurring(false);
            incomingTransaction.setCreatedAt(LocalDateTime.now());
            String insertIncomingSql = "INSERT INTO transactions (source_account_id, destination_account_id, " +
                    "amount, transaction_type, description, transaction_date, is_recurring, created_at) " +
                    "VALUES (?, ?, ?, ?::transaction_type_enum, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertIncomingSql)) {
                stmt.setInt(1, incomingTransaction.getSourceAccountId());
                stmt.setInt(2, incomingTransaction.getDestinationAccountId());
                stmt.setBigDecimal(3, incomingTransaction.getAmount());
                stmt.setString(4, incomingTransaction.getTransactionType().toString());
                stmt.setString(5, incomingTransaction.getDescription());
                stmt.setDate(6, java.sql.Date.valueOf(incomingTransaction.getTransactionDate()));
                stmt.setBoolean(7, incomingTransaction.getIsRecurring());
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(incomingTransaction.getCreatedAt()));
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Falha ao criar transação de entrada");
                }
            }
            PixResponseDTO response = new PixResponseDTO();
            response.setTransactionId(outgoingTransactionId);
            response.setSourceAccountId(sourceAccount.getAccountId());
            response.setDestinationAccountId(destinationAccountId);
            response.setSourceUserId(sourceAccount.getUserId());
            response.setDestinationUserId(destinationAccount.getUserId());
            response.setAmount(finalAmount);
            response.setDescription(description);
            response.setTransactionDate(LocalDate.now());
            response.setCreatedAt(LocalDateTime.now());
            response.setStatus("SUCCESS");
            response.setMessage("Transferência PIX realizada com sucesso");
            return response;
        });
    }
}