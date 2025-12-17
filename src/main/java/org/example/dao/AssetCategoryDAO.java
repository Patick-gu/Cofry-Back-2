package org.example.dao;
import org.example.model.AssetCategory;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class AssetCategoryDAO {
    private AssetCategory mapResultSetToAssetCategory(ResultSet rs) throws SQLException {
        AssetCategory category = new AssetCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        return category;
    }
    public AssetCategory save(AssetCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO investments.asset_category (name) VALUES (?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        category.setId(rs.getInt("id"));
                    }
                }
                return category;
            }
        });
    }
    public Optional<AssetCategory> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssetCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<AssetCategory> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category ORDER BY name";
            List<AssetCategory> categories = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToAssetCategory(rs));
                }
                return categories;
            }
        });
    }
    public Optional<AssetCategory> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssetCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public AssetCategory update(AssetCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE investments.asset_category SET name = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setInt(2, category.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Categoria não encontrada para atualização: " + category.getId());
                }
                return category;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM investments.asset_category WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
}