package org.example.dao;
import org.example.model.SubscriptionPlan;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class SubscriptionPlanDAO {
    private SubscriptionPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setName(rs.getString("name"));
        BigDecimal price = rs.getBigDecimal("price");
        plan.setPrice(price != null ? price : BigDecimal.ZERO);
        plan.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            plan.setCreatedAt(createdAt.toLocalDateTime());
        }
        return plan;
    }
    private void preparePlanForSave(SubscriptionPlan plan) {
        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }
        if (plan.getPrice() == null) {
            plan.setPrice(BigDecimal.ZERO);
        }
    }
    public SubscriptionPlan save(SubscriptionPlan plan) {
        preparePlanForSave(plan);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO subscription_plans (name, price, description, created_at) " +
                        "VALUES (?, ?, ?, ?) RETURNING plan_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plan.getName());
                stmt.setBigDecimal(2, plan.getPrice());
                stmt.setString(3, plan.getDescription());
                stmt.setTimestamp(4, Timestamp.valueOf(plan.getCreatedAt()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        plan.setPlanId(rs.getInt("plan_id"));
                    }
                }
                return plan;
            }
        });
    }
    public Optional<SubscriptionPlan> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans WHERE plan_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlan(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<SubscriptionPlan> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans ORDER BY plan_id";
            List<SubscriptionPlan> plans = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapResultSetToPlan(rs));
                }
                return plans;
            }
        });
    }
    public Optional<SubscriptionPlan> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlan(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public SubscriptionPlan update(SubscriptionPlan plan) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE subscription_plans SET name = ?, price = ?, description = ? " +
                        "WHERE plan_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plan.getName());
                stmt.setBigDecimal(2, plan.getPrice());
                stmt.setString(3, plan.getDescription());
                stmt.setInt(4, plan.getPlanId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Plano não encontrado para atualização: " + plan.getPlanId());
                }
                return plan;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM subscription_plans WHERE plan_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(SubscriptionPlan plan) {
        if (plan != null && plan.getPlanId() != null) {
            return delete(plan.getPlanId());
        }
        return false;
    }
}