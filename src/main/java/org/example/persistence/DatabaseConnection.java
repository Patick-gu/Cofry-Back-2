package org.example.persistence;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
public class DatabaseConnection {
    private static EntityManagerFactory entityManagerFactory;
    private static final String PERSISTENCE_UNIT_NAME = "CofryPU";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://cofry-db.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/postgres?sslmode=require";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "Cofry.072519";
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", DEFAULT_DB_URL);
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", DEFAULT_DB_USER);
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_DB_PASSWORD);
    private static final String DB_DRIVER = "org.postgresql.Driver";
    public static void initialize() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            boolean persistenceXmlFound = false;
            InputStream persistenceXml = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("META-INF/persistence.xml");
            if (persistenceXml == null) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    persistenceXml = contextClassLoader.getResourceAsStream("META-INF/persistence.xml");
                }
            }
            if (persistenceXml == null) {
                persistenceXml = ClassLoader.getSystemResourceAsStream("META-INF/persistence.xml");
            }
            if (persistenceXml != null) {
                try {
                    persistenceXml.close();
                    persistenceXmlFound = true;
                    System.out.println("persistence.xml encontrado. Tentando inicializar EntityManagerFactory...");
                    entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    System.out.println("EntityManagerFactory inicializado usando persistence.xml");
                    return;
                } catch (Exception e) {
                    System.err.println("Erro ao carregar persistence.xml: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Causa: " + e.getCause().getMessage());
                    }
                    e.printStackTrace();
                    persistenceXmlFound = false;
                }
            }
            if (!persistenceXmlFound) {
                System.out.println("persistence.xml não encontrado no classpath (META-INF/persistence.xml)");
                ClassLoader cl = DatabaseConnection.class.getClassLoader();
                System.out.println("ClassLoader: " + (cl != null ? cl.getClass().getName() : "null"));
                java.net.URL url = cl != null ? cl.getResource("META-INF/persistence.xml") : null;
                System.out.println("URL do recurso: " + (url != null ? url.toString() : "não encontrado"));
            }
            System.out.println("Criando configuração programática...");
            entityManagerFactory = createEntityManagerFactory();
            System.out.println("EntityManagerFactory inicializado usando configuração programática");
        }
    }
    private static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.driver", DB_DRIVER);
        properties.put("javax.persistence.jdbc.url", DB_URL);
        properties.put("javax.persistence.jdbc.user", DB_USER);
        properties.put("javax.persistence.jdbc.password", DB_PASSWORD);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate"); 
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        properties.put("hibernate.connection.pool_size", "10");
        properties.put("hibernate.connection.autocommit", "false");
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.cache.use_query_cache", "false");
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
    }
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            initialize();
        }
        return entityManagerFactory.createEntityManager();
    }
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("EntityManagerFactory fechado com sucesso.");
        }
    }
    public static boolean isInitialized() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            initialize();
        }
        return entityManagerFactory;
    }
}