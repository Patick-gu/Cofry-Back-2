package org.example.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class ConnectionFactory {
    private static final String DEFAULT_HOST = "db.qcgvvrbwtjijyylxxugb.supabase.co";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "Cofry.072519";
    private static String getDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            if (databaseUrl.startsWith("jdbc:")) {
                return databaseUrl;
            }
            if (databaseUrl.startsWith("postgresql://")) {
                return convertRenderDatabaseUrl(databaseUrl);
            }
            return databaseUrl;
        }
        String host = System.getenv().getOrDefault("DB_HOST", DEFAULT_HOST);
        String port = System.getenv().getOrDefault("DB_PORT", DEFAULT_PORT);
        String dbName = System.getenv().getOrDefault("DB_NAME", DEFAULT_DB_NAME);
        if (host.contains("supabase.co")) {
            return String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, dbName);
        }
        return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
    }
    private static String convertRenderDatabaseUrl(String renderUrl) {
        try {
            if (renderUrl.startsWith("postgresql://")) {
                renderUrl = renderUrl.replace("postgresql://", "jdbc:postgresql://");
                if (!renderUrl.contains("?")) {
                    renderUrl += "?sslmode=require";
                } else if (!renderUrl.contains("sslmode")) {
                    renderUrl += "&sslmode=require";
                }
                return renderUrl;
            }
            return renderUrl;
        } catch (Exception e) {
            System.err.println("Erro ao converter DATABASE_URL: " + e.getMessage());
            return renderUrl;
        }
    }
    private static String getDatabaseUser() {
        String dbUser = System.getenv("DB_USER");
        if (dbUser != null && !dbUser.trim().isEmpty()) {
            return dbUser;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                java.net.URI uri = new java.net.URI(databaseUrl.replace("postgresql://", "http://"));
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    return userInfo.split(":")[0];
                }
            } catch (Exception e) {
                System.err.println("Erro ao extrair usuário de DATABASE_URL: " + e.getMessage());
            }
        }
        return DEFAULT_USER;
    }
    private static String getDatabasePassword() {
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword != null && !dbPassword.trim().isEmpty()) {
            return dbPassword;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                java.net.URI uri = new java.net.URI(databaseUrl.replace("postgresql://", "http://"));
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    return userInfo.split(":")[1];
                }
            } catch (Exception e) {
                System.err.println("Erro ao extrair senha de DATABASE_URL: " + e.getMessage());
            }
        }
        return DEFAULT_PASSWORD;
    }
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = getDatabaseUrl();
            String user = getDatabaseUser();
            String password = getDatabasePassword();
            
            System.out.println("🔌 Tentando conectar ao banco de dados...");
            System.out.println("   URL: " + url.replace(password, "***"));
            System.out.println("   User: " + user);
            
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Conexão estabelecida com sucesso!");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("O Driver do PostgreSQL não foi encontrado! Verifique os Artifacts.", e);
        } catch (SQLException e) {
            String url = getDatabaseUrl();
            String user = getDatabaseUser();
            System.err.println("❌ Erro ao conectar ao banco de dados!");
            System.err.println("   URL tentada: " + (url != null ? url.replace(getDatabasePassword(), "***") : "null"));
            System.err.println("   User: " + user);
            System.err.println("   Erro: " + e.getMessage());
            
            if (e.getMessage() != null && e.getMessage().contains("Network is unreachable")) {
                System.err.println("\n💡 Possíveis soluções:");
                System.err.println("   1. Verifique sua conexão com a internet");
                System.err.println("   2. Verifique se o host do banco está acessível");
                System.err.println("   3. Verifique firewall/antivírus bloqueando a conexão");
                System.err.println("   4. Teste conectividade: ping " + (url != null && url.contains("//") ? 
                    url.split("//")[1].split(":")[0] : "host"));
            }
            
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conectado com sucesso ao banco de dados!");
            System.out.println("URL: " + getDatabaseUrl());
            System.out.println("User: " + getDatabaseUser());
        } catch (RuntimeException | SQLException e) {
            System.out.println("❌ Falha na conexão com banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}