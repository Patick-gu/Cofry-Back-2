package org.example.service;
import org.example.dao.AccountDAO;
import org.example.dao.UserDAO;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import java.math.BigDecimal;
import java.util.List;
public class AccountService {
    private final AccountDAO accountDAO;
    private final UserDAO userDAO;
    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.userDAO = new UserDAO();
    }
    public Account createAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Conta não pode ser nula");
        }
        validateAccount(account);
        if (account.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(account.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + account.getUserId()));
        if (account.getAccountNumber() != null) {
            accountDAO.findByAccountNumber(account.getAccountNumber())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Já existe uma conta com o número: " + account.getAccountNumber());
                    });
        }
        if (account.getAccountType() == null) {
            account.setAccountType(AccountTypeEnum.CHECKING);
        }
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        if (account.getStatus() == null) {
            account.setStatus("ACTIVE");
        }
        return accountDAO.save(account);
    }
    public Account getAccountById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return accountDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com ID: " + id));
    }
    public Account getAccountByIdAndUserId(Integer id, Integer userId) {
        if (id == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        Account account = getAccountById(id);
        if (!account.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Conta não encontrada ou não pertence ao usuário especificado");
        }
        return account;
    }
    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }
    public List<Account> getAccountsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return accountDAO.findByUserId(userId);
    }
    public Account getAccountByNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta não pode ser nulo ou vazio");
        }
        return accountDAO.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com número: " + accountNumber));
    }
    public List<Account> getAccountsByType(AccountTypeEnum accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("Tipo da conta não pode ser nulo");
        }
        return accountDAO.findByType(accountType);
    }
    public List<Account> getAccountsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio");
        }
        return accountDAO.findByStatus(status);
    }
    public Account updateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Conta não pode ser nula");
        }
        if (account.getAccountId() == null) {
            throw new IllegalArgumentException("ID da conta é obrigatório para atualização");
        }
        getAccountById(account.getAccountId());
        if (account.getAccountNumber() != null) {
            accountDAO.findByAccountNumber(account.getAccountNumber())
                    .ifPresent(existing -> {
                        if (!existing.getAccountId().equals(account.getAccountId())) {
                            throw new IllegalArgumentException("Já existe uma conta com o número: " + account.getAccountNumber());
                        }
                    });
        }
        if (account.getUserId() != null) {
            userDAO.findById(account.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + account.getUserId()));
        }
        validateAccount(account);
        return accountDAO.update(account);
    }
    public Account setAccountBalance(Integer accountId, BigDecimal balance) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Saldo não pode ser nulo");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser negativo");
        }
        Account account = getAccountById(accountId);
        account.setBalance(balance);
        return accountDAO.update(account);
    }
    public Account deactivateAccount(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        Account account = getAccountById(id);
        account.setStatus("INACTIVE");
        return accountDAO.update(account);
    }
    public void deleteAccount(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        getAccountById(id);
        try {
            boolean deleted = accountDAO.delete(id);
            if (!deleted) {
                throw new IllegalArgumentException("Conta não encontrada com ID: " + id);
            }
        } catch (IllegalStateException e) {
            throw e;
        }
    }
    private void validateAccount(Account account) {
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta é obrigatório");
        }
        if (account.getAgencyNumber() == null || account.getAgencyNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número da agência é obrigatório");
        }
        if (account.getBalance() != null && account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser negativo");
        }
    }
}