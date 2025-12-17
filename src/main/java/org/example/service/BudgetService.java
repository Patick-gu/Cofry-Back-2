package org.example.service;
import org.example.dao.BudgetDAO;
import org.example.dao.UserDAO;
import org.example.dao.TransactionCategoryDAO;
import org.example.model.Budget;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final TransactionCategoryDAO categoryDAO;
    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.userDAO = new UserDAO();
        this.categoryDAO = new TransactionCategoryDAO();
    }
    public Budget createBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Orçamento não pode ser nulo");
        }
        validateBudget(budget);
        if (budget.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(budget.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + budget.getUserId()));
        if (budget.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório");
        }
        categoryDAO.findById(budget.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + budget.getCategoryId()));
        Optional<Budget> existing = budgetDAO.findByUserCategoryAndPeriod(
                budget.getUserId(),
                budget.getCategoryId(),
                budget.getPeriodMonth(),
                budget.getPeriodYear()
        );
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um orçamento para este usuário, categoria e período"
            );
        }
        return budgetDAO.save(budget);
    }
    public Budget getBudgetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return budgetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado com ID: " + id));
    }
    public List<Budget> getAllBudgets() {
        return budgetDAO.findAll();
    }
    public List<Budget> getBudgetsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return budgetDAO.findByUserId(userId);
    }
    public List<Budget> getBudgetsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        return budgetDAO.findByCategoryId(categoryId);
    }
    public List<Budget> getBudgetsByPeriod(Integer month, Integer year) {
        if (month == null || year == null) {
            throw new IllegalArgumentException("Mês e ano são obrigatórios");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês deve estar entre 1 e 12");
        }
        return budgetDAO.findByPeriod(month, year);
    }
    public Optional<Budget> getBudgetByUserCategoryAndPeriod(Integer userId, Integer categoryId, Integer month, Integer year) {
        if (userId == null || categoryId == null || month == null || year == null) {
            throw new IllegalArgumentException("Todos os parâmetros são obrigatórios");
        }
        return budgetDAO.findByUserCategoryAndPeriod(userId, categoryId, month, year);
    }
    public Budget updateBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Orçamento não pode ser nulo");
        }
        if (budget.getBudgetId() == null) {
            throw new IllegalArgumentException("ID do orçamento é obrigatório para atualização");
        }
        getBudgetById(budget.getBudgetId());
        validateBudget(budget);
        if (budget.getUserId() != null) {
            userDAO.findById(budget.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + budget.getUserId()));
        }
        if (budget.getCategoryId() != null) {
            categoryDAO.findById(budget.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + budget.getCategoryId()));
        }
        return budgetDAO.update(budget);
    }
    public void deleteBudget(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = budgetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Orçamento não encontrado com ID: " + id);
        }
    }
    private void validateBudget(Budget budget) {
        if (budget.getAmountLimit() == null || budget.getAmountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limite do orçamento deve ser maior que zero");
        }
        if (budget.getPeriodMonth() == null || budget.getPeriodMonth() < 1 || budget.getPeriodMonth() > 12) {
            throw new IllegalArgumentException("Mês deve estar entre 1 e 12");
        }
        if (budget.getPeriodYear() == null || budget.getPeriodYear() < 1900) {
            throw new IllegalArgumentException("Ano inválido");
        }
    }
}