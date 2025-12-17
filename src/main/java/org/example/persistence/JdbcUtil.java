package org.example.persistence;
import java.sql.Connection;
import java.sql.SQLException;
public class JdbcUtil {
    public static <T> T executeInTransaction(ConnectionOperation<T> operation) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            T result = operation.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Erro ao fazer rollback: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw new RuntimeException("Erro ao executar transação: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao fechar conexão: " + e.getMessage(), e);
                }
            }
        }
    }
    public static void executeInTransaction(ConnectionVoidOperation operation) {
        executeInTransaction(conn -> {
            operation.execute(conn);
            return null;
        });
    }
    public static <T> T executeWithoutTransaction(ConnectionOperation<T> operation) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return operation.execute(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao executar operação: " + e.getMessage(), e);
        }
    }
    @FunctionalInterface
    public interface ConnectionOperation<T> {
        T execute(Connection conn) throws SQLException;
    }
    @FunctionalInterface
    public interface ConnectionVoidOperation {
        void execute(Connection conn) throws SQLException;
    }
}