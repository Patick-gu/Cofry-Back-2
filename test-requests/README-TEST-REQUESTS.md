# Exemplos de Requisições para Teste

## Como Testar os Endpoints

### 1. Cadastro de Usuário

**Endpoint:** `POST /api/form/user`

**Exemplo de requisição:**

```bash
curl -X POST http://localhost:8080/Cofry-Backend2/api/form/user \
  -H "Content-Type: application/json" \
  -d @test-requests/user-registration.json
```

**JSON válido:**
```json
{
  "fullName": "João Silva Santos",
  "email": "joao.silva@example.com",
  "cpf": "123.456.789-09",
  "password": "MinhaSenhaSegura123!"
}
```

**Resposta esperada (201 Created):**
```json
{
  "userId": 1,
  "firstName": "João",
  "lastName": "Silva Santos",
  "fullName": "João Silva Santos",
  "email": "joao.silva@example.com",
  "cpf": "123.456.789-09",
  "phoneNumber": null,
  "dateOfBirth": "2025-12-15",
  "isActive": true,
  "planId": 1,
  "createdAt": "2025-12-15T00:30:00"
}
```

### 2. Cadastro de Endereço

**Endpoint:** `POST /api/form/address`

**Exemplo:**
```json
{
  "userId": 1,
  "phoneNumber": "+55 (11) 91234-5678",
  "zipCode": "01310-100",
  "houseNumber": "123"
}
```

### 3. Cadastro de Conta

**Endpoint:** `POST /api/form/account`

**Exemplo:**
```json
{
  "userId": 1,
  "bank": "Banco do Brasil",
  "agency": "001",
  "accountNumber": "12345-6",
  "selectedPlan": "Premium"
}
```

### 4. Buscar Endereço por CEP

**Endpoint:** `GET /api/form/address/lookup?zipCode=01310100`

### 5. Listar Estados

**Endpoint:** `GET /api/form/address/states`

### 6. Listar Cidades por Estado

**Endpoint:** `GET /api/form/address/cities?state=SP`

### 7. Listar Planos Disponíveis

**Endpoint:** `GET /api/form/account/plans`

---

## Testes com Postman ou Insomnia

1. Importe as requisições abaixo
2. Ajuste a URL base: `http://localhost:8080/Cofry-Backend2`
3. Execute os testes

---

## Validações Implementadas

### Usuário:
- ✅ `fullName` é dividido automaticamente em `firstName` e `lastName`
- ✅ CPF é validado e formatado
- ✅ Email é validado
- ✅ Senha é encriptada com salt

### Endereço:
- ✅ CEP é validado e busca endereço automaticamente
- ✅ Telefone mantém formatação recebida
- ✅ Campos são preenchidos automaticamente quando possível

### Conta:
- ✅ Planos são validados (Basic, Premium, Enterprise)
- ✅ Plano do usuário é atualizado automaticamente

