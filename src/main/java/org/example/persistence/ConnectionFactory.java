package org.example.persistence;

import org.example.config.SupabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    private static final ThreadLocal<String> currentUserToken = new ThreadLocal<>();
    
    public static void setUserToken(String jwtToken) {
        currentUserToken.set(jwtToken);
    }
    
    public static void clearUserToken() {
        currentUserToken.remove();
    }
    
    public static String getUserToken() {
        return currentUserToken.get();
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            
            String url = SupabaseConfig.getDatabaseUrl();
            String user = SupabaseConfig.getDatabaseUser();
            String password = SupabaseConfig.getDatabasePassword();
            
            Connection conn = DriverManager.getConnection(url, user, password);
            
            String token = currentUserToken.get();
            if (token != null && !token.trim().isEmpty()) {
                configureRLSSession(conn, token);
            }
            
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("O Driver do PostgreSQL não foi encontrado! Verifique os Artifacts.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
    
    private static void configureRLSSession(Connection conn, String jwtToken) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(String.format("SET request.jwt.claim.sub = '%s'", extractUserIdFromToken(jwtToken)));
            stmt.execute(String.format("SET request.jwt.claims = '%s'", jwtToken));
        }
    }
    
    private static String extractUserIdFromToken(String jwtToken) {
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return "";
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.google.gson.JsonObject jsonPayload = new com.google.gson.Gson().fromJson(payload, com.google.gson.JsonObject.class);
            
            if (jsonPayload.has("sub")) {
                return jsonPayload.get("sub").getAsString();
            }
            
            return "";
        } catch (Exception e) {
            System.err.println("Erro ao extrair userId do token JWT: " + e.getMessage());
            return "";
        }
    }
    
    public static Connection getAdminConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            
            String url = SupabaseConfig.getDatabaseUrl();
            String user = SupabaseConfig.getDatabaseUser();
            String password = SupabaseConfig.getDatabasePassword();
            
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("O Driver do PostgreSQL não foi encontrado!", e);
        }
    }
}
