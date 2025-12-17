package org.example.service;
import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.model.UserAsset;
import java.math.BigDecimal;
import java.util.List;
public class UserAssetService {
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final UserService userService;
    public UserAssetService() {
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.userService = new UserService();
    }
    public UserAsset saveOrUpdateUserAsset(UserAsset userAsset) {
        if (userAsset == null) {
            throw new IllegalArgumentException("Posição não pode ser nula");
        }
        validateUserAsset(userAsset);
        userService.getUserById(userAsset.getUserId());
        assetDAO.findById(userAsset.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + userAsset.getAssetId()));
        return userAssetDAO.saveOrUpdate(userAsset);
    }
    public UserAsset getUserAssetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return userAssetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Posição não encontrada com ID: " + id));
    }
    public UserAsset getUserAssetByUserIdAndAssetId(Integer userId, Integer assetId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        if (assetId == null) {
            throw new IllegalArgumentException("ID do ativo não pode ser nulo");
        }
        return userAssetDAO.findByUserIdAndAssetId(userId, assetId)
                .orElse(null); 
    }
    public List<UserAsset> getUserAssetsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return userAssetDAO.findByUserId(userId);
    }
    public void deleteUserAsset(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = userAssetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Posição não encontrada com ID: " + id);
        }
    }
    private void validateUserAsset(UserAsset userAsset) {
        if (userAsset.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (userAsset.getAssetId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório");
        }
        if (userAsset.getQuantity() == null || userAsset.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior ou igual a zero");
        }
        if (userAsset.getAveragePrice() == null || userAsset.getAveragePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço médio deve ser maior que zero");
        }
    }
}