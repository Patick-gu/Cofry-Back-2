package org.example.service;
import org.example.dao.AssetCategoryDAO;
import org.example.model.AssetCategory;
import java.util.List;
import java.util.Optional;
public class AssetCategoryService {
    private final AssetCategoryDAO assetCategoryDAO;
    public AssetCategoryService() {
        this.assetCategoryDAO = new AssetCategoryDAO();
    }
    public AssetCategory createAssetCategory(AssetCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        Optional<AssetCategory> existing = assetCategoryDAO.findByName(category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
        }
        return assetCategoryDAO.save(category);
    }
    public AssetCategory getAssetCategoryById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return assetCategoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));
    }
    public List<AssetCategory> getAllAssetCategories() {
        return assetCategoryDAO.findAll();
    }
    public AssetCategory updateAssetCategory(AssetCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório para atualização");
        }
        getAssetCategoryById(category.getId());
        if (category.getName() != null) {
            assetCategoryDAO.findByName(category.getName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(category.getId())) {
                            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
                        }
                    });
        }
        return assetCategoryDAO.update(category);
    }
    public void deleteAssetCategory(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = assetCategoryDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + id);
        }
    }
}