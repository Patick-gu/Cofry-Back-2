package org.example.persistence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class DatabaseConnectionExample {
    public static void main(String[] args) {
        try {
            exampleDirectConnection();
            exampleWithJdbcUtil();
        } catch (Exception e) {
            System.err.println("Erro ao conectar com o banco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void exampleDirectConnection() {
        System.out.println("=== Exemplo 1: Conexão Direta ===");
        try (Connection conn = ConnectionFactory.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Total de usuários no banco: " + count);
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT first_name, last_name, email FROM users LIMIT 1")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Primeiro usuário encontrado:");
                        System.out.println("  Nome: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                        System.out.println("  Email: " + rs.getString("email"));
                    } else {
                        System.out.println("Nenhum usuário encontrado no banco.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao executar query: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void exampleWithJdbcUtil() {
        System.out.println("\n=== Exemplo 2: Usando JdbcUtil ===");
        Integer userCount = JdbcUtil.executeWithoutTransaction(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        });
        System.out.println("Total de usuários (via JdbcUtil): " + userCount);
        try {
            JdbcUtil.executeInTransaction(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (plan_id, first_name, last_name, tax_id, email, " +
                        "password_hash, is_active, created_at, updated_at) " +
                        "VALUES (1, 'Exemplo', 'Teste', '000.000.000-00', 'exemplo@teste.com', " +
                        "'hash123', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("Usuário de exemplo criado (linhas afetadas: " + rowsAffected + ")");
                }
                return null;
            });
        } catch (Exception e) {
            System.out.println("Nota: Não foi possível criar usuário de exemplo (pode já existir)");
        }
    }
}