# 🔌 Configuração de Porta do Servidor

## 📋 Status

✅ **Configuração de porta adicionada!**

A porta do servidor agora é configurável via variável de ambiente `PORT`.

---

## 🎯 Como Funciona

### Porta Padrão
- **8080** (se `PORT` não for definido)

### Configuração Dinâmica
- Use a variável de ambiente `PORT` para alterar a porta

---

## 🚀 Como Usar

### 1️⃣ Docker Compose

```bash
# Porta padrão (8080)
docker-compose up

# Porta customizada
PORT=3000 docker-compose up
```

### 2️⃣ Docker Direto

```bash
# Porta padrão
docker run -p 8080:8080 cofry-backend

# Porta customizada
docker run -e PORT=3000 -p 3000:3000 cofry-backend
```

### 3️⃣ Variável de Ambiente

```bash
# Linux/Mac
export PORT=3000

# Windows PowerShell
$env:PORT=3000

# Windows CMD
set PORT=3000
```

### 4️⃣ Arquivo .env

Crie um arquivo `.env` na raiz do projeto:

```env
PORT=3000
DB_HOST=...
DB_PASSWORD=...
```

Depois use com docker-compose:

```bash
docker-compose --env-file .env up
```

---

## 📝 Arquivos Atualizados

### ✅ Dockerfile
- ✅ Script de inicialização que lê `PORT`
- ✅ Configura Tomcat dinamicamente
- ✅ Expõe porta configurável

### ✅ docker-compose.yml
- ✅ Usa `PORT` da variável de ambiente
- ✅ Padrão: 8080

### ✅ env.example
- ✅ Adicionada variável `PORT=8080`

### ✅ render.yaml
- ✅ Configura `PORT=8080` para Render

---

## 🌐 Plataformas de Deploy

### Render
A porta é automaticamente configurada via `PORT` (Render define automaticamente).

### Heroku
Heroku define `PORT` automaticamente, não precisa configurar.

### AWS EC2 / Outros
Defina `PORT` no arquivo `.env` ou como variável de ambiente.

---

## 🔍 Verificar Porta em Execução

```bash
# Ver logs do container
docker logs cofry-backend

# Ver processos
docker ps

# Ver variáveis de ambiente
docker exec cofry-backend env | grep PORT
```

---

## ⚠️ Importante

- **Docker Compose**: Certifique-se de que a porta do host está mapeada corretamente
- **Produção**: Alguns serviços (Render, Heroku) definem `PORT` automaticamente
- **Firewall**: Certifique-se de que a porta está aberta no firewall

---

## ✅ Teste

```bash
# Testar com porta customizada
PORT=3000 docker-compose up

# Em outro terminal
curl http://localhost:3000/api/users
```

---

## 📚 Referências

- [Tomcat Port Configuration](https://tomcat.apache.org/tomcat-9.0-doc/config/http.html)
- [Docker Port Mapping](https://docs.docker.com/config/containers/container-networking/)

