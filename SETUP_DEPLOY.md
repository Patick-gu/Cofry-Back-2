# 🚀 Guia Rápido de Configuração para Deploy

## ✅ Status Atual

- ✅ Código commitado e enviado para o GitHub
- ✅ Repositório: https://github.com/Patick-gu/Cofry-Back-2.git
- ✅ Branch: `main`
- ✅ Dockerfile configurado
- ✅ GitHub Actions workflow criado
- ✅ Scripts de deploy prontos

## 📝 Próximos Passos para Deploy Automático

### 1. Configurar Secrets no GitHub

Acesse: `https://github.com/Patick-gu/Cofry-Back-2/settings/secrets/actions`

Clique em **"New repository secret"** e adicione:

#### AWS_ACCESS_KEY_ID
```
Sua chave de acesso AWS
```

#### AWS_SECRET_ACCESS_KEY
```
Sua chave secreta AWS
```

#### EC2_HOST
```
IP público ou DNS da sua instância EC2
Exemplo: ec2-12-34-56-78.us-east-1.compute.amazonaws.com
ou: 12.34.56.78
```

#### EC2_USER
```
ubuntu
```
(ou `ec2-user` dependendo do SO da sua instância)

#### EC2_SSH_PRIVATE_KEY
```
Conteúdo completo do arquivo .pem (chave privada SSH)
-----BEGIN RSA PRIVATE KEY-----
[conteúdo completo da chave]
-----END RSA PRIVATE KEY-----
```

### 2. Preparar Instância EC2

Conecte-se à sua EC2:

```bash
ssh -i sua-chave.pem ubuntu@seu-ec2-ip
```

Execute o script de setup:

```bash
curl -o setup-ec2.sh https://raw.githubusercontent.com/Patick-gu/Cofry-Back-2/main/scripts/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh
```

### 3. Configurar Variáveis de Ambiente na EC2

```bash
cd ~/cofry-backend
curl -o env.example https://raw.githubusercontent.com/Patick-gu/Cofry-Back-2/main/env.example
cp env.example .env
nano .env
```

Edite o `.env` com suas credenciais do RDS:
```env
DATABASE_URL=jdbc:postgresql://seu-rds-endpoint:5432/postgres
DB_HOST=seu-rds-endpoint
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=sua_senha
```

### 4. Testar Deploy Manual (Opcional)

Se quiser testar antes do deploy automático:

```bash
# No seu computador local
git clone https://github.com/Patick-gu/Cofry-Back-2.git
cd Cofry-Back-2
mvn clean package -DskipTests
docker build -t cofry-backend:latest .
docker save cofry-backend:latest -o image.tar

# Enviar para EC2
scp -i sua-chave.pem image.tar docker-compose.yml env.example deploy.sh ubuntu@seu-ec2-ip:~/cofry-backend/

# Na EC2
ssh -i sua-chave.pem ubuntu@seu-ec2-ip
cd ~/cofry-backend
chmod +x deploy.sh
./deploy.sh
```

### 5. Deploy Automático

Após configurar os secrets, o deploy será automático quando você fizer:

```bash
git push origin main
```

O GitHub Actions irá:
1. Buildar a aplicação
2. Criar a imagem Docker
3. Fazer upload para a EC2
4. Executar o deploy automaticamente

### 6. Verificar Deploy

```bash
# Verificar se está rodando
ssh -i sua-chave.pem ubuntu@seu-ec2-ip
docker ps
docker logs -f cofry-backend

# Testar API
curl http://localhost:8080/api/users
```

## 🔧 Troubleshooting

### Se o deploy falhar:

1. **Verificar logs do GitHub Actions:**
   - Vá em: `https://github.com/Patick-gu/Cofry-Back-2/actions`
   - Clique no workflow que falhou
   - Veja os logs

2. **Verificar logs na EC2:**
   ```bash
   docker logs cofry-backend
   ```

3. **Verificar conectividade com banco:**
   ```bash
   docker exec cofry-backend env | grep DB_
   ```

4. **Verificar Security Groups:**
   - EC2: Porta 8080 aberta
   - RDS: Permite conexões da EC2 (Security Group)

## 📚 Documentação Completa

Para mais detalhes, consulte:
- `DEPLOY.md` - Guia completo de deploy
- `DEPLOY_CHECKLIST.md` - Checklist de deploy
- `README.md` - Documentação da API

## 🎉 Pronto!

Após seguir esses passos, seu sistema estará pronto para deploy automático!

