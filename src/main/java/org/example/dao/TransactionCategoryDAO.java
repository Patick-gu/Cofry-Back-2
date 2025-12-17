package org.example.dao;
import org.example.model.TransactionCategory;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class TransactionCategoryDAO {
    private TransactionCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        TransactionCategory category = new TransactionCategory();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setIconCode(rs.getString("icon_code"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }
        return category;
    }
    private void prepareCategoryForSave(TransactionCategory category) {
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
    }
    public TransactionCategory save(TransactionCategory category) {
        prepareCategoryForSave(category);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO transaction_categories (name, icon_code, created_at) " +
                        "VALUES (?, ?, ?) RETURNING category_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getIconCode());
                if (category.getCreatedAt() != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(category.getCreatedAt()));
                } else {
                    stmt.setTimestamp(3, null);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt("category_id"));
                    }
                }
                return category;
            }
        });
    }
    public Optional<TransactionCategory> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories WHERE category_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<TransactionCategory> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories ORDER BY category_id";
            List<TransactionCategory> categories = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
                return categories;
            }
        });
    }
    public Optional<TransactionCategory> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public TransactionCategory update(TransactionCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE transaction_categories SET name = ?, icon_code = ? " +
                        "WHERE category_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getIconCode());
                stmt.setInt(3, category.getCategoryId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Categoria não encontrada para atualização: " + category.getCategoryId());
                }
                return category;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM transaction_categories WHERE category_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(TransactionCategory category) {
        if (category != null && category.getCategoryId() != null) {
            return delete(category.getCategoryId());
        }
        return false;
    }
}