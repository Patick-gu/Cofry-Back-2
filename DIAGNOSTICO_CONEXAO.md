# 🔍 Diagnóstico de Problemas de Conexão

## ❌ Erro: "Network is unreachable"

### Causa
O erro `java.net.SocketException: Network is unreachable` indica que a aplicação não consegue alcançar o servidor do banco de dados através da rede.

### Possíveis Causas

#### 1. **Problema de Conectividade de Rede**
- ❌ Sem conexão com a internet
- ❌ Rede bloqueando conexões externas
- ❌ Proxy configurado incorretamente

#### 2. **Firewall/Antivírus Bloqueando**
- ❌ Firewall do Windows bloqueando porta 5432
- ❌ Antivírus bloqueando conexões PostgreSQL
- ❌ Firewall corporativo bloqueando conexões externas

#### 3. **Configuração do Supabase**
- ❌ Host incorreto
- ❌ Porta incorreta
- ❌ Banco de dados pausado (plano free)
- ❌ IP não autorizado no Supabase

#### 4. **Configuração SSL**
- ❌ SSL requerido mas não configurado
- ❌ Certificado SSL inválido

---

## 🔧 Soluções

### ✅ 1. Verificar Conectividade Básica

```bash
# Testar ping ao host
ping db.qcgvvrbwtjijyylxxugb.supabase.co

# Testar conexão TCP na porta 5432
telnet db.qcgvvrbwtjijyylxxugb.supabase.co 5432
# ou
nc -zv db.qcgvvrbwtjijyylxxugb.supabase.co 5432
```

### ✅ 2. Verificar Firewall do Windows

1. Abra **Windows Defender Firewall**
2. Vá em **Configurações Avançadas**
3. Verifique se porta 5432 está permitida (saída)
4. Temporariamente desative o firewall para testar

### ✅ 3. Verificar Status do Supabase

1. Acesse o [Dashboard do Supabase](https://app.supabase.com)
2. Verifique se o projeto está **ativo** (não pausado)
3. Verifique **Settings > Database > Connection string**
4. Confirme que o host está correto

### ✅ 4. Verificar Configuração no Código

Verifique se a URL está correta executando:

```bash
java -cp target/classes org.example.persistence.ConnectionFactory
```

Isso mostrará a URL exata que está sendo usada.

### ✅ 5. Testar com psql (PostgreSQL CLI)

```bash
# Instalar PostgreSQL client (se não tiver)
# Windows: baixar de https://www.postgresql.org/download/windows/

psql "host=db.qcgvvrbwtjijyylxxugb.supabase.co port=5432 dbname=postgres user=postgres password=Cofry.072519 sslmode=require"
```

### ✅ 6. Verificar Variáveis de Ambiente

```bash
# Windows PowerShell
echo $env:DATABASE_URL
echo $env:DB_HOST

# Windows CMD
echo %DATABASE_URL%
echo %DB_HOST%
```

### ✅ 7. Usar Connection Pooling

Se o problema for temporário, pode ser falta de timeout adequado. O código já tem retry implícito.

---

## 📋 Checklist de Diagnóstico

- [ ] Conectividade de internet funcionando
- [ ] Ping ao host do Supabase funciona
- [ ] Porta 5432 acessível (telnet/nc)
- [ ] Firewall não está bloqueando
- [ ] Projeto Supabase está ativo (não pausado)
- [ ] Credenciais corretas (host, port, user, password)
- [ ] SSL configurado (`sslmode=require`)
- [ ] Nenhum proxy bloqueando conexões
- [ ] Antivírus não está interferindo

---

## 🔄 Alternativas

### Opção 1: Usar Variáveis de Ambiente

Se o problema for com o host padrão, defina variáveis de ambiente:

```bash
# Windows PowerShell
$env:DB_HOST="db.qcgvvrbwtjijyylxxugb.supabase.co"
$env:DB_PORT="5432"
$env:DB_NAME="postgres"
$env:DB_USER="postgres"
$env:DB_PASSWORD="Cofry.072519"
```

### Opção 2: Usar Connection Pool com Timeout

Adicionar timeout na URL:

```java
String url = "jdbc:postgresql://host:port/db?sslmode=require&connectTimeout=10&socketTimeout=10";
```

### Opção 3: Verificar Logs do Supabase

Acesse o dashboard do Supabase e verifique:
- **Logs > Database Logs**
- **Database > Connection Pooling**

---

## 📞 Suporte

Se nenhuma solução funcionar:

1. **Verifique os logs completos** da aplicação
2. **Teste com psql** diretamente
3. **Verifique se outras aplicações conseguem conectar**
4. **Contate o suporte do Supabase** se necessário

---

## 🔍 Logs Melhorados

O código agora imprime:
- URL de conexão (sem senha)
- Usuário
- Mensagens de erro detalhadas
- Sugestões de solução

Execute novamente e verifique os logs para mais informações.

