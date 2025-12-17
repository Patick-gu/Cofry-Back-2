# 🔌 Configuração de Banco de Dados

## 📊 Banco Atual: **Supabase (PostgreSQL)**

### Configuração Padrão

```
Host: db.qcgvvrbwtjijyylxxugb.supabase.co
Porta: 5432
Banco: postgres
Usuário: postgres
Senha: Cofry.072519
SSL: Obrigatório (sslmode=require)
```

---

## 🚀 Suporte a Múltiplos Ambientes

O sistema suporta **3 formas de configuração**, em ordem de prioridade:

### 1️⃣ **Variáveis de Ambiente (MAIOR PRIORIDADE)**

#### Para Render/Heroku (formato `postgresql://`)
```bash
DATABASE_URL=postgresql://user:password@host:port/dbname
```

**✅ Conversão Automática:**
- O sistema detecta `postgresql://` e converte para `jdbc:postgresql://`
- Adiciona `sslmode=require` automaticamente se não estiver presente
- Extrai usuário e senha automaticamente da URL

#### Para Configuração Manual (variáveis separadas)
```bash
DB_HOST=db.qcgvvrbwtjijyylxxugb.supabase.co
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=Cofry.072519
```

#### Para JPA (JDBC URL completa)
```bash
DB_URL=jdbc:postgresql://db.qcgvvrbwtjijyylxxugb.supabase.co:5432/postgres?sslmode=require
```

### 2️⃣ **Arquivo `persistence.xml`** (Hibernate/JPA)

O arquivo `src/main/resources/META-INF/persistence.xml` usa variáveis de ambiente com fallback:

```xml
<property name="javax.persistence.jdbc.url" 
          value="${DB_URL:jdbc:postgresql://db.qcgvvrbwtjijyylxxugb.supabase.co:5432/postgres?sslmode=require}"/>
<property name="javax.persistence.jdbc.user" 
          value="${DB_USER:postgres}"/>
<property name="javax.persistence.jdbc.password" 
          value="${DB_PASSWORD:Cofry.072519}"/>
```

### 3️⃣ **Valores Padrão no Código** (FALLBACK)

Se nenhuma variável de ambiente for definida, usa os valores padrão do Supabase.

---

## 🔒 SSL/TLS

### Supabase
- ✅ **SSL Obrigatório**: `sslmode=require`
- Adicionado automaticamente em todos os formatos de URL

### Render/Heroku
- ✅ **SSL Suportado**: Adicionado automaticamente na conversão
- Se a URL já tiver parâmetros, adiciona `&sslmode=require`
- Se não tiver parâmetros, adiciona `?sslmode=require`

---

## 📝 Exemplos de Configuração

### Exemplo 1: Supabase (Padrão Atual)
```bash
# Sem variáveis de ambiente - usa padrão
# Conecta automaticamente ao Supabase
```

### Exemplo 2: Render
```bash
# No Render Dashboard, defina:
DATABASE_URL=postgresql://user:pass@dpg-xxxxx.oregon-postgres.render.com:5432/cofry_db

# O sistema converte automaticamente para:
# jdbc:postgresql://user:pass@dpg-xxxxx.oregon-postgres.render.com:5432/cofry_db?sslmode=require
```

### Exemplo 3: Banco Local
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cofry_local
DB_USER=postgres
DB_PASSWORD=sua_senha
# SSL não é adicionado para localhost
```

### Exemplo 4: AWS RDS
```bash
DB_HOST=cofry-db.xxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=cofry
DB_USER=admin
DB_PASSWORD=sua_senha
# SSL pode ser adicionado manualmente na URL se necessário
```

---

## 🔧 Arquivos de Configuração

### 1. `ConnectionFactory.java` (JDBC - Principal)
```java
// Usado por todos os DAOs
// Prioridade: DATABASE_URL > DB_HOST/DB_PORT/DB_NAME > Padrão
```

### 2. `DatabaseConnection.java` (JPA/Hibernate)
```java
// Usado para EntityManager
// Prioridade: DB_URL > Padrão
```

### 3. `persistence.xml` (JPA/Hibernate)
```xml
<!-- Usado pelo Hibernate -->
<!-- Prioridade: Variáveis de ambiente > Padrão -->
```

---

## ✅ Validação

### Testar Conexão
```bash
# Compilar e executar
mvn compile
java -cp target/classes org.example.persistence.ConnectionFactory
```

### Saída Esperada
```
✅ Conectado com sucesso ao banco de dados!
URL: jdbc:postgresql://db.qcgvvrbwtjijyylxxugb.supabase.co:5432/postgres?sslmode=require
User: postgres
```

---

## 🔄 Alterando o Banco de Dados

### Opção 1: Variáveis de Ambiente (Recomendado)
```bash
# Linux/Mac
export DATABASE_URL="postgresql://user:pass@host:port/db"

# Windows (PowerShell)
$env:DATABASE_URL="postgresql://user:pass@host:port/db"

# Windows (CMD)
set DATABASE_URL=postgresql://user:pass@host:port/db
```

### Opção 2: Editar Código (Não recomendado para produção)
Edite os valores `DEFAULT_*` em:
- `ConnectionFactory.java`
- `DatabaseConnection.java`
- `persistence.xml`

---

## 📋 Checklist de Migração

- [ ] Definir variáveis de ambiente no ambiente de destino
- [ ] Verificar se `sslmode=require` está presente (para Supabase)
- [ ] Testar conexão com `ConnectionFactory.main()`
- [ ] Verificar logs do aplicativo
- [ ] Testar operações CRUD básicas

---

## 🐛 Troubleshooting

### Erro: "SSL required"
**Solução:** Certifique-se de que `sslmode=require` está na URL

### Erro: "Connection refused"
**Solução:** Verifique host, porta e firewall

### Erro: "Authentication failed"
**Solução:** Verifique usuário e senha nas variáveis de ambiente

### Render: URL não convertida
**Solução:** Certifique-se de que `DATABASE_URL` começa com `postgresql://`

---

## 📚 Referências

- [Documentação Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [Documentação Render](https://render.com/docs/databases)
- [PostgreSQL JDBC](https://jdbc.postgresql.org/documentation/)

---

**Última atualização:** Configurado para Supabase com suporte automático a Render

