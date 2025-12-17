# 🏦 Cofry Backend API

API REST completa para gerenciamento financeiro pessoal, desenvolvida em Java com arquitetura em camadas.

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Endpoints da API](#-endpoints-da-api)
- [Autenticação](#-autenticação)
- [Estrutura de Resposta](#-estrutura-de-resposta)
- [Tratamento de Erros](#-tratamento-de-erros)
- [Exemplos de Uso](#-exemplos-de-uso)

---

## 🎯 Sobre o Projeto

Cofry é uma API REST para gerenciamento financeiro pessoal que oferece funcionalidades completas de:

- 💰 **Gestão de Contas**: Múltiplas contas bancárias com controle de saldo
- 💳 **Cartões**: Gerenciamento de cartões de crédito/débito
- 📊 **Transações**: Histórico completo de transações financeiras
- 💸 **PIX**: Transferências entre usuários via CPF
- 📄 **Boletos**: Emissão e pagamento de boletos bancários (DDA)
- 📈 **Investimentos**: Gestão de carteira de investimentos
- 🎯 **Metas**: Metas de poupança e orçamentos
- 👤 **Usuários**: Sistema completo de usuários com planos de assinatura

---

## 🛠 Tecnologias

- **Java 21**
- **Maven** - Gerenciamento de dependências
- **PostgreSQL** - Banco de dados (AWS RDS)
- **JDBC** - Acesso ao banco de dados
- **JPA/Hibernate** - ORM
- **Servlets** - API REST
- **Tomcat 10** - Servidor de aplicação
- **Gson** - Serialização JSON
- **Docker** - Containerização
- **GitHub Actions** - CI/CD

---

## 🏗 Arquitetura

A API segue o padrão de arquitetura em camadas:

```
┌─────────────────────────────────────┐
│        Controller (Servlets)        │  ← Endpoints REST
├─────────────────────────────────────┤
│          Service (Lógica)           │  ← Regras de negócio
├─────────────────────────────────────┤
│         DAO (Data Access)           │  ← Acesso ao banco
├─────────────────────────────────────┤
│         Database (PostgreSQL)       │  ← Persistência
└─────────────────────────────────────┘
```

### Estrutura do Projeto

```
src/main/java/org/example/
├── controller/      # Servlets (endpoints REST)
├── service/         # Lógica de negócio
├── dao/             # Data Access Objects
├── model/           # Entidades JPA
├── dto/             # Data Transfer Objects
├── persistence/     # Gerenciamento de conexão
├── utils/           # Utilitários (validação, criptografia)
└── config/          # Configurações (CORS, filtros)
```

---

## 📦 Pré-requisitos

- Java 21 ou superior
- Maven 3.6+
- PostgreSQL 12+ (ou acesso ao AWS RDS)
- Tomcat 9 ou superior
- Git

---

## ⚙️ Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/cofry-backend.git
cd cofry-backend
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` ou configure as variáveis de ambiente do sistema:

```bash
# Banco de Dados AWS RDS
export DB_HOST=cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com
export DB_PORT=5432
export DB_NAME=postgres
export DB_USER=postgres
export DB_PASSWORD=sua_senha_aqui

# OU use uma única variável
export DATABASE_URL=jdbc:postgresql://host:port/database
```

### 3. Execute o script SQL

Execute o script `CofryLocal.sql` no banco de dados para criar todas as tabelas:

```bash
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f CofryLocal.sql
```

### 4. Compile o projeto

```bash
mvn clean package
```

### 5. Execute no Tomcat

```bash
# Copie o .war gerado para o diretório webapps do Tomcat
cp target/Cofry-Backend2-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/

# Inicie o Tomcat
$TOMCAT_HOME/bin/startup.sh
```

### 6. Verifique a API

```bash
curl http://localhost:8080/api/users
```

---

## 🐳 Deploy com Docker

### Opção 1: Render (Recomendado para começar)

O projeto está **100% compatível com Render**! Veja o guia completo em [`DEPLOY_RENDER.md`](DEPLOY_RENDER.md)

**Quick Start:**
1. Acesse https://dashboard.render.com
2. Conecte o repositório `Patick-gu/Cofry-Back-2`
3. Selecione "Docker" como runtime
4. Configure variáveis de ambiente
5. Deploy automático! ✨

### Opção 2: Docker Compose (Local)

1. **Configure as variáveis de ambiente:**

```bash
cp env.example .env
# Edite o arquivo .env com suas credenciais
```

2. **Execute com Docker Compose:**

```bash
docker-compose up -d
```

3. **Verifique se está rodando:**

```bash
docker logs -f cofry-backend
curl http://localhost:8080/api/users
```

### Opção 3: Build manual do Docker

```bash
# Build da imagem
docker build -t cofry-backend:latest .

# Execute o container
docker run -d \
  -p 8080:8080 \
  --name cofry-backend \
  -e DB_HOST=seu-host \
  -e DB_USER=seu-usuario \
  -e DB_PASSWORD=sua-senha \
  cofry-backend:latest
```

---

## 🚀 Deploy no AWS EC2

### Pré-requisitos

- Instância EC2 com Ubuntu 22.04 ou superior
- Chave SSH da instância
- Credenciais AWS configuradas
- Repositório no GitHub

### Configuração Inicial do EC2

1. **Conecte-se à instância EC2:**

```bash
ssh -i sua-chave.pem ubuntu@seu-ec2-ip
```

2. **Execute o script de setup:**

```bash
curl -o setup-ec2.sh https://raw.githubusercontent.com/seu-usuario/cofry-backend/main/scripts/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh
```

3. **Configure as variáveis de ambiente:**

```bash
cd ~/cofry-backend
cp env.example .env
nano .env  # Edite com suas credenciais
```

### Configuração do GitHub Actions

1. **Configure os Secrets no GitHub:**

Vá em `Settings > Secrets and variables > Actions` e adicione:

- `AWS_ACCESS_KEY_ID`: Sua chave de acesso AWS
- `AWS_SECRET_ACCESS_KEY`: Sua chave secreta AWS
- `EC2_HOST`: IP público ou DNS da instância EC2
- `EC2_USER`: Usuário da instância (geralmente `ubuntu`)
- `EC2_SSH_PRIVATE_KEY`: Conteúdo da chave privada SSH (.pem)

2. **O workflow será executado automaticamente** ao fazer push para `main` ou `master`.

### Deploy Manual no EC2

Se preferir fazer deploy manual:

```bash
# No servidor EC2
cd ~/cofry-backend
git pull origin main
docker-compose down
docker-compose build
docker-compose up -d
```

### Verificar Deploy

```bash
# Verificar logs
docker logs -f cofry-backend

# Verificar saúde da API
curl http://localhost:8080/api/users

# Ou usar o script de health check
./scripts/health-check.sh http://seu-ec2-ip:8080
```

### Configuração do Security Group

Certifique-se de que o Security Group da EC2 permite tráfego na porta 8080:

- Tipo: Custom TCP
- Porta: 8080
- Origem: 0.0.0.0/0 (ou apenas seus IPs permitidos)

---

## 🔌 Endpoints da API

### Base URL

```
http://localhost:8080/api
```

---

### 🔐 Autenticação

#### `POST /api/auth/login`
Autentica um usuário e retorna seus dados.

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "senha123"
}
```

**Response:**
```json
{
  "userId": 1,
  "firstName": "João",
  "lastName": "Silva",
  "email": "usuario@example.com",
  "cpf": "123.456.789-00",
  "planId": 1,
  "isActive": true
}
```

#### `POST /api/auth/change-password`
Altera a senha do usuário.

**Request:**
```json
{
  "userId": 1,
  "currentPassword": "senha_atual",
  "newPassword": "nova_senha_123!"
}
```

---

### 👤 Usuários

#### `GET /api/users`
Lista todos os usuários.

#### `GET /api/users/{id}`
Busca um usuário por ID.

#### `GET /api/users/{id}/complete`
Busca informações completas do usuário (inclui endereços e contas).

#### `POST /api/users`
Cria um novo usuário.

**Request:**
```json
{
  "firstName": "Maria",
  "lastName": "Santos",
  "email": "maria@example.com",
  "taxId": "987.654.321-00",
  "dateOfBirth": "1995-03-15",
  "planId": 1
}
```

#### `PUT /api/users/{id}`
Atualiza um usuário existente (incluindo plano de assinatura).

**Request:**
```json
{
  "planId": 2
}
```

#### `DELETE /api/users/{id}`
Remove um usuário.

---

### 🏦 Contas Bancárias

#### `GET /api/accounts?userId={userId}`
Lista contas de um usuário.

#### `GET /api/accounts/{id}`
Busca uma conta por ID.

#### `POST /api/accounts`
Cria uma nova conta bancária.

#### `PUT /api/accounts/{id}`
Atualiza uma conta.

#### `PUT /api/accounts/{id}/balance`
Define o saldo de uma conta.

#### `DELETE /api/accounts/{id}`
Remove uma conta.

---

### 💳 Cartões

#### `GET /api/form/card/user/{userId}`
Lista cartões de um usuário.

#### `GET /api/form/card/{id}`
Busca um cartão por ID.

#### `GET /api/form/card/types`
Lista tipos de cartão disponíveis.

#### `POST /api/form/card`
Cria um novo cartão.

**Request:**
```json
{
  "userId": 1,
  "accountId": 1,
  "cardNumber": "1234567890123456",
  "cardHolderName": "JOÃO SILVA",
  "expirationDate": "12/25",
  "cvv": "123",
  "cardType": "CREDIT",
  "brand": "VISA",
  "limit": 5000.00
}
```

#### `PUT /api/form/card/{id}`
Atualiza um cartão.

#### `DELETE /api/form/card/{id}`
Remove um cartão.

---

### 💸 Transações

#### `GET /api/transactions?accountId={accountId}`
Lista transações de uma conta.

#### `GET /api/transactions/{id}`
Busca uma transação por ID.

#### `POST /api/transactions`
Cria uma nova transação (atualiza saldo automaticamente).

**Request:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 100.50,
  "transactionType": "TRANSFER",
  "description": "Transferência entre contas",
  "transactionDate": "2025-12-16"
}
```

**Tipos de transação:**
- `DEPOSIT` - Depósito
- `WITHDRAWAL` - Saque
- `TRANSFER` - Transferência
- `PAYMENT` - Pagamento

#### `PUT /api/transactions/{id}`
Atualiza uma transação.

#### `DELETE /api/transactions/{id}`
Remove uma transação.

---

### 💰 PIX

#### `POST /api/pix/transfer`
Realiza transferência PIX entre usuários via CPF.

**Request:**
```json
{
  "sourceAccountId": 1,
  "recipientCpf": "987.654.321-00",
  "amount": 250.00,
  "description": "Transferência via PIX"
}
```

**Response:**
```json
{
  "success": true,
  "transactionId": 123,
  "message": "Transferência PIX realizada com sucesso",
  "newBalance": 4750.00
}
```

---

### 📄 Boletos (Bills)

#### `GET /api/form/boleto/user/{userId}`
Lista boletos de um usuário.

#### `GET /api/form/boleto/cpf/{cpf}`
Lista boletos por CPF (para pagamento de terceiros).

#### `GET /api/form/boleto/status/{status}`
Lista boletos por status (`OPEN`, `OVERDUE`, `PAID`).

#### `POST /api/form/boleto`
Cria um novo boleto.

#### `POST /api/form/boleto/{id}/pay`
Paga um boleto (desconta do saldo automaticamente).

**Request:**
```json
{
  "accountId": 1,
  "description": "Pagamento de boleto"
}
```

#### `POST /api/form/boleto/{id}/automatize`
Configura pagamento automático de boleto.

---

### 📈 Investimentos

#### `GET /api/assets`
Lista todos os ativos disponíveis (ações, FIIs, etc.).

#### `GET /api/assets/{id}`
Busca um ativo por ID.

#### `GET /api/assets/ticker/{ticker}`
Busca um ativo por ticker (ex: `PETR4`).

#### `POST /api/investments/transaction`
Cria uma transação de investimento (compra/venda).

**Request:**
```json
{
  "userId": 1,
  "assetId": 5,
  "quantity": 10,
  "price": 25.50,
  "transactionType": "BUY",
  "transactionDate": "2025-12-16"
}
```

#### `GET /api/investments/history/user/{userId}`
Lista histórico de transações de investimento do usuário.

#### `GET /api/investments/portfolio/user/{userId}`
Retorna resumo do portfólio do usuário.

#### `GET /api/investments/distribution/user/{userId}`
Retorna distribuição de ativos no portfólio.

---

### 🎯 Metas de Poupança

#### `GET /api/savings-goals?userId={userId}`
Lista metas de poupança de um usuário.

#### `GET /api/savings-goals/{id}`
Busca uma meta por ID.

#### `POST /api/savings-goals`
Cria uma nova meta de poupança.

#### `PUT /api/savings-goals/{id}`
Atualiza uma meta.

#### `DELETE /api/savings-goals/{id}`
Remove uma meta.

---

### 📊 Orçamentos

#### `GET /api/budgets?userId={userId}`
Lista orçamentos de um usuário.

#### `GET /api/budgets/{id}`
Busca um orçamento por ID.

#### `POST /api/budgets`
Cria um novo orçamento.

#### `PUT /api/budgets/{id}`
Atualiza um orçamento.

#### `DELETE /api/budgets/{id}`
Remove um orçamento.

---

### 📍 Endereços

#### `GET /api/addresses?userId={userId}`
Lista endereços de um usuário.

#### `GET /api/addresses/{id}`
Busca um endereço por ID.

#### `POST /api/addresses`
Cria um novo endereço.

#### `PUT /api/addresses/{id}`
Atualiza um endereço.

#### `DELETE /api/addresses/{id}`
Remove um endereço.

---

## 🔐 Autenticação

Atualmente, a API utiliza autenticação simples via email/CPF e senha. O sistema:

- Aceita login por **email** ou **CPF**
- Valida senha usando hash SHA-256
- Retorna dados do usuário autenticado

**Nota:** Para produção, recomenda-se implementar autenticação baseada em tokens (JWT).

---

## 📤 Estrutura de Resposta

### Sucesso

```json
{
  "success": true,
  "data": { ... }
}
```

### Erro

```json
{
  "success": false,
  "error": "Mensagem de erro descritiva"
}
```

### Códigos HTTP

- `200 OK` - Requisição bem-sucedida
- `201 Created` - Recurso criado com sucesso
- `400 Bad Request` - Dados inválidos ou faltando
- `404 Not Found` - Recurso não encontrado
- `500 Internal Server Error` - Erro interno do servidor

---

## ❌ Tratamento de Erros

Todos os erros retornam uma resposta JSON padronizada:

```json
{
  "success": false,
  "error": "Descrição do erro"
}
```

**Exemplos de erros comuns:**

- `"Usuário não encontrado com ID: 123"`
- `"Saldo insuficiente. Saldo disponível: R$ 100,00. Valor necessário: R$ 250,00"`
- `"Email ou CPF já cadastrado"`
- `"Boleto já está pago"`
- `"Senha atual incorreta"`

---

## 💡 Exemplos de Uso

### Criar usuário e conta

```bash
# 1. Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "João",
    "lastName": "Silva",
    "email": "joao@example.com",
    "taxId": "123.456.789-00",
    "dateOfBirth": "1990-01-15",
    "planId": 1
  }'

# 2. Criar conta (será criada automaticamente com saldo de R$ 20.000)
# 3. Criar cartão Cofry (será criado automaticamente)
```

### Realizar transferência PIX

```bash
curl -X POST http://localhost:8080/api/pix/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": 1,
    "recipientCpf": "987.654.321-00",
    "amount": 100.00,
    "description": "Transferência PIX"
  }'
```

### Pagar boleto

```bash
curl -X POST http://localhost:8080/api/form/boleto/1/pay \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "description": "Pagamento de conta de luz"
  }'
```

### Comprar ativo

```bash
curl -X POST http://localhost:8080/api/investments/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "assetId": 5,
    "quantity": 10,
    "price": 25.50,
    "transactionType": "BUY",
    "transactionDate": "2025-12-16"
  }'
```

---

## 🔒 Segurança

- ✅ Senhas são armazenadas como hash SHA-256
- ✅ Validação de CPF brasileiro
- ✅ Validação de dados de entrada
- ✅ CORS configurado
- ⚠️ **Recomendações para produção:**
  - Implementar autenticação JWT
  - Adicionar rate limiting
  - Usar HTTPS em produção
  - Implementar logging de segurança
  - Adicionar validação de tokens CSRF

---

## 📝 Notas Importantes

1. **Saldo Automático**: Transações do tipo `DEPOSIT`, `WITHDRAWAL`, `PAYMENT` e `TRANSFER` atualizam automaticamente o saldo das contas.

2. **Cartão Automático**: Ao criar um novo usuário, um cartão Cofry é gerado automaticamente com:
   - Número aleatório gerado
   - Limite de R$ 10.000
   - Vencimento em 4 anos
   - Bandeira Visa
   - Tipo Crédito/Débito

3. **Saldo Inicial**: Novos usuários recebem automaticamente uma conta com saldo inicial de R$ 20.000.

4. **Planos de Assinatura**: O sistema possui 3 planos:
   - **Cofry Start** (R$ 0,00) - Gratuito
   - **Cofry Pro** (R$ 7,77) - Intermediário
   - **Cofry Black** (R$ 47,99) - Premium

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

## 👨‍💻 Autor

Desenvolvido como parte do projeto Cofry - Sistema de Gestão Financeira Pessoal.

---

## 📞 Suporte

Para dúvidas ou problemas, abra uma [issue](https://github.com/seu-usuario/cofry-backend/issues) no repositório.
