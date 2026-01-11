# Refatoração Supabase - Guia de Migração

## 📋 Resumo da Refatoração

Este projeto foi refatorado para usar **Supabase Auth** e **Row Level Security (RLS)**, migrando de:
- IDs numéricos (`Integer`) → **UUIDs** (`java.util.UUID`)
- Autenticação manual (hash de senha) → **Supabase Auth API**
- Conexão com usuário `postgres` → **Conexão com JWT injection para RLS**

---

## 🗂️ Arquivos Principais Criados/Modificados

### ✅ 1. Configuração do Supabase
**Arquivo:** `src/main/java/org/example/config/SupabaseConfig.java`

```java
public class SupabaseConfig {
    // Variáveis de ambiente:
    // - SUPABASE_URL
    // - SUPABASE_ANON_KEY
    // - DATABASE_URL
    // - DB_USER
    // - DB_PASSWORD
}
```

### ✅ 2. Serviço de Autenticação Supabase
**Arquivo:** `src/main/java/org/example/service/SupabaseAuthService.java`

```java
SupabaseAuthService authService = new SupabaseAuthService();

// Criar novo usuário
AuthResponse signupResponse = authService.signUp(email, password, metadata);
UUID userId = signupResponse.user.id;
String accessToken = signupResponse.accessToken;

// Login
AuthResponse loginResponse = authService.login(email, password);

// Obter informações do usuário
UserInfo userInfo = authService.getUserInfo(accessToken);

// Logout
authService.logout(accessToken);
```

### ✅ 3. ConnectionFactory com Suporte a JWT/RLS
**Arquivo:** `src/main/java/org/example/persistence/ConnectionFactory.java`

```java
// Definir token JWT antes de executar queries
ConnectionFactory.setUserToken(jwtToken);

// Executar queries (RLS será aplicado automaticamente)
Connection conn = ConnectionFactory.getConnection();

// Limpar token após operações
ConnectionFactory.clearUserToken();
```

**Como funciona:**
- Antes de cada query, o `ConnectionFactory` injeta o JWT na sessão PostgreSQL:
  ```sql
  SET request.jwt.claim.sub = 'user_uuid_here';
  SET request.jwt.claims = 'jwt_token_here';
  ```
- Isso permite que as políticas RLS do Supabase identifiquem o usuário autenticado.

---

## 🔄 Modelos Atualizados (int → UUID)

### ✅ 4. User.java (sem password_hash)
**Arquivo:** `src/main/java/org/example/model/User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;  // ✅ Mudou de Integer para UUID
    
    // ❌ Removido: passwordHash
    // ✅ Autenticação delegada ao Supabase Auth
}
```

### ✅ 5. Account.java
**Arquivo:** `src/main/java/org/example/model/Account.java`

```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_id", columnDefinition = "uuid")
    private UUID accountId;  // ✅ UUID
    
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;  // ✅ UUID
}
```

### ✅ 6. Transaction.java
**Arquivo:** `src/main/java/org/example/model/Transaction.java`

```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "transaction_id", columnDefinition = "uuid")
    private UUID transactionId;  // ✅ UUID
    
    @Column(name = "source_account_id", columnDefinition = "uuid")
    private UUID sourceAccountId;  // ✅ UUID
    
    @Column(name = "destination_account_id", columnDefinition = "uuid")
    private UUID destinationAccountId;  // ✅ UUID
}
```

---

## 📊 DAOs Refatorados

### ✅ 7. UserDAO.java
**Arquivo:** `src/main/java/org/example/dao/UserDAO.java`

```java
public class UserDAO {
    // Métodos atualizados para UUID:
    public User save(User user) { ... }
    public Optional<User> findById(UUID id) { ... }  // ✅ UUID
    public boolean delete(UUID id) { ... }  // ✅ UUID
}
```

**Exemplo de INSERT com UUID:**
```java
String sql = "INSERT INTO users (user_id, plan_id, first_name, last_name, tax_id, email, phone_number, " +
            "date_of_birth, is_active, created_at, updated_at) " +
            "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

stmt.setObject(1, user.getUserId());  // ✅ UUID
stmt.setInt(2, user.getPlanId());
stmt.setString(3, user.getFirstName());
// ... resto dos parâmetros
```

### ✅ 8. TransactionDAO.java
**Arquivo:** `src/main/java/org/example/dao/TransactionDAO.java`

```java
public class TransactionDAO {
    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, source_account_id, destination_account_id, ...) " +
                    "VALUES (?::uuid, ?::uuid, ?::uuid, ...)";
        
        stmt.setObject(1, transaction.getTransactionId());  // ✅ UUID
        stmt.setObject(2, transaction.getSourceAccountId());  // ✅ UUID
        // ...
    }
    
    public Optional<Transaction> findById(UUID id) { ... }
    public List<Transaction> findByUserId(UUID userId) { ... }
}
```

---

## 🌐 Novo Servlet de Autenticação

### ✅ 9. SupabaseAuthServlet.java
**Arquivo:** `src/main/java/org/example/controller/SupabaseAuthServlet.java`

**Endpoints:**

#### 🔵 POST `/api/auth/supabase/signup`
```json
{
  "email": "usuario@example.com",
  "password": "SenhaSegura123",
  "firstName": "João",
  "lastName": "Silva",
  "cpf": "123.456.789-00",
  "phoneNumber": "+5511999999999",
  "dateOfBirth": "1990-01-01"
}
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1...",
  "refreshToken": "...",
  "expiresIn": 3600,
  "tokenType": "bearer",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "usuario@example.com",
    "userMetadata": {
      "firstName": "João",
      "lastName": "Silva",
      "taxId": "123.456.789-00"
    }
  }
}
```

#### 🔵 POST `/api/auth/supabase/login`
```json
{
  "email": "usuario@example.com",
  "password": "SenhaSegura123"
}
```

**Resposta:** (mesma estrutura do signup)

#### 🔵 POST `/api/auth/supabase/logout`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

#### 🔵 GET `/api/auth/supabase/me`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Resposta:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@example.com",
  "emailConfirmedAt": "2026-01-11T10:30:00Z",
  "createdAt": "2026-01-11T10:30:00Z",
  "updatedAt": "2026-01-11T10:30:00Z",
  "userMetadata": {
    "firstName": "João",
    "lastName": "Silva"
  }
}
```

---

## 🔐 Fluxo de Autenticação Completo

### 1️⃣ **Cliente faz Login**
```http
POST /api/auth/supabase/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "password": "senha123"
}
```

### 2️⃣ **Servidor retorna JWT**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### 3️⃣ **Cliente envia JWT em requisições subsequentes**
```http
GET /api/transactions
Authorization: Bearer eyJhbGciOiJIUzI1...
```

### 4️⃣ **Servlet injeta JWT antes de executar queries**
```java
// No início do método do Servlet
String authHeader = request.getHeader("Authorization");
String token = authHeader.substring(7); // Remove "Bearer "
ConnectionFactory.setUserToken(token);

// Executa operações (RLS aplicado automaticamente)
transactionService.getAllTransactions();

// Limpa o token
ConnectionFactory.clearUserToken();
```

### 5️⃣ **PostgreSQL aplica RLS automaticamente**
```sql
-- Exemplo de política RLS no banco
CREATE POLICY "Users can only see their own transactions"
ON transactions
FOR SELECT
USING (
  source_account_id IN (
    SELECT account_id FROM accounts 
    WHERE user_id = auth.uid()
  )
);
```

---

## ⚙️ Variáveis de Ambiente Necessárias

Crie um arquivo `.env` ou configure no servidor:

```bash
# Supabase
SUPABASE_URL=https://qcgvvrbwtjijyylxxugb.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Database
DATABASE_URL=jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require
DB_USER=postgres.qcgvvrbwtjijyylxxugb
DB_PASSWORD=Cofry.072519
```

---

## 📌 Observações Importantes

### ❌ O que foi REMOVIDO:
1. Campo `password_hash` da tabela `users`
2. Métodos de validação de senha nos DAOs
3. Uso de `Integer` como ID nas entidades principais
4. Conexões diretas sem JWT

### ✅ O que foi ADICIONADO:
1. Suporte a `UUID` em todos os modelos
2. `SupabaseAuthService` com `HttpClient` (Java 17+)
3. Injeção automática de JWT nas queries via `ConnectionFactory`
4. Novo servlet `SupabaseAuthServlet`
5. Classe de configuração `SupabaseConfig`

### ⚠️ Migração de Dados:
Se você já possui dados no banco com IDs `INTEGER`, será necessário:
1. Criar uma migration para converter `INTEGER` → `UUID`
2. Mover usuários para o Supabase Auth
3. Atualizar todas as foreign keys

---

## 🧪 Testando a Implementação

### Teste 1: Criar Usuário
```bash
curl -X POST http://localhost:8080/api/auth/supabase/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "Senha@123",
    "firstName": "Teste",
    "lastName": "User"
  }'
```

### Teste 2: Login
```bash
curl -X POST http://localhost:8080/api/auth/supabase/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "password": "Senha@123"
  }'
```

### Teste 3: Obter Transações (com RLS)
```bash
curl -X GET http://localhost:8080/api/transactions \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## 🎯 Benefícios da Refatoração

✅ **Segurança:** RLS garante que usuários só acessem seus próprios dados  
✅ **Escalabilidade:** UUIDs distribuídos sem conflitos  
✅ **Manutenção:** Autenticação delegada ao Supabase  
✅ **Compliance:** Senhas gerenciadas por serviço especializado  
✅ **Performance:** Conexão pooling do Supabase  

---

## 📚 Referências

- [Supabase Auth API](https://supabase.com/docs/reference/javascript/auth-api)
- [Row Level Security (RLS)](https://supabase.com/docs/guides/auth/row-level-security)
- [PostgreSQL UUID Type](https://www.postgresql.org/docs/current/datatype-uuid.html)
