package org.example.service;
import org.example.dao.BoletoDAO;
import org.example.model.Boleto;
import org.example.model.BoletoStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public class BoletoService {
    private final BoletoDAO boletoDAO;
    private final BoletoCodeService boletoCodeService;
    public BoletoService() {
        this.boletoDAO = new BoletoDAO();
        this.boletoCodeService = new BoletoCodeService();
    }
    public Boleto createBoleto(String title, BigDecimal amount, LocalDate dueDate, 
                               String bankCode, String walletCode, String ourNumber, Integer userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do boleto é obrigatório");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do boleto deve ser maior que zero");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (walletCode == null || walletCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da carteira é obrigatório");
        }
        if (ourNumber == null || ourNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Nosso número é obrigatório");
        }
        if (userId != null) {
            UserService userService = new UserService();
            userService.getUserById(userId); 
        }
        String boletoCode = boletoCodeService.generateBoletoCode(
            bankCode, walletCode, ourNumber, dueDate, amount
        );
        Boleto boleto = new Boleto();
        boleto.setTitle(title);
        boleto.setAmount(amount);
        boleto.setDueDate(dueDate);
        boleto.setBankCode(bankCode);
        boleto.setWalletCode(walletCode);
        boleto.setOurNumber(ourNumber);
        boleto.setBoletoCode(boletoCode);
        boleto.setUserId(userId);
        updateStatusBasedOnDueDate(boleto);
        return boletoDAO.save(boleto);
    }
    public Boleto getBoletoById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return boletoDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boleto não encontrado com ID: " + id));
    }
    public List<Boleto> listBoletos() {
        List<Boleto> boletos = boletoDAO.findAll();
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    public List<Boleto> listBoletosByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<Boleto> boletos = boletoDAO.findByUserId(userId);
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    public List<Boleto> listBoletosByStatus(BoletoStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        List<Boleto> boletos = boletoDAO.findByStatus(status);
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    public List<Boleto> listBoletosByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        }
        org.example.utils.ValidateCPF validateCPF = new org.example.utils.ValidateCPF();
        String cleanCpf = validateCPF.unformat(cpf.trim());
        if (!validateCPF.isValid(cleanCpf)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
        String formattedCpf = validateCPF.format(cleanCpf);
        org.example.service.UserService userService = new org.example.service.UserService();
        org.example.model.User user = userService.getUserByTaxId(formattedCpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com CPF: " + cpf));
        List<Boleto> boletos = boletoDAO.findByUserId(user.getUserId());
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    public Boleto markAsPaid(Long id) {
        Boleto boleto = getBoletoById(id);
        if (boleto.getStatus() == BoletoStatus.PAID) {
            throw new IllegalArgumentException("Boleto já está pago");
        }
        boleto.setStatus(BoletoStatus.PAID);
        boleto.setPaidAt(java.time.LocalDateTime.now());
        return boletoDAO.update(boleto);
    }
    public PayBoletoResult payBoleto(Long boletoId, Integer accountId, String description) {
        Boleto boleto = getBoletoById(boletoId);
        if (boleto.getStatus() == BoletoStatus.PAID) {
            throw new IllegalArgumentException("Boleto já está pago");
        }
        AccountService accountService = new AccountService();
        org.example.model.Account account = accountService.getAccountById(accountId);
        if (account.getBalance().compareTo(boleto.getAmount()) < 0) {
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
            throw new IllegalArgumentException(
                String.format("Saldo insuficiente. Saldo disponível: %s. Valor necessário: %s",
                    formatter.format(account.getBalance()),
                    formatter.format(boleto.getAmount()))
            );
        }
        BigDecimal newBalance = account.getBalance().subtract(boleto.getAmount());
        account.setBalance(newBalance);
        accountService.updateAccount(account);
        TransactionService transactionService = new TransactionService();
        org.example.model.Transaction transaction = new org.example.model.Transaction();
        transaction.setSourceAccountId(accountId);
        transaction.setAmount(boleto.getAmount());
        transaction.setTransactionType(org.example.model.TransactionTypeEnum.PAYMENT);
        transaction.setDescription(description != null && !description.trim().isEmpty() 
            ? description 
            : "Pagamento de boleto - " + boleto.getTitle());
        transaction.setTransactionDate(java.time.LocalDate.now());
        transaction.setIsRecurring(false);
        org.example.model.Transaction createdTransaction = transactionService.createTransaction(transaction);
        boleto.setStatus(BoletoStatus.PAID);
        boleto.setPaidAt(java.time.LocalDateTime.now());
        Boleto updatedBoleto = boletoDAO.update(boleto);
        PayBoletoResult result = new PayBoletoResult();
        result.setBoleto(updatedBoleto);
        result.setTransaction(createdTransaction);
        result.setNewBalance(newBalance);
        return result;
    }
    public static class PayBoletoResult {
        private Boleto boleto;
        private org.example.model.Transaction transaction;
        private BigDecimal newBalance;
        public Boleto getBoleto() {
            return boleto;
        }
        public void setBoleto(Boleto boleto) {
            this.boleto = boleto;
        }
        public org.example.model.Transaction getTransaction() {
            return transaction;
        }
        public void setTransaction(org.example.model.Transaction transaction) {
            this.transaction = transaction;
        }
        public BigDecimal getNewBalance() {
            return newBalance;
        }
        public void setNewBalance(BigDecimal newBalance) {
            this.newBalance = newBalance;
        }
    }
    public Boleto updateBoleto(Boleto boleto) {
        if (boleto == null) {
            throw new IllegalArgumentException("Boleto não pode ser nulo");
        }
        if (boleto.getId() == null) {
            throw new IllegalArgumentException("ID do boleto é obrigatório para atualização");
        }
        Boleto existingBoleto = getBoletoById(boleto.getId());
        updateStatusBasedOnDueDate(boleto);
        return boletoDAO.update(boleto);
    }
    public void deleteBoleto(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = boletoDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Boleto não encontrado com ID: " + id);
        }
    }
    private void updateStatusBasedOnDueDate(Boleto boleto) {
        if (boleto.getStatus() == BoletoStatus.PAID) {
            return; 
        }
        LocalDate today = LocalDate.now();
        if (boleto.getDueDate() != null) {
            if (boleto.getDueDate().isBefore(today) || boleto.getDueDate().isEqual(today)) {
                boleto.setStatus(BoletoStatus.OVERDUE);
            } else {
                boleto.setStatus(BoletoStatus.OPEN);
            }
        }
    }
}