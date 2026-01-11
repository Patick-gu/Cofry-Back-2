package org.example.config;

public class SupabaseConfig {
    private static final String SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY;
    private static final String SUPABASE_SERVICE_ROLE_KEY;
    private static final String DATABASE_URL;
    private static final String DATABASE_USER;
    private static final String DATABASE_PASSWORD;
    
    static {
        SUPABASE_URL = getEnvOrDefault("SUPABASE_URL", "https://qcgvvrbwtjijyylxxugb.supabase.co");
        SUPABASE_ANON_KEY = getEnvOrDefault("SUPABASE_ANON_KEY", "");
        SUPABASE_SERVICE_ROLE_KEY = getEnvOrDefault("SUPABASE_SERVICE_ROLE_KEY", "");
        
        DATABASE_URL = getEnvOrDefault("DATABASE_URL", 
            "jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require");
        DATABASE_USER = getEnvOrDefault("DB_USER", "postgres.qcgvvrbwtjijyylxxugb");
        DATABASE_PASSWORD = getEnvOrDefault("DB_PASSWORD", "Cofry.072519");
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    
    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }
    
    public static String getSupabaseAnonKey() {
        return SUPABASE_ANON_KEY;
    }
    
    public static String getSupabaseServiceRoleKey() {
        return SUPABASE_SERVICE_ROLE_KEY;
    }
    
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }
    
    public static String getDatabaseUser() {
        return DATABASE_USER;
    }
    
    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }
    
    public static String getAuthApiUrl() {
        return SUPABASE_URL + "/auth/v1";
    }
}
