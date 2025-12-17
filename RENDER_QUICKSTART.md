# ⚡ Quick Start - Deploy no Render

## ✅ Status: Pronto para Deploy!

Seu projeto está **100% compatível** com Render. Todas as adaptações necessárias já foram feitas!

## 🚀 Deploy em 5 Passos

### 1. Acesse o Render
- https://dashboard.render.com
- Faça login ou crie conta gratuita

### 2. Crie Web Service
- Clique em **"New +"** → **"Web Service"**
- Conecte: `Patick-gu/Cofry-Back-2`
- Branch: `main`

### 3. Configure
- **Runtime:** `Docker`
- **Dockerfile Path:** `./Dockerfile`
- **Instance Type:** `Free` (ou pago)

### 4. Adicione Banco de Dados (Opcional)
- **New +** → **"PostgreSQL"**
- Render configurará `DATABASE_URL` automaticamente
- OU use seu RDS AWS (configure variáveis manualmente)

### 5. Configure Variáveis de Ambiente

**Opção A: PostgreSQL do Render (Recomendado)**
- Render fornece `DATABASE_URL` automaticamente
- ✅ Código converte automaticamente para JDBC!

**Opção B: AWS RDS Externo**
```
DATABASE_URL=jdbc:postgresql://seu-rds:5432/postgres
DB_HOST=seu-rds-endpoint
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=sua-senha
```

### 6. Deploy!
- Clique em **"Create Web Service"**
- Aguarde 5-10 minutos
- Pronto! 🎉

## ✨ O que foi adaptado automaticamente

✅ **ConnectionFactory** - Converte `DATABASE_URL` do Render para JDBC  
✅ **Dockerfile** - Funciona no Render sem mudanças  
✅ **Variáveis de ambiente** - Suporta ambos os formatos  
✅ **render.yaml** - Configuração pronta (opcional)

## 🔗 Documentação Completa

Veja [`DEPLOY_RENDER.md`](DEPLOY_RENDER.md) para guia completo!

## ⚠️ Importante

- **Plano Free:** App "dorme" após 15 min de inatividade
- **Primeira requisição:** Pode levar ~30 segundos para "acordar"
- **Para produção:** Considere plano pago ($7/mês)

---

**Deploy feito? Teste:** `https://seu-app.onrender.com/api/users`

