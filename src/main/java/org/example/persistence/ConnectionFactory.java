package org.example.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class ConnectionFactory {
    private static final String DEFAULT_HOST = "cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "jala.0725.A";
    private static String getDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            return databaseUrl;
        }
        String host = System.getenv().getOrDefault("DB_HOST", DEFAULT_HOST);
        String port = System.getenv().getOrDefault("DB_PORT", DEFAULT_PORT);
        String dbName = System.getenv().getOrDefault("DB_NAME", DEFAULT_DB_NAME);
        return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
    }
    private static String getDatabaseUser() {
        return System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
    }
    private static String getDatabasePassword() {
        return System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
    }
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = getDatabaseUrl();
            String user = getDatabaseUser();
            String password = getDatabasePassword();
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("O Driver do PostgreSQL não foi encontrado! Verifique os Artifacts.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco AWS RDS: " + e.getMessage(), e);
        }
    }
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conectado com sucesso ao banco AWS RDS!");
            System.out.println("URL: " + getDatabaseUrl());
            System.out.println("User: " + getDatabaseUser());
        } catch (RuntimeException | SQLException e) {
            System.out.println("❌ Falha na conexão com AWS RDS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}