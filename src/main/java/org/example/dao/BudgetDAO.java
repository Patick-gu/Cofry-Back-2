package org.example.dao;
import org.example.model.Budget;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class BudgetDAO {
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setBudgetId(rs.getInt("budget_id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategoryId(rs.getInt("category_id"));
        BigDecimal amountLimit = rs.getBigDecimal("amount_limit");
        budget.setAmountLimit(amountLimit != null ? amountLimit : BigDecimal.ZERO);
        budget.setPeriodMonth(rs.getInt("period_month"));
        budget.setPeriodYear(rs.getInt("period_year"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            budget.setCreatedAt(createdAt.toLocalDateTime());
        }
        return budget;
    }
    private void prepareBudgetForSave(Budget budget) {
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(LocalDateTime.now());
        }
    }
    public Budget save(Budget budget) {
        prepareBudgetForSave(budget);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO budgets (user_id, category_id, amount_limit, period_month, " +
                        "period_year, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING budget_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, budget.getUserId());
                stmt.setInt(2, budget.getCategoryId());
                stmt.setBigDecimal(3, budget.getAmountLimit());
                stmt.setInt(4, budget.getPeriodMonth());
                stmt.setInt(5, budget.getPeriodYear());
                stmt.setTimestamp(6, Timestamp.valueOf(budget.getCreatedAt()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        budget.setBudgetId(rs.getInt("budget_id"));
                    }
                }
                return budget;
            }
        });
    }
    public Optional<Budget> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets WHERE budget_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBudget(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Budget> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets ORDER BY budget_id";
            List<Budget> budgets = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(mapResultSetToBudget(rs));
                }
                return budgets;
            }
        });
    }
    public List<Budget> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets WHERE user_id = ? ORDER BY period_year DESC, period_month DESC";
            List<Budget> budgets = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        budgets.add(mapResultSetToBudget(rs));
                    }
                    return budgets;
                }
            }
        });
    }
    public List<Budget> findByCategoryId(Integer categoryId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets WHERE category_id = ? ORDER BY period_year DESC, period_month DESC";
            List<Budget> budgets = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        budgets.add(mapResultSetToBudget(rs));
                    }
                    return budgets;
                }
            }
        });
    }
    public List<Budget> findByPeriod(Integer month, Integer year) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets WHERE period_month = ? AND period_year = ? ORDER BY budget_id";
            List<Budget> budgets = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, month);
                stmt.setInt(2, year);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        budgets.add(mapResultSetToBudget(rs));
                    }
                    return budgets;
                }
            }
        });
    }
    public Optional<Budget> findByUserCategoryAndPeriod(Integer userId, Integer categoryId, Integer month, Integer year) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM budgets WHERE user_id = ? AND category_id = ? " +
                        "AND period_month = ? AND period_year = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, categoryId);
                stmt.setInt(3, month);
                stmt.setInt(4, year);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBudget(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public Budget update(Budget budget) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE budgets SET user_id = ?, category_id = ?, amount_limit = ?, " +
                        "period_month = ?, period_year = ? WHERE budget_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, budget.getUserId());
                stmt.setInt(2, budget.getCategoryId());
                stmt.setBigDecimal(3, budget.getAmountLimit());
                stmt.setInt(4, budget.getPeriodMonth());
                stmt.setInt(5, budget.getPeriodYear());
                stmt.setInt(6, budget.getBudgetId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Orçamento não encontrado para atualização: " + budget.getBudgetId());
                }
                return budget;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM budgets WHERE budget_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(Budget budget) {
        if (budget != null && budget.getBudgetId() != null) {
            return delete(budget.getBudgetId());
        }
        return false;
    }
}