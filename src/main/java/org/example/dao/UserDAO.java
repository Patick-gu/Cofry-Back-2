package org.example.dao;

import org.example.model.User;
import org.example.persistence.JdbcUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDAO {
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        
        Object userIdObj = rs.getObject("user_id");
        if (userIdObj != null) {
            user.setUserId(UUID.fromString(userIdObj.toString()));
        }
        
        int planId = rs.getInt("plan_id");
        if (rs.wasNull()) {
            user.setPlanId(1);
        } else {
            user.setPlanId(planId);
        }
        
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setTaxId(rs.getString("tax_id"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        user.setIsActive(rs.getBoolean("is_active"));
        if (rs.wasNull()) {
            user.setIsActive(true);
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
    
    private void prepareUserForSave(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(now);
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getPlanId() == null) {
            user.setPlanId(1);
        }
    }
    
    public User save(User user) {
        prepareUserForSave(user);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO users (user_id, plan_id, first_name, last_name, tax_id, email, phone_number, " +
                        "date_of_birth, is_active, created_at, updated_at) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, user.getUserId());
                stmt.setInt(2, user.getPlanId());
                stmt.setString(3, user.getFirstName());
                stmt.setString(4, user.getLastName());
                stmt.setString(5, user.getTaxId());
                stmt.setString(6, user.getEmail());
                stmt.setString(7, user.getPhoneNumber());
                stmt.setDate(8, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
                stmt.setBoolean(9, user.getIsActive());
                stmt.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
                stmt.setTimestamp(11, Timestamp.valueOf(user.getUpdatedAt()));
                
                stmt.executeUpdate();
                return user;
            }
        });
    }
    
    public Optional<User> findById(UUID id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE user_id = ?::uuid";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    public List<User> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users ORDER BY created_at DESC";
            List<User> users = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            }
        });
    }
    
    public Optional<User> findByEmail(String email) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    public Optional<User> findByTaxId(String taxId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE tax_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, taxId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    public List<User> findByStatus(Boolean isActive) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE is_active = ? ORDER BY created_at DESC";
            List<User> users = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, isActive);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                    return users;
                }
            }
        });
    }
    
    public List<User> findByPlanId(Integer planId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE plan_id = ? ORDER BY created_at DESC";
            List<User> users = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, planId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                    return users;
                }
            }
        });
    }
    
    public User update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE users SET plan_id = ?, first_name = ?, last_name = ?, tax_id = ?, " +
                        "email = ?, phone_number = ?, date_of_birth = ?, " +
                        "is_active = ?, updated_at = ? WHERE user_id = ?::uuid";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, user.getPlanId());
                stmt.setString(2, user.getFirstName());
                stmt.setString(3, user.getLastName());
                stmt.setString(4, user.getTaxId());
                stmt.setString(5, user.getEmail());
                stmt.setString(6, user.getPhoneNumber());
                stmt.setDate(7, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
                stmt.setBoolean(8, user.getIsActive());
                stmt.setTimestamp(9, Timestamp.valueOf(user.getUpdatedAt()));
                stmt.setObject(10, user.getUserId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Usuário não encontrado para atualização: " + user.getUserId());
                }
                return user;
            }
        });
    }
    
    public boolean delete(UUID id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM users WHERE user_id = ?::uuid";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    public boolean delete(User user) {
        if (user != null && user.getUserId() != null) {
            return delete(user.getUserId());
        }
        return false;
    }
}
