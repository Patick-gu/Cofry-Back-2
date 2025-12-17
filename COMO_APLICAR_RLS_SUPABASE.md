# 📝 Como Aplicar RLS no Supabase

## 🎯 Objetivo
Habilitar Row-Level Security (RLS) em todas as tabelas do banco Supabase para resolver os 10 erros de segurança.

---

## 📋 Passo a Passo

### 1️⃣ Acesse o Supabase Dashboard

1. Vá para [https://supabase.com](https://supabase.com)
2. Faça login na sua conta
3. Selecione seu projeto

---

### 2️⃣ Abra o SQL Editor

1. No menu lateral, clique em **"SQL Editor"**
2. Clique em **"New query"** (Nova query)

---

### 3️⃣ Execute o Script

#### Opção A: Copiar e Colar (Mais Rápido)

1. Abra o arquivo: `database-scripts/apply-rls-to-supabase.sql`
2. **Copie todo o conteúdo** do arquivo
3. **Cole** no SQL Editor do Supabase
4. Clique em **"Run"** (ou pressione `Ctrl+Enter` / `Cmd+Enter`)

#### Opção B: Upload do Arquivo

1. No SQL Editor, clique em **"..."** (três pontos)
2. Selecione **"Import from file"**
3. Escolha o arquivo `database-scripts/apply-rls-to-supabase.sql`

---

### 4️⃣ Verificar o Resultado

Após executar o script, você deve ver uma tabela com 10 linhas mostrando:

```
schemaname | tablename               | rls_enabled | policies_count
-----------|-------------------------|-------------|---------------
public     | subscription_plans      | true        | 4
public     | users                   | true        | 4
public     | addresses               | true        | 4
...
```

**Todos devem ter:**
- ✅ `rls_enabled = true`
- ✅ `policies_count = 4` (SELECT, INSERT, UPDATE, DELETE)

---

### 5️⃣ Verificar no Dashboard

1. Vá para **"Table Editor"** no menu lateral
2. Selecione qualquer tabela
3. No topo, você deve ver **"Row Level Security: Enabled"**

---

### 6️⃣ Verificar os Erros

1. Vá para **"Database"** → **"Reports"** (ou "Advisors")
2. Os **10 erros de RLS** devem ter desaparecido! ✅

---

## 🔍 Troubleshooting

### Erro: "relation does not exist"

**Solução:** Certifique-se de que todas as tabelas existem. Execute primeiro o `CofryLocal.sql` completo se necessário.

### Erro: "permission denied"

**Solução:** Verifique se você está usando o usuário correto (`postgres`). Use as credenciais do banco.

### RLS não aparece habilitado

**Solução:** Recarregue a página do Supabase ou execute novamente apenas os comandos `ALTER TABLE ... ENABLE ROW LEVEL SECURITY;`

---

## ✅ O Que o Script Faz

1. ✅ Habilita RLS nas 10 tabelas
2. ✅ Remove políticas antigas (se existirem)
3. ✅ Cria 4 políticas permissivas por tabela:
   - SELECT (leitura)
   - INSERT (inserção)
   - UPDATE (atualização)
   - DELETE (exclusão)
4. ✅ Verifica se tudo funcionou

---

## 🔐 Políticas Criadas

As políticas são **permissivas** (permitem tudo). Isso é adequado se você usa **JDBC direto** como no seu projeto atual.

**Se usar PostgREST com Supabase Auth**, você precisará ajustar as políticas para usar `auth.uid()`. Veja `database-scripts/enable-rls-security.sql` para exemplo.

---

## 📞 Suporte

Se algo der errado:
1. Verifique os logs no SQL Editor
2. Execute apenas as partes específicas que falharam
3. Verifique se todas as tabelas existem: `SELECT tablename FROM pg_tables WHERE schemaname = 'public';`

---

## 🎉 Pronto!

Após executar, todos os 10 erros de segurança devem estar resolvidos!

