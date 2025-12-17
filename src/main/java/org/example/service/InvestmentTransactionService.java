package org.example.service;
import org.example.dao.InvestmentTransactionDAO;
import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.model.InvestmentTransaction;
import org.example.model.UserAsset;
import org.example.model.Asset;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
public class InvestmentTransactionService {
    private final InvestmentTransactionDAO transactionDAO;
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final UserService userService;
    public InvestmentTransactionService() {
        this.transactionDAO = new InvestmentTransactionDAO();
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.userService = new UserService();
    }
    public InvestmentTransaction processTransaction(InvestmentTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        validateTransaction(transaction);
        userService.getUserById(transaction.getUserId());
        assetDAO.findById(transaction.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + transaction.getAssetId()));
        return JdbcUtil.executeInTransaction(conn -> {
            InvestmentTransaction savedTransaction = transactionDAO.save(transaction);
            updateUserPosition(transaction.getUserId(), transaction.getAssetId(), 
                             transaction.getType(), transaction.getPrice(), transaction.getQuantity());
            return savedTransaction;
        });
    }
    private void updateUserPosition(Integer userId, Integer assetId, String type, 
                                   BigDecimal price, BigDecimal quantity) {
        Optional<UserAsset> existingPosition = userAssetDAO.findByUserIdAndAssetId(userId, assetId);
        UserAsset position;
        if (existingPosition.isPresent()) {
            position = existingPosition.get();
        } else {
            position = new UserAsset();
            position.setUserId(userId);
            position.setAssetId(assetId);
            position.setQuantity(BigDecimal.ZERO);
            position.setAveragePrice(BigDecimal.ZERO);
        }
        if ("Compra".equalsIgnoreCase(type)) {
            BigDecimal totalQuantity = position.getQuantity().add(quantity);
            BigDecimal totalValue = position.getQuantity().multiply(position.getAveragePrice())
                    .add(quantity.multiply(price));
            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newAveragePrice = totalValue.divide(totalQuantity, 8, RoundingMode.HALF_UP);
                position.setAveragePrice(newAveragePrice);
            }
            position.setQuantity(totalQuantity);
        } else if ("Venda".equalsIgnoreCase(type)) {
            BigDecimal newQuantity = position.getQuantity().subtract(quantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Quantidade insuficiente para venda. Posição atual: " + 
                                                 position.getQuantity() + ", tentando vender: " + quantity);
            }
            position.setQuantity(newQuantity);
        } else {
            throw new IllegalArgumentException("Tipo de transação inválido. Use 'Compra' ou 'Venda'");
        }
        userAssetDAO.saveOrUpdate(position);
    }
    public InvestmentTransaction getTransactionById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return transactionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada com ID: " + id));
    }
    public List<InvestmentTransaction> getTransactionsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return transactionDAO.findByUserId(userId);
    }
    public List<org.example.dto.InvestmentTransactionResponseDTO> getTransactionHistoryByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<InvestmentTransaction> transactions = transactionDAO.findByUserId(userId);
        List<org.example.dto.InvestmentTransactionResponseDTO> responseDTOs = new java.util.ArrayList<>();
        for (InvestmentTransaction transaction : transactions) {
            org.example.dto.InvestmentTransactionResponseDTO dto = new org.example.dto.InvestmentTransactionResponseDTO();
            dto.setId(transaction.getId());
            dto.setUserId(transaction.getUserId());
            dto.setAssetId(transaction.getAssetId());
            dto.setType(transaction.getType());
            dto.setPrice(transaction.getPrice());
            dto.setQuantity(transaction.getQuantity());
            dto.setTotalValue(transaction.getTotalValue());
            dto.setTransactionDate(transaction.getTransactionDate());
            dto.setStatus(transaction.getStatus());
            try {
                Asset asset = assetDAO.findById(transaction.getAssetId())
                        .orElseThrow(() -> new RuntimeException("Ativo não encontrado: " + transaction.getAssetId()));
                dto.setAssetTicker(asset.getTicker());
                dto.setAssetName(asset.getName());
            } catch (Exception e) {
                dto.setAssetTicker("N/A");
                dto.setAssetName("Ativo não encontrado");
            }
            responseDTOs.add(dto);
        }
        return responseDTOs;
    }
    public List<InvestmentTransaction> getTransactionsByAssetId(Integer assetId) {
        if (assetId == null) {
            throw new IllegalArgumentException("ID do ativo não pode ser nulo");
        }
        return transactionDAO.findByAssetId(assetId);
    }
    public List<InvestmentTransaction> getTransactionsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo não pode ser nulo ou vazio");
        }
        if (!"Compra".equalsIgnoreCase(type) && !"Venda".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Tipo inválido. Use 'Compra' ou 'Venda'");
        }
        return transactionDAO.findByType(type);
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
    private void validateTransaction(InvestmentTransaction transaction) {
        if (transaction.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (transaction.getAssetId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório");
        }
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo da transação é obrigatório");
        }
        if (!"Compra".equalsIgnoreCase(transaction.getType()) && !"Venda".equalsIgnoreCase(transaction.getType())) {
            throw new IllegalArgumentException("Tipo inválido. Use 'Compra' ou 'Venda'");
        }
        if (transaction.getPrice() == null || transaction.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        if (transaction.getQuantity() == null || transaction.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}