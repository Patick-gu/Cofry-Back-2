# 🚀 Guia Completo de Deploy - Cofry Backend

Este documento fornece instruções detalhadas para fazer deploy da aplicação Cofry Backend no AWS EC2 usando Docker.

## 📋 Índice

- [Pré-requisitos](#pré-requisitos)
- [Configuração Inicial do EC2](#configuração-inicial-do-ec2)
- [Deploy Automático com GitHub Actions](#deploy-automático-com-github-actions)
- [Deploy Manual](#deploy-manual)
- [Monitoramento e Logs](#monitoramento-e-logs)
- [Troubleshooting](#troubleshooting)

---

## Pré-requisitos

### Na AWS

1. **Instância EC2**
   - Tipo: t2.micro ou superior (recomendado: t2.small)
   - SO: Ubuntu 22.04 LTS
   - Security Group: Permitir portas 22 (SSH) e 8080 (HTTP)
   - Elastic IP (opcional, mas recomendado)

2. **RDS PostgreSQL**
   - Instância PostgreSQL configurada
   - Security Group permitindo conexões da EC2
   - Credenciais de acesso

### No GitHub

1. Repositório criado
2. Código pushado
3. Acesso para configurar Secrets

---

## Configuração Inicial do EC2

### 1. Conectar à Instância

```bash
ssh -i sua-chave.pem ubuntu@seu-ec2-ip
```

### 2. Executar Script de Setup

```bash
# Baixar o script
curl -o setup-ec2.sh https://raw.githubusercontent.com/seu-usuario/cofry-backend/main/scripts/setup-ec2.sh

# Tornar executável
chmod +x setup-ec2.sh

# Executar
./setup-ec2.sh
```

O script irá:
- Instalar Docker
- Instalar Docker Compose
- Configurar firewall (UFW)
- Criar diretório de deploy

### 3. Configurar Variáveis de Ambiente

```bash
cd ~/cofry-backend

# Copiar arquivo de exemplo
cp env.example .env

# Editar com suas credenciais
nano .env
```

Conteúdo do `.env`:

```env
DATABASE_URL=jdbc:postgresql://seu-rds-endpoint:5432/postgres
DB_HOST=seu-rds-endpoint
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=sua_senha
```

### 4. Configurar Security Group do RDS

No console AWS, edite o Security Group do RDS para permitir conexões:

- Tipo: PostgreSQL
- Porta: 5432
- Origem: Security Group da EC2 (ou IP da EC2)

---

## Deploy Automático com GitHub Actions

### 1. Configurar Secrets no GitHub

1. Acesse: `https://github.com/seu-usuario/cofry-backend/settings/secrets/actions`
2. Clique em "New repository secret"
3. Adicione os seguintes secrets:

#### AWS_ACCESS_KEY_ID
```
Sua chave de acesso AWS (IAM User)
```

#### AWS_SECRET_ACCESS_KEY
```
Sua chave secreta AWS
```

#### EC2_HOST
```
IP público ou DNS da instância EC2
Exemplo: ec2-12-34-56-78.us-east-1.compute.amazonaws.com
```

#### EC2_USER
```
usuário da instância (geralmente 'ubuntu' para Ubuntu)
```

#### EC2_SSH_PRIVATE_KEY
```
Conteúdo completo do arquivo .pem (chave privada SSH)
Inclua as linhas:
-----BEGIN RSA PRIVATE KEY-----
...
-----END RSA PRIVATE KEY-----
```

### 2. Configurar IAM User para GitHub Actions

1. No console AWS, crie um IAM User:
   - Nome: `github-actions-deploy`
   - Permissões: `EC2FullAccess` (ou apenas as permissões necessárias)

2. Crie Access Keys para este usuário

3. Use essas keys como `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY`

### 3. Fluxo de Deploy Automático

O workflow `.github/workflows/deploy.yml` será executado automaticamente quando:

- Fazer push para `main` ou `master`
- Fazer push manual via GitHub Actions UI

**O que o workflow faz:**
1. Compila o projeto com Maven
2. Cria imagem Docker
3. Salva a imagem em um arquivo tar
4. Faz upload para o EC2 via SSH
5. Executa o script `deploy.sh` no servidor
6. O script carrega a imagem, para o container antigo, inicia o novo

### 4. Verificar Deploy

```bash
# Na EC2
docker ps
docker logs -f cofry-backend

# Do seu computador
curl http://seu-ec2-ip:8080/api/users
```

---

## Deploy Manual

Se preferir fazer deploy manual ou se o GitHub Actions não funcionar:

### 1. Build Local

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/cofry-backend.git
cd cofry-backend

# Build da aplicação
mvn clean package -DskipTests

# Build da imagem Docker
docker build -t cofry-backend:latest .
```

### 2. Enviar para EC2

```bash
# Salvar imagem
docker save cofry-backend:latest -o image.tar

# Enviar para EC2
scp -i sua-chave.pem image.tar ubuntu@seu-ec2-ip:~/
scp -i sua-chave.pem docker-compose.yml ubuntu@seu-ec2-ip:~/cofry-backend/
scp -i sua-chave.pem deploy.sh ubuntu@seu-ec2-ip:~/cofry-backend/
```

### 3. Deploy no Servidor

```bash
# Conectar à EC2
ssh -i sua-chave.pem ubuntu@seu-ec2-ip

# Ir para diretório
cd ~/cofry-backend

# Executar deploy
chmod +x deploy.sh
./deploy.sh
```

---

## Monitoramento e Logs

### Ver Logs do Container

```bash
# Logs em tempo real
docker logs -f cofry-backend

# Últimas 100 linhas
docker logs --tail 100 cofry-backend

# Logs com timestamp
docker logs -t cofry-backend
```

### Status do Container

```bash
# Listar containers
docker ps

# Informações detalhadas
docker inspect cofry-backend

# Estatísticas de recursos
docker stats cofry-backend
```

### Health Check

```bash
# Verificar se API está respondendo
curl http://localhost:8080/api/users

# Ou usar o script
./scripts/health-check.sh
```

### Reiniciar Container

```bash
docker restart cofry-backend
```

---

## Troubleshooting

### Container não inicia

```bash
# Verificar logs
docker logs cofry-backend

# Verificar variáveis de ambiente
docker exec cofry-backend env | grep DB_

# Verificar conectividade com banco
docker exec cofry-backend ping seu-rds-endpoint
```

### Erro de conexão com banco

1. Verifique Security Group do RDS
2. Verifique credenciais no `.env`
3. Teste conexão manual:
```bash
psql -h seu-rds-endpoint -U postgres -d postgres
```

### Porta 8080 já em uso

```bash
# Verificar o que está usando a porta
sudo lsof -i :8080

# Parar processo
sudo kill <PID>
```

### Atualizar código

```bash
# Pull do repositório
cd ~/cofry-backend
git pull origin main

# Rebuild
docker-compose build
docker-compose up -d
```

### Limpar recursos Docker

```bash
# Parar e remover container
docker stop cofry-backend
docker rm cofry-backend

# Remover imagens não utilizadas
docker image prune -a

# Limpeza completa (cuidado!)
docker system prune -a
```

### Verificar uso de recursos

```bash
# CPU e memória
htop

# Espaço em disco
df -h

# Uso de Docker
docker system df
```

---

## Configurações Avançadas

### Usar Nginx como Reverse Proxy

Crie `/etc/nginx/sites-available/cofry-backend`:

```nginx
server {
    listen 80;
    server_name seu-dominio.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Configurar SSL com Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d seu-dominio.com
```

### Backup Automático

Crie um cron job para backup do banco:

```bash
# Editar crontab
crontab -e

# Adicionar linha (backup diário às 2h)
0 2 * * * docker exec postgres pg_dump -U postgres postgres > /backup/backup_$(date +\%Y\%m\%d).sql
```

---

## Suporte

Para problemas ou dúvidas:
- Abra uma issue no GitHub
- Verifique os logs: `docker logs cofry-backend`
- Verifique a documentação da API no README.md

