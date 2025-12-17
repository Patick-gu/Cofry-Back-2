package org.example.service;
import org.example.dao.TransactionCategoryDAO;
import org.example.model.TransactionCategory;
import java.util.List;
import java.util.Optional;
public class TransactionCategoryService {
    private final TransactionCategoryDAO categoryDAO;
    public TransactionCategoryService() {
        this.categoryDAO = new TransactionCategoryDAO();
    }
    public TransactionCategory createCategory(TransactionCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        Optional<TransactionCategory> existing = categoryDAO.findByName(category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
        }
        return categoryDAO.save(category);
    }
    public TransactionCategory getCategoryById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return categoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));
    }
    public List<TransactionCategory> getAllCategories() {
        return categoryDAO.findAll();
    }
    public Optional<TransactionCategory> getCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return categoryDAO.findByName(name);
    }
    public TransactionCategory updateCategory(TransactionCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório para atualização");
        }
        getCategoryById(category.getCategoryId());
        if (category.getName() != null) {
            Optional<TransactionCategory> existing = categoryDAO.findByName(category.getName());
            if (existing.isPresent() && !existing.get().getCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
            }
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        return categoryDAO.update(category);
    }
    public void deleteCategory(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = categoryDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + id);
        }
    }
}