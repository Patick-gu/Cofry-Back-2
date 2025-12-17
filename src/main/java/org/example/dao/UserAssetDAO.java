package org.example.dao;
import org.example.model.UserAsset;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class UserAssetDAO {
    private UserAsset mapResultSetToUserAsset(ResultSet rs) throws SQLException {
        UserAsset userAsset = new UserAsset();
        userAsset.setId(rs.getInt("id"));
        userAsset.setUserId(rs.getInt("user_id"));
        userAsset.setAssetId(rs.getInt("asset_id"));
        userAsset.setQuantity(rs.getBigDecimal("quantity"));
        userAsset.setAveragePrice(rs.getBigDecimal("average_price"));
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) {
            userAsset.setLastUpdated(lastUpdated.toLocalDateTime());
        }
        return userAsset;
    }
    public UserAsset saveOrUpdate(UserAsset userAsset) {
        return JdbcUtil.executeInTransaction(conn -> {
            Optional<UserAsset> existing = findByUserIdAndAssetId(userAsset.getUserId(), userAsset.getAssetId());
            if (existing.isPresent()) {
                UserAsset existingAsset = existing.get();
                existingAsset.setQuantity(userAsset.getQuantity());
                existingAsset.setAveragePrice(userAsset.getAveragePrice());
                existingAsset.setLastUpdated(LocalDateTime.now());
                return update(existingAsset);
            } else {
                return save(userAsset);
            }
        });
    }
    public UserAsset save(UserAsset userAsset) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO investments.user_asset (user_id, asset_id, quantity, average_price, last_updated) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userAsset.getUserId());
                stmt.setInt(2, userAsset.getAssetId());
                stmt.setBigDecimal(3, userAsset.getQuantity());
                stmt.setBigDecimal(4, userAsset.getAveragePrice());
                stmt.setTimestamp(5, Timestamp.valueOf(
                    userAsset.getLastUpdated() != null ? userAsset.getLastUpdated() : LocalDateTime.now()
                ));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userAsset.setId(rs.getInt("id"));
                    }
                }
                return userAsset;
            }
        });
    }
    public Optional<UserAsset> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.user_asset WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUserAsset(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public Optional<UserAsset> findByUserIdAndAssetId(Integer userId, Integer assetId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.user_asset WHERE user_id = ? AND asset_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, assetId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUserAsset(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<UserAsset> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.user_asset WHERE user_id = ? ORDER BY last_updated DESC";
            List<UserAsset> positions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        positions.add(mapResultSetToUserAsset(rs));
                    }
                    return positions;
                }
            }
        });
    }
    public List<UserAsset> findByAssetId(Integer assetId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.user_asset WHERE asset_id = ? ORDER BY last_updated DESC";
            List<UserAsset> positions = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, assetId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        positions.add(mapResultSetToUserAsset(rs));
                    }
                    return positions;
                }
            }
        });
    }
    public UserAsset update(UserAsset userAsset) {
        userAsset.setLastUpdated(LocalDateTime.now());
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE investments.user_asset SET quantity = ?, average_price = ?, last_updated = ? " +
                        "WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, userAsset.getQuantity());
                stmt.setBigDecimal(2, userAsset.getAveragePrice());
                stmt.setTimestamp(3, Timestamp.valueOf(userAsset.getLastUpdated()));
                stmt.setInt(4, userAsset.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Posição não encontrada para atualização: " + userAsset.getId());
                }
                return userAsset;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM investments.user_asset WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(UserAsset userAsset) {
        if (userAsset != null && userAsset.getId() != null) {
            return delete(userAsset.getId());
        }
        return false;
    }
}