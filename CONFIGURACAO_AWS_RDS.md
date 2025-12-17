# ☁️ Configuração do Banco de Dados AWS RDS

## 📋 Visão Geral

O sistema foi configurado para usar **apenas o banco de dados AWS RDS** a partir de agora. O banco local (Cofry-local) não é mais utilizado.

---

## 🔧 Configuração Atual

### Banco de Dados AWS

- **Host:** `cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com`
- **Porta:** `5432`
- **Banco:** `postgres` (padrão, pode ser alterado via variável de ambiente)
- **Usuário:** `postgres` (padrão)
- **Região:** `us-east-1` (Norte da Virgínia)

---

## 🔐 Variáveis de Ambiente (Recomendado)

Para maior segurança e flexibilidade, é recomendado usar variáveis de ambiente ao invés de valores hardcoded.

### Opção 1: Variáveis Individuais

```bash
# Windows (PowerShell)
$env:DB_HOST="cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com"
$env:DB_PORT="5432"
$env:DB_NAME="postgres"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua_senha_aqui"

# Windows (CMD)
set DB_HOST=cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com
set DB_PORT=5432
set DB_NAME=postgres
set DB_USER=postgres
set DB_PASSWORD=sua_senha_aqui

# Linux/Mac
export DB_HOST="cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com"
export DB_PORT="5432"
export DB_NAME="postgres"
export DB_USER="postgres"
export DB_PASSWORD="sua_senha_aqui"
```

### Opção 2: URL Completa

```bash
# Windows (PowerShell)
$env:DATABASE_URL="jdbc:postgresql://cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/postgres"

# Linux/Mac
export DATABASE_URL="jdbc:postgresql://cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/postgres"
```

**Nota:** Se `DATABASE_URL` estiver definida, ela terá prioridade sobre as variáveis individuais.

---

## 📝 Arquivos de Configuração

### 1. ConnectionFactory.java

Arquivo principal para conexões JDBC diretas. Usa variáveis de ambiente ou valores padrão da AWS.

**Localização:** `src/main/java/org/example/persistence/ConnectionFactory.java`

### 2. persistence.xml

Configuração JPA/Hibernate. Suporta variáveis de ambiente.

**Localização:** `src/main/resources/META-INF/persistence.xml`

### 3. DatabaseConnection.java

Gerenciador de conexões JPA. Usa variáveis de ambiente ou valores padrão da AWS.

**Localização:** `src/main/java/org/example/persistence/DatabaseConnection.java`

---

## 🚀 Como Usar

### Sem Variáveis de Ambiente

Se não definir variáveis de ambiente, o sistema usará os valores padrão configurados para AWS:

```java
// Valores padrão já apontam para AWS
Connection conn = ConnectionFactory.getConnection();
```

### Com Variáveis de Ambiente

1. Defina as variáveis de ambiente (veja seção acima)
2. Execute a aplicação normalmente
3. O sistema usará as variáveis definidas

---

## 🧪 Testar Conexão

### Teste via ConnectionFactory

Execute a classe `ConnectionFactory`:

```bash
# Via IDE (Run)
# Ou via linha de comando após compilar
java -cp "lib/*:target/classes" org.example.persistence.ConnectionFactory
```

**Saída esperada:**
```
✅ Conectado com sucesso ao banco AWS RDS!
URL: jdbc:postgresql://cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/postgres
User: postgres
```

### Teste via ConnectionTest

Execute a classe `ConnectionTest`:

```bash
java -cp "lib/*:target/classes" org.example.persistence.ConnectionTest
```

---

## 🔒 Segurança

### ⚠️ IMPORTANTE: Senhas em Código

**NÃO é recomendado** ter senhas hardcoded no código em produção!

### ✅ Boas Práticas

1. **Use variáveis de ambiente** para credenciais
2. **Use AWS Secrets Manager** ou **Parameter Store** em produção
3. **Remova credenciais** do código antes de fazer commit
4. **Use IAM roles** quando possível (EC2, Lambda, ECS)

### Exemplo com AWS Secrets Manager

```java
// Exemplo futuro - integrar com AWS Secrets Manager
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

// Buscar credenciais do Secrets Manager
SecretsManagerClient client = SecretsManagerClient.builder()
    .region(Region.US_EAST_1)
    .build();

GetSecretValueRequest request = GetSecretValueRequest.builder()
    .secretId("cofry/database/credentials")
    .build();
```

---

## 📊 Monitoramento

### Verificar Status da Conexão

```sql
-- Conectar ao banco AWS via psql ou cliente PostgreSQL
psql -h cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com -U postgres -d postgres

-- Verificar conexões ativas
SELECT * FROM pg_stat_activity;

-- Verificar informações do banco
SELECT version();
SELECT current_database();
```

### Logs da Aplicação

A aplicação imprime logs quando:
- ✅ Conexão estabelecida com sucesso
- ❌ Erro ao conectar
- ℹ️ Usando configuração programática vs persistence.xml

---

## 🔄 Migração Completa

### Arquivos Atualizados

✅ `ConnectionFactory.java` - Atualizado para AWS
✅ `persistence.xml` - Atualizado para AWS
✅ `DatabaseConnection.java` - Atualizado para AWS

### Arquivos que Podem Referenciar o Banco Antigo

⚠️ `ConnectionTest.java` - Pode ter comentários antigos (não afeta funcionalidade)
⚠️ `CofryLocal.sql` - Script SQL local (não usado mais)
⚠️ Documentação antiga - Pode mencionar banco local

---

## ❓ Troubleshooting

### Erro: "Connection refused"

**Causa:** Firewall ou Security Group bloqueando conexão

**Solução:**
1. Verifique o Security Group do RDS na AWS
2. Adicione seu IP ou VPC à lista de permissões
3. Porta 5432 deve estar aberta

### Erro: "Authentication failed"

**Causa:** Credenciais incorretas

**Solução:**
1. Verifique usuário e senha
2. Confirme se as variáveis de ambiente estão definidas corretamente
3. Teste conexão direta via psql

### Erro: "Database does not exist"

**Causa:** Nome do banco incorreto

**Solução:**
1. Verifique se o banco existe no RDS
2. Defina `DB_NAME` corretamente
3. Ou use `DATABASE_URL` com o nome correto

---

## 📞 Suporte

Em caso de problemas:

1. Verifique os logs da aplicação
2. Teste conexão direta ao banco AWS
3. Verifique Security Groups e VPC na AWS
4. Confirme que as credenciais estão corretas

---

## ✅ Checklist de Migração

- [x] ConnectionFactory atualizado para AWS
- [x] persistence.xml atualizado para AWS
- [x] DatabaseConnection atualizado para AWS
- [x] Suporte a variáveis de ambiente implementado
- [x] Documentação criada
- [ ] Testes de conexão realizados
- [ ] Variáveis de ambiente configuradas (se necessário)
- [ ] Security Groups verificados na AWS
- [ ] Backup do banco local (se necessário)

---

**Última atualização:** 16 de Janeiro de 2025  
**Status:** ✅ Sistema configurado para usar apenas AWS RDS

