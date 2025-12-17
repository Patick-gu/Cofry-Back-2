# CHECKLIST - CofryLocal.sql (Produção)

## ✅ VERIFICAÇÃO COMPLETA DO SCRIPT

### 1. PLANOS DE ASSINATURA ✅
- [x] DROP TYPE (se necessário)
- [x] CREATE TABLE subscription_plans
- [x] INSERT 3 planos: Cofry Start (0.00), Cofry Pro (7.77), Cofry Black (47.99)
- [x] Foreign key em users

### 2. BOLETOS (BILLS) ✅
- [x] DROP TYPE bill_status_enum
- [x] CREATE TYPE bill_status_enum
- [x] CREATE TABLE bills
- [x] Índices criados (user_id, status, due_date, created_at, bill_code)
- [x] INSERT 20 boletos de exemplo
- [x] Campos: title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code

### 3. CARTÕES (CARDS) ✅
- [x] DROP TYPE card_type_enum
- [x] CREATE TYPE card_type_enum
- [x] CREATE TABLE cards
- [x] Índices criados (user_id, account_id, status)
- [x] Campos: card_number (VARCHAR 25), limit_amount, current_balance, expiry_date, cvv, brand

### 4. SALDO (ACCOUNTS) ✅
- [x] CREATE TABLE accounts
- [x] Campo balance NUMERIC(15, 2) DEFAULT 0.00
- [x] Foreign key para users
- [x] Índice em bank_code
- [x] Campos: account_number, agency_number, account_type, status

### 5. INVESTIMENTOS ✅
- [x] DROP SCHEMA investments CASCADE
- [x] CREATE SCHEMA investments
- [x] CREATE TABLE investments.asset_category
- [x] CREATE TABLE investments.asset
- [x] CREATE TABLE investments.user_asset
- [x] CREATE TABLE investments.transaction
- [x] INSERT 3 categorias: Ações BR, Cripto, Renda Fixa / FIIs
- [x] INSERT 20 ações brasileiras
- [x] INSERT 20 criptomoedas
- [x] INSERT 10 ativos de Renda Fixa/FIIs
- [x] Total: 50 ativos de investimento

### 6. OUTRAS TABELAS ESSENCIAIS ✅
- [x] users
- [x] addresses
- [x] transaction_categories (10 categorias inseridas)
- [x] transactions
- [x] budgets
- [x] savings_goals

### 7. ENUMS ✅
- [x] bill_status_enum (OPEN, OVERDUE, PAID)
- [x] card_type_enum (CREDIT, DEBIT, PREPAID)
- [x] account_type_enum (CHECKING, SAVINGS)
- [x] transaction_type_enum (DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT)
- [x] goal_status_enum (IN_PROGRESS, COMPLETED, PAUSED)

### 8. UTF-8 ✅
- [x] Comentários sobre UTF-8 no início
- [x] Instruções para verificar encoding
- [x] Script preparado para UTF-8

### 9. ORDEM DE EXECUÇÃO ✅
- [x] DROP em ordem correta (dependências respeitadas)
- [x] CREATE TYPE antes das tabelas
- [x] CREATE TABLE em ordem de dependências
- [x] INSERT em ordem correta (planos antes de users, categorias antes de transações)

### 10. DADOS BASE ✅
- [x] 3 planos de assinatura
- [x] 10 categorias de transação
- [x] 3 categorias de ativos
- [x] 50 ativos de investimento
- [x] 20 boletos de exemplo

## 🎯 STATUS FINAL: PRONTO PARA PRODUÇÃO ✅

Todas as verificações foram concluídas com sucesso!


