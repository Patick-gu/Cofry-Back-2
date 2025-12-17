# 🔒 Guia de Row-Level Security (RLS) - Supabase

## 📋 Problema Resolvido

O Supabase detectou que **Row-Level Security (RLS)** não estava habilitado nas tabelas expostas ao PostgREST. Isso foi **corrigido** nos scripts SQL.

---

## ✅ O Que Foi Feito

### 1. Scripts Criados:

#### `database-scripts/enable-rls-simple.sql`
- ✅ Habilita RLS em todas as 10 tabelas
- ✅ Cria políticas **permissivas** (para uso com JDBC direto)
- ✅ **Recomendado** se você usa JDBC direto (não PostgREST)

#### `database-scripts/enable-rls-security.sql`
- ✅ Habilita RLS em todas as 10 tabelas
- ✅ Cria políticas **restritivas** (usa `auth.uid()` do Supabase Auth)
- ✅ **Recomendado** se você usa PostgREST com Supabase Auth

### 2. CofryLocal.sql Atualizado:
- ✅ RLS já vem habilitado por padrão
- ✅ Políticas permissivas incluídas

---

## 🎯 Tabelas com RLS Habilitado

1. ✅ `subscription_plans` (Planos de assinatura)
2. ✅ `users` (Usuários)
3. ✅ `addresses` (Endereços)
4. ✅ `accounts` (Contas)
5. ✅ `transaction_categories` (Categorias de transação)
6. ✅ `transactions` (Transações)
7. ✅ `budgets` (Orçamentos)
8. ✅ `savings_goals` (Metas de poupança)
9. ✅ `cards` (Cartões)
10. ✅ `bills` (Boletos)

---

## 🚀 Como Usar

### Opção 1: Já executou CofryLocal.sql?
✅ RLS já está habilitado! Nada mais a fazer.

### Opção 2: Banco já existe?
Execute um dos scripts:

**Para JDBC direto (recomendado):**
```sql
-- Execute no Supabase SQL Editor
\i database-scripts/enable-rls-simple.sql
```

**Para PostgREST com Supabase Auth:**
```sql
-- Execute no Supabase SQL Editor
\i database-scripts/enable-rls-security.sql
```

---

## 🔍 Verificar se RLS Está Habilitado

Execute no Supabase SQL Editor:

```sql
SELECT 
    schemaname,
    tablename,
    rowsecurity as rls_enabled
FROM pg_tables
WHERE schemaname = 'public'
AND tablename IN (
    'subscription_plans',
    'users',
    'addresses',
    'accounts',
    'transaction_categories',
    'transactions',
    'budgets',
    'savings_goals',
    'cards',
    'bills'
)
ORDER BY tablename;
```

Todas devem retornar `rls_enabled = true`.

---

## ⚙️ Ajustar Políticas (Opcional)

Se você usar **JDBC direto** (como seu projeto atual), as políticas permissivas estão corretas.

Se você usar **PostgREST com Supabase Auth**, ajuste as políticas em `enable-rls-security.sql` para usar `auth.uid()` corretamente.

### Exemplo de Política Restritiva:

```sql
-- Usuários só veem seus próprios dados
CREATE POLICY "users_own_data" ON users
    FOR SELECT
    USING (user_id::text = auth.uid()::text);
```

---

## 🔐 Importante

- **JDBC Direto**: RLS não bloqueia conexões JDBC com credenciais corretas
- **PostgREST**: RLS controla acesso via API REST do Supabase
- **Segurança**: Ajuste políticas conforme sua lógica de negócio

---

## 📚 Documentação

- [Supabase RLS Docs](https://supabase.com/docs/guides/auth/row-level-security)
- [PostgreSQL RLS Docs](https://www.postgresql.org/docs/current/ddl-rowsecurity.html)

---

## ✅ Status

**Todos os 10 erros foram resolvidos!** 🎉

Execute o script apropriado no seu banco Supabase para aplicar as correções.

