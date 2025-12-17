package org.example.service;
import org.example.dao.AssetDAO;
import org.example.model.Asset;
import java.util.List;
import java.util.Optional;
public class AssetService {
    private final AssetDAO assetDAO;
    private final AssetCategoryService assetCategoryService;
    public AssetService() {
        this.assetDAO = new AssetDAO();
        this.assetCategoryService = new AssetCategoryService();
    }
    public Asset createAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Ativo não pode ser nulo");
        }
        validateAsset(asset);
        assetCategoryService.getAssetCategoryById(asset.getCategoryId());
        Optional<Asset> existing = assetDAO.findByTicker(asset.getTicker());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe um ativo com o ticker: " + asset.getTicker());
        }
        return assetDAO.save(asset);
    }
    public Asset getAssetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return assetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + id));
    }
    public Asset getAssetByTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker não pode ser nulo ou vazio");
        }
        return assetDAO.findByTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ticker: " + ticker));
    }
    public List<Asset> getAllAssets() {
        return assetDAO.findAll();
    }
    public List<Asset> getActiveAssets() {
        return assetDAO.findActive();
    }
    public List<Asset> getAssetsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        assetCategoryService.getAssetCategoryById(categoryId);
        return assetDAO.findByCategoryId(categoryId);
    }
    public Asset updateAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Ativo não pode ser nulo");
        }
        if (asset.getId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório para atualização");
        }
        Asset existingAsset = getAssetById(asset.getId());
        if (asset.getCategoryId() != null) {
            assetCategoryService.getAssetCategoryById(asset.getCategoryId());
        }
        if (asset.getTicker() != null && !asset.getTicker().equals(existingAsset.getTicker())) {
            Optional<Asset> assetWithTicker = assetDAO.findByTicker(asset.getTicker());
            if (assetWithTicker.isPresent() && !assetWithTicker.get().getId().equals(asset.getId())) {
                throw new IllegalArgumentException("Já existe um ativo com o ticker: " + asset.getTicker());
            }
        }
        validateAsset(asset);
        return assetDAO.update(asset);
    }
    public void deleteAsset(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = assetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Ativo não encontrado com ID: " + id);
        }
    }
    private void validateAsset(Asset asset) {
        if (asset.getTicker() == null || asset.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker é obrigatório");
        }
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do ativo é obrigatório");
        }
        if (asset.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório");
        }
        if (asset.getApiIdentifier() == null || asset.getApiIdentifier().trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador da API é obrigatório");
        }
    }
}