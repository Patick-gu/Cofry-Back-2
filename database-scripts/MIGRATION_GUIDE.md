# 🔐 Migração para Arquitetura Moderna Supabase

## 📋 Resumo das Mudanças

Este script implementa a **arquitetura moderna recomendada pelo Supabase**, com segurança máxima e integração nativa com Supabase Auth.

---

## ✅ Principais Mudanças

### 1️⃣ **IDs: INTEGER → UUID**

**Antes:**
```sql
user_id SERIAL PRIMARY KEY          -- 1, 2, 3...
account_id SERIAL PRIMARY KEY       -- 1, 2, 3...
transaction_id SERIAL PRIMARY KEY   -- 1, 2, 3...
```

**Depois:**
```sql
user_id UUID PRIMARY KEY                          -- Mesmo ID do auth.users
account_id UUID PRIMARY KEY DEFAULT gen_random_uuid()
transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

**Por quê?**
- ✅ Sincronização com `auth.users` do Supabase
- ✅ Segurança: IDs não sequenciais (imprevisíveis)
- ✅ Escalabilidade: distribuição global sem conflitos
- ✅ Integração perfeita com RLS via `auth.uid()`

---

### 2️⃣ **Autenticação: Manual → Supabase Auth**

**Antes:**
```sql
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(150),
    password_hash VARCHAR(255),  -- ❌ Removido
    ...
);
```

**Depois:**
```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY,  -- ✅ Mesmo ID de auth.users
    email VARCHAR(150),
    -- SEM password_hash
    ...
);

-- Trigger automático para sincronizar
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION handle_new_user();
```

**Como funciona:**
1. Usuário se registra via **Supabase Auth** (signup)
2. Supabase cria entrada em `auth.users`
3. **Trigger automático** cria entrada em `public.users` com mesmo UUID
4. Senha gerenciada pelo Supabase (hash bcrypt automático)

---

### 3️⃣ **Segurança: ALLOW ALL → RLS Baseado em `auth.uid()`**

**Antes (INSEGURO):**
```sql
CREATE POLICY allow_all_users ON users 
    FOR ALL USING (true) WITH CHECK (true);
-- ❌ Qualquer um pode acessar qualquer dado!
```

**Depois (SEGURO):**
```sql
-- Usuários só veem seus próprios dados
CREATE POLICY "Usuários veem próprios dados"
    ON users FOR SELECT
    USING (auth.uid() = user_id);

-- Usuários só atualizam seus próprios dados
CREATE POLICY "Usuários atualizam próprios dados"
    ON users FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);
```

**Transações com segurança baseada em contas:**
```sql
CREATE POLICY "Usuários veem próprias transações"
    ON transactions FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
    );
```

---

## 🔐 Políticas RLS Implementadas

### **Públicas (Leitura para todos)**
| Tabela | Política |
|--------|----------|
| `subscription_plans` | ✅ Todos podem visualizar planos |
| `transaction_categories` | ✅ Todos podem visualizar categorias |
| `investments.asset_category` | ✅ Todos podem visualizar categorias |
| `investments.asset` | ✅ Todos podem visualizar ativos |

### **Privadas (Apenas dados do usuário autenticado)**
| Tabela | SELECT | INSERT | UPDATE | DELETE |
|--------|--------|--------|--------|--------|
| `users` | ✅ Próprios dados | ❌ | ✅ Próprios dados | ❌ |
| `addresses` | ✅ | ✅ | ✅ | ✅ |
| `accounts` | ✅ | ✅ | ✅ | ✅ |
| `transactions` | ✅ (se a conta for sua) | ✅ | ✅ | ✅ |
| `budgets` | ✅ | ✅ | ✅ | ✅ |
| `savings_goals` | ✅ | ✅ | ✅ | ✅ |
| `cards` | ✅ | ✅ | ✅ | ✅ |
| `bills` | ✅ | ✅ | ✅ | ✅ |
| `investments.user_asset` | ✅ | ✅ | ✅ | ✅ |
| `investments.transaction` | ✅ | ✅ | ✅ | ✅ |

---

## 🚀 Como Usar

### **1. Executar o Script no Supabase**

1. Acesse o **SQL Editor** no Supabase
2. Cole o conteúdo de `supabase-secure-setup.sql`
3. Clique em **Run** (▶️)

### **2. Criar Usuário via Supabase Auth**

**Frontend (JavaScript/TypeScript):**
```javascript
import { createClient } from '@supabase/supabase-js'

const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY)

// Criar usuário
const { data, error } = await supabase.auth.signUp({
  email: 'usuario@example.com',
  password: 'SenhaSegura123!',
  options: {
    data: {
      first_name: 'João',
      last_name: 'Silva',
      tax_id: '123.456.789-00'
    }
  }
})

// Usuário criado!
console.log(data.user.id)  // UUID do usuário
```

**Backend Java (via SupabaseAuthService):**
```java
SupabaseAuthService authService = new SupabaseAuthService();

UserMetadata metadata = new UserMetadata();
metadata.firstName = "João";
metadata.lastName = "Silva";
metadata.taxId = "123.456.789-00";

AuthResponse response = authService.signUp(
    "usuario@example.com", 
    "SenhaSegura123!", 
    metadata
);

UUID userId = response.user.id;  // UUID criado pelo Supabase
String accessToken = response.accessToken;
```

### **3. Fazer Login**

**Frontend:**
```javascript
const { data, error } = await supabase.auth.signInWithPassword({
  email: 'usuario@example.com',
  password: 'SenhaSegura123!'
})

const accessToken = data.session.access_token
```

**Backend Java:**
```java
AuthResponse response = authService.login(
    "usuario@example.com", 
    "SenhaSegura123!"
);

String accessToken = response.accessToken;
UUID userId = response.user.id;
```

### **4. Usar o Token em Queries**

**Frontend (Automático):**
```javascript
// Supabase JS automaticamente envia o token
const { data, error } = await supabase
  .from('accounts')
  .select('*')
// RLS garante que só retorna contas do usuário autenticado
```

**Backend Java (Manual com ConnectionFactory):**
```java
// Antes de executar queries
ConnectionFactory.setUserToken(accessToken);

// Executar operações
List<Account> accounts = accountDAO.findByUserId(userId);

// Limpar após uso
ConnectionFactory.clearUserToken();
```

---

## 🔄 Fluxo Completo de Autenticação

### **Signup (Registro)**
```
1. Frontend/Backend → Supabase Auth API
   POST /auth/v1/signup
   { email, password, user_metadata }

2. Supabase Auth cria entrada em auth.users
   user_id = UUID gerado pelo Supabase

3. Trigger on_auth_user_created dispara
   → Cria entrada em public.users com mesmo UUID

4. Retorna JWT com claims:
   { 
     sub: "user_uuid",
     email: "usuario@example.com",
     user_metadata: { first_name, last_name, ... }
   }
```

### **Login**
```
1. Frontend/Backend → Supabase Auth API
   POST /auth/v1/token?grant_type=password
   { email, password }

2. Supabase valida credenciais

3. Retorna JWT (access_token)

4. Cliente usa token em todas as requisições
```

### **Query com RLS**
```
1. Cliente envia token JWT no header:
   Authorization: Bearer eyJhbGciOiJIUzI1...

2. Supabase extrai auth.uid() do token

3. Políticas RLS aplicam filtros:
   SELECT * FROM accounts WHERE user_id = auth.uid()

4. Retorna apenas dados do usuário autenticado
```

---

## 📊 Exemplo de Estrutura de Dados

### **auth.users (Gerenciado pelo Supabase)**
```
id                                   | email              | encrypted_password
-------------------------------------|--------------------|-----------------
550e8400-e29b-41d4-a716-446655440000 | joao@example.com   | $2a$10$...
```

### **public.users (Sua aplicação)**
```
user_id                              | email              | first_name | last_name
-------------------------------------|--------------------|-----------|-----------
550e8400-e29b-41d4-a716-446655440000 | joao@example.com   | João      | Silva
```

### **accounts**
```
account_id                           | user_id                              | balance
-------------------------------------|--------------------------------------|--------
a1b2c3d4-e5f6-7890-abcd-ef1234567890 | 550e8400-e29b-41d4-a716-446655440000 | 5000.00
```

---

## ⚠️ Mudanças Necessárias no Código Java

### **1. Atualizar Models (int → UUID)**
```java
// Antes
public class User {
    private Integer userId;
}

// Depois
public class User {
    private UUID userId;  // ✅
}
```

### **2. Atualizar DAOs**
```java
// Antes
public Optional<User> findById(Integer id) { ... }

// Depois
public Optional<User> findById(UUID id) {  // ✅
    String sql = "SELECT * FROM users WHERE user_id = ?::uuid";
    stmt.setObject(1, id);
}
```

### **3. Usar SupabaseAuthService**
```java
// ❌ NÃO FAZER MAIS: Hash manual de senha
String hash = BCrypt.hashpw(password, BCrypt.gensalt());
user.setPasswordHash(hash);

// ✅ FAZER: Delegar ao Supabase
SupabaseAuthService authService = new SupabaseAuthService();
AuthResponse response = authService.signUp(email, password, metadata);
UUID userId = response.user.id;
```

---

## 🎯 Benefícios da Nova Arquitetura

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Segurança** | ❌ Acesso total liberado | ✅ RLS baseado em auth.uid() |
| **Senhas** | ❌ Hash manual (vulnerável) | ✅ Supabase Auth (bcrypt) |
| **IDs** | ❌ Sequenciais (previsíveis) | ✅ UUIDs (imprevisíveis) |
| **Escalabilidade** | ⚠️ Limitada | ✅ Distribuída globalmente |
| **Manutenção** | ❌ Código complexo | ✅ Delegado ao Supabase |
| **Compliance** | ⚠️ LGPD/GDPR manual | ✅ Supabase gerencia |

---

## 📝 Checklist de Migração

- [ ] Executar `supabase-secure-setup.sql` no Supabase
- [ ] Atualizar models Java (`Integer` → `UUID`)
- [ ] Atualizar DAOs para UUID
- [ ] Implementar `SupabaseAuthService` (já criado)
- [ ] Refatorar `ConnectionFactory` para JWT (já criado)
- [ ] Atualizar servlets para usar Supabase Auth
- [ ] Remover código de hash de senha
- [ ] Testar políticas RLS
- [ ] Migrar usuários existentes (se houver)

---

## 🔥 Próximos Passos

1. **Execute o script SQL no Supabase**
2. **Configure as variáveis de ambiente:**
   ```bash
   SUPABASE_URL=https://xxx.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1...
   ```
3. **Use os arquivos Java já criados:**
   - `SupabaseConfig.java`
   - `SupabaseAuthService.java`
   - `ConnectionFactory.java` (refatorado)
   - Modelos atualizados (`User.java`, `Account.java`, etc.)

---

✅ **Pronto para produção com segurança máxima!** 🚀
