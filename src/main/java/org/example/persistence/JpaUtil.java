package org.example.persistence;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
public class JpaUtil {
    public static <T> T executeInTransaction(EntityManagerOperation<T> operation) {
        EntityManager em = DatabaseConnection.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            T result = operation.execute(em);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erro ao executar transação: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    public static void executeInTransaction(EntityManagerVoidOperation operation) {
        executeInTransaction(em -> {
            operation.execute(em);
            return null;
        });
    }
    public static <T> T executeWithoutTransaction(EntityManagerOperation<T> operation) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            return operation.execute(em);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar operação: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    @FunctionalInterface
    public interface EntityManagerOperation<T> {
        T execute(EntityManager em);
    }
    @FunctionalInterface
    public interface EntityManagerVoidOperation {
        void execute(EntityManager em);
    }
}