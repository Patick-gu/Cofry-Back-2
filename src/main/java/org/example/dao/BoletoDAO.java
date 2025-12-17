package org.example.dao;
import org.example.model.Boleto;
import org.example.model.BoletoStatus;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class BoletoDAO {
    private Boleto mapResultSetToBoleto(ResultSet rs) throws SQLException {
        Boleto boleto = new Boleto();
        boleto.setId(rs.getLong("bill_id"));
        boleto.setTitle(rs.getString("title"));
        boleto.setAmount(rs.getBigDecimal("amount"));
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            boleto.setDueDate(dueDate.toLocalDate());
        }
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            boleto.setStatus(BoletoStatus.valueOf(statusStr));
        }
        boleto.setBankCode(rs.getString("bank_code"));
        boleto.setWalletCode(rs.getString("wallet_code"));
        boleto.setOurNumber(rs.getString("our_number"));
        boleto.setBoletoCode(rs.getString("bill_code"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            boleto.setUserId(userId);
        }
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            boleto.setPaidAt(paidAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            boleto.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            boleto.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return boleto;
    }
    private void prepareBoletoForSave(Boleto boleto) {
        if (boleto.getStatus() == null) {
            boleto.setStatus(BoletoStatus.OPEN);
        }
        if (boleto.getCreatedAt() == null) {
            boleto.setCreatedAt(LocalDateTime.now());
        }
        if (boleto.getUpdatedAt() == null) {
            boleto.setUpdatedAt(LocalDateTime.now());
        }
    }
    public Boleto save(Boleto boleto) {
        prepareBoletoForSave(boleto);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, " +
                        "our_number, bill_code, user_id, paid_at, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?::bill_status_enum, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING bill_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, boleto.getTitle());
                stmt.setBigDecimal(2, boleto.getAmount());
                stmt.setDate(3, Date.valueOf(boleto.getDueDate()));
                stmt.setString(4, boleto.getStatus().toString());
                stmt.setString(5, boleto.getBankCode());
                stmt.setString(6, boleto.getWalletCode());
                stmt.setString(7, boleto.getOurNumber());
                stmt.setString(8, boleto.getBoletoCode());
                if (boleto.getUserId() != null) {
                    stmt.setInt(9, boleto.getUserId());
                } else {
                    stmt.setNull(9, Types.INTEGER);
                }
                if (boleto.getPaidAt() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(boleto.getPaidAt()));
                } else {
                    stmt.setNull(10, Types.TIMESTAMP);
                }
                stmt.setTimestamp(11, Timestamp.valueOf(boleto.getCreatedAt()));
                stmt.setTimestamp(12, Timestamp.valueOf(boleto.getUpdatedAt()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boleto.setId(rs.getLong("bill_id"));
                    }
                }
                return boleto;
            }
        });
    }
    public Optional<Boleto> findById(Long id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM bills WHERE bill_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBoleto(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Boleto> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM bills ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boletos.add(mapResultSetToBoleto(rs));
                }
                return boletos;
            }
        });
    }
    public List<Boleto> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM bills WHERE user_id = ? ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        boletos.add(mapResultSetToBoleto(rs));
                    }
                    return boletos;
                }
            }
        });
    }
    public List<Boleto> findByStatus(BoletoStatus status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM bills WHERE status = ?::bill_status_enum ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        boletos.add(mapResultSetToBoleto(rs));
                    }
                    return boletos;
                }
            }
        });
    }
    public Boleto update(Boleto boleto) {
        boleto.setUpdatedAt(LocalDateTime.now());
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE bills SET title = ?, amount = ?, due_date = ?, status = ?::bill_status_enum, " +
                        "bank_code = ?, wallet_code = ?, our_number = ?, bill_code = ?, user_id = ?, " +
                        "paid_at = ?, updated_at = ? WHERE bill_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, boleto.getTitle());
                stmt.setBigDecimal(2, boleto.getAmount());
                stmt.setDate(3, Date.valueOf(boleto.getDueDate()));
                stmt.setString(4, boleto.getStatus().toString());
                stmt.setString(5, boleto.getBankCode());
                stmt.setString(6, boleto.getWalletCode());
                stmt.setString(7, boleto.getOurNumber());
                stmt.setString(8, boleto.getBoletoCode());
                if (boleto.getUserId() != null) {
                    stmt.setInt(9, boleto.getUserId());
                } else {
                    stmt.setNull(9, Types.INTEGER);
                }
                if (boleto.getPaidAt() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(boleto.getPaidAt()));
                } else {
                    stmt.setNull(10, Types.TIMESTAMP);
                }
                stmt.setTimestamp(11, Timestamp.valueOf(boleto.getUpdatedAt()));
                stmt.setLong(12, boleto.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Boleto não encontrado para atualização: " + boleto.getId());
                }
                return boleto;
            }
        });
    }
    public boolean delete(Long id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM bills WHERE bill_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(Boleto boleto) {
        if (boleto != null && boleto.getId() != null) {
            return delete(boleto.getId());
        }
        return false;
    }
}