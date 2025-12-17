package org.example.dao;
import org.example.model.Address;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class AddressDAO {
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getInt("address_id"));
        address.setUserId(rs.getInt("user_id"));
        address.setStreet(rs.getString("street"));
        address.setNumber(rs.getString("number"));
        address.setComplement(rs.getString("complement"));
        address.setNeighborhood(rs.getString("neighborhood"));
        address.setCity(rs.getString("city"));
        address.setState(rs.getString("state"));
        address.setZipCode(rs.getString("zip_code"));
        address.setCountry(rs.getString("country"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            address.setCreatedAt(createdAt.toLocalDateTime());
        }
        return address;
    }
    private void prepareAddressForSave(Address address) {
        if (address.getCountry() == null) {
            address.setCountry("Brazil");
        }
        if (address.getCreatedAt() == null) {
            address.setCreatedAt(LocalDateTime.now());
        }
    }
    public Address save(Address address) {
        prepareAddressForSave(address);
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO addresses (user_id, street, number, complement, neighborhood, " +
                        "city, state, zip_code, country, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING address_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, address.getUserId());
                stmt.setString(2, address.getStreet());
                stmt.setString(3, address.getNumber());
                stmt.setString(4, address.getComplement());
                stmt.setString(5, address.getNeighborhood());
                stmt.setString(6, address.getCity());
                stmt.setString(7, address.getState());
                stmt.setString(8, address.getZipCode());
                stmt.setString(9, address.getCountry());
                stmt.setTimestamp(10, Timestamp.valueOf(address.getCreatedAt()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        address.setAddressId(rs.getInt("address_id"));
                    }
                }
                return address;
            }
        });
    }
    public Optional<Address> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM addresses WHERE address_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAddress(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    public List<Address> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM addresses ORDER BY address_id";
            List<Address> addresses = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapResultSetToAddress(rs));
                }
                return addresses;
            }
        });
    }
    public List<Address> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY address_id";
            List<Address> addresses = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        addresses.add(mapResultSetToAddress(rs));
                    }
                    return addresses;
                }
            }
        });
    }
    public List<Address> findByCity(String city) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM addresses WHERE city = ? ORDER BY address_id";
            List<Address> addresses = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, city);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        addresses.add(mapResultSetToAddress(rs));
                    }
                    return addresses;
                }
            }
        });
    }
    public List<Address> findByState(String state) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM addresses WHERE state = ? ORDER BY address_id";
            List<Address> addresses = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, state);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        addresses.add(mapResultSetToAddress(rs));
                    }
                    return addresses;
                }
            }
        });
    }
    public Address update(Address address) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE addresses SET user_id = ?, street = ?, number = ?, complement = ?, " +
                        "neighborhood = ?, city = ?, state = ?, zip_code = ?, country = ? " +
                        "WHERE address_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, address.getUserId());
                stmt.setString(2, address.getStreet());
                stmt.setString(3, address.getNumber());
                stmt.setString(4, address.getComplement());
                stmt.setString(5, address.getNeighborhood());
                stmt.setString(6, address.getCity());
                stmt.setString(7, address.getState());
                stmt.setString(8, address.getZipCode());
                stmt.setString(9, address.getCountry());
                stmt.setInt(10, address.getAddressId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Endereço não encontrado para atualização: " + address.getAddressId());
                }
                return address;
            }
        });
    }
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM addresses WHERE address_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    public boolean delete(Address address) {
        if (address != null && address.getAddressId() != null) {
            return delete(address.getAddressId());
        }
        return false;
    }
}