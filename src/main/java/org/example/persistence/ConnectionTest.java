package org.example.persistence;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class ConnectionTest {
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("TESTE DE CONEXÃO COM BANCO DE DADOS");
        System.out.println("===========================================");
        System.out.println();
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("✅ Conectado com sucesso ao banco AWS RDS!");
            System.out.println();
            System.out.println("2. Informações do Banco de Dados:");
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("   - Banco de Dados: " + metaData.getDatabaseProductName());
            System.out.println("   - Versão: " + metaData.getDatabaseProductVersion());
            System.out.println("   - Driver: " + metaData.getDriverName());
            System.out.println("   - Versão do Driver: " + metaData.getDriverVersion());
            System.out.println("   - URL: " + metaData.getURL());
            System.out.println("   - Usuário: " + metaData.getUserName());
            System.out.println();
            System.out.println("3. Testando query simples...");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT current_database(), current_user, version()")) {
                if (rs.next()) {
                    System.out.println("   ✓ Query executada com sucesso!");
                    System.out.println("   - Database: " + rs.getString(1));
                    System.out.println("   - User: " + rs.getString(2));
                    System.out.println("   - Version: " + rs.getString(3).split(",")[0]);
                }
            }
            System.out.println();
            System.out.println("4. Verificando tabelas no banco...");
            String[] tables = {"users", "accounts", "transactions", "subscription_plans", 
                              "addresses", "transaction_categories", "budgets", "savings_goals"};
            int foundTables = 0;
            for (String tableName : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + tableName + " LIMIT 1")) {
                    if (rs.next()) {
                        System.out.println("   ✓ Tabela '" + tableName + "' encontrada!");
                        foundTables++;
                    }
                } catch (SQLException e) {
                    System.out.println("   ✗ Tabela '" + tableName + "' não encontrada ou erro: " + 
                                     e.getMessage().split("\n")[0]);
                }
            }
            System.out.println();
            System.out.println("   Total: " + foundTables + "/" + tables.length + " tabelas encontradas.");
            System.out.println();
            System.out.println("===========================================");
            System.out.println("TESTE CONCLUÍDO COM SUCESSO!");
            System.out.println("===========================================");
        } catch (SQLException e) {
            System.err.println();
            System.err.println("===========================================");
            System.err.println("ERRO AO CONECTAR COM O BANCO DE DADOS!");
            System.err.println("===========================================");
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println();
            System.err.println("Possíveis causas:");
            System.err.println("1. Banco AWS RDS não está acessível");
            System.err.println("2. Credenciais incorretas (usuário/senha)");
            System.err.println("3. Security Group bloqueando conexão (verifique na AWS)");
            System.err.println("4. Porta 5432 não está aberta no Security Group");
            System.err.println("5. VPC/Subnet incorretas");
            System.err.println();
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.err.println();
            System.err.println("===========================================");
            System.err.println("ERRO AO CONECTAR COM O BANCO DE DADOS!");
            System.err.println("===========================================");
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println();
            e.printStackTrace();
        }
    }
}