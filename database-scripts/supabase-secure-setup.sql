-- ============================================================
-- SCRIPT COMPLETO PARA SUPABASE - ARQUITETURA MODERNA
-- UUID + Supabase Auth + Row Level Security (RLS)
-- ============================================================

-- 1. LIMPEZA TOTAL (RESET)
-- ============================================================

-- Remover políticas RLS antes de dropar tabelas
DO $$ 
DECLARE 
    pol record;
BEGIN 
    FOR pol IN 
        SELECT schemaname, tablename, policyname
        FROM pg_policies 
        WHERE schemaname IN ('public', 'investments')
    LOOP 
        EXECUTE format('DROP POLICY IF EXISTS %I ON %I.%I;', pol.policyname, pol.schemaname, pol.tablename);
    END LOOP;
END $$;

-- Dropar tabelas
DROP TABLE IF EXISTS bills CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS budgets CASCADE;
DROP TABLE IF EXISTS savings_goals CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS transaction_categories CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS subscription_plans CASCADE;

-- Dropar schema investments
DROP SCHEMA IF EXISTS investments CASCADE;

-- Dropar enums
DROP TYPE IF EXISTS bill_status_enum CASCADE;
DROP TYPE IF EXISTS card_type_enum CASCADE;
DROP TYPE IF EXISTS account_type_enum CASCADE;
DROP TYPE IF EXISTS transaction_type_enum CASCADE;
DROP TYPE IF EXISTS goal_status_enum CASCADE;

-- 2. CRIAÇÃO DOS ENUMS
-- ============================================================

CREATE TYPE bill_status_enum AS ENUM ('OPEN', 'OVERDUE', 'PAID');
CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
CREATE TYPE account_type_enum AS ENUM ('CHECKING', 'SAVINGS');
CREATE TYPE transaction_type_enum AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT');
CREATE TYPE goal_status_enum AS ENUM ('IN_PROGRESS', 'COMPLETED', 'PAUSED');

-- 3. CRIAÇÃO DAS TABELAS (PUBLIC) - UUID + SEM PASSWORD
-- ============================================================

-- Subscription Plans
CREATE TABLE subscription_plans (
    plan_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Users (sincronizado com auth.users via trigger)
CREATE TABLE users (
    user_id UUID PRIMARY KEY,  -- Mesmo ID do auth.users
    plan_id INT DEFAULT 1,
    
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    tax_id VARCHAR(14) UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    
    -- SEM password_hash - autenticação via Supabase Auth
    
    date_of_birth DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
);

-- Addresses
CREATE TABLE addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) DEFAULT 'Brazil',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_user_address FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Accounts
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    bank_code VARCHAR(3),
    bank_name VARCHAR(100),
    account_number VARCHAR(20) NOT NULL,
    agency_number VARCHAR(10) NOT NULL,
    account_type account_type_enum NOT NULL DEFAULT 'CHECKING',
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    
    CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Transaction Categories
CREATE TABLE transaction_categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon_code VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Transactions
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id UUID NOT NULL,
    destination_account_id UUID,
    category_id INT,
    amount NUMERIC(15, 2) NOT NULL,
    transaction_type transaction_type_enum NOT NULL,
    description VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    is_recurring BOOLEAN DEFAULT FALSE,
    installment_current INT,
    installment_total INT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT check_positive_amount CHECK (amount > 0),
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_dest_account FOREIGN KEY (destination_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_trans_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
);

-- Budgets
CREATE TABLE budgets (
    budget_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id INT NOT NULL,
    amount_limit NUMERIC(15, 2) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT unique_budget UNIQUE (user_id, category_id, period_month, period_year),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
);

-- Savings Goals
CREATE TABLE savings_goals (
    goal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount NUMERIC(15, 2) NOT NULL,
    current_amount NUMERIC(15, 2) DEFAULT 0.00,
    target_date DATE,
    status goal_status_enum DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Cards
CREATE TABLE cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_id UUID,
    card_number VARCHAR(25) NOT NULL,
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(4),
    card_type card_type_enum NOT NULL,
    brand VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    limit_amount NUMERIC(15, 2),
    current_balance NUMERIC(15, 2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- Bills
CREATE TABLE bills (
    bill_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status bill_status_enum NOT NULL DEFAULT 'OPEN',
    bank_code VARCHAR(3) NOT NULL,
    wallet_code VARCHAR(5) NOT NULL,
    our_number VARCHAR(23) NOT NULL,
    bill_code VARCHAR(48) NOT NULL UNIQUE,
    user_id UUID,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 4. CRIAÇÃO DAS TABELAS (INVESTMENTS SCHEMA)
-- ============================================================

CREATE SCHEMA IF NOT EXISTS investments;

-- Asset Categories
CREATE TABLE investments.asset_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Assets
CREATE TABLE investments.asset (
    id SERIAL PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    api_identifier VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_asset_category FOREIGN KEY (category_id) REFERENCES investments.asset_category (id)
);

-- User Assets
CREATE TABLE investments.user_asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    asset_id INT NOT NULL,
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity >= 0),
    average_price NUMERIC(18, 8) NOT NULL,
    last_updated TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE (user_id, asset_id),
    CONSTRAINT fk_user_asset_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_asset_asset FOREIGN KEY (asset_id) REFERENCES investments.asset (id)
);

-- Investment Transactions
CREATE TABLE investments.transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    asset_id INT NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('Compra', 'Venda')),
    price NUMERIC(18, 8) NOT NULL CHECK (price > 0),
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity > 0),
    total_value NUMERIC(18, 8) NOT NULL CHECK (total_value > 0),
    transaction_date TIMESTAMPTZ DEFAULT NOW(),
    status VARCHAR(20) NOT NULL,
    
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_asset FOREIGN KEY (asset_id) REFERENCES investments.asset (id)
);

-- 5. TRIGGER PARA SINCRONIZAR auth.users COM users
-- ============================================================

-- Função para criar usuário na tabela users quando criar no auth.users
CREATE OR REPLACE FUNCTION public.handle_new_user() 
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (user_id, email, created_at, updated_at)
    VALUES (
        NEW.id,
        NEW.email,
        NOW(),
        NOW()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger que dispara após INSERT em auth.users
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers de updated_at
CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION handle_updated_at();

CREATE TRIGGER set_updated_at_cards
    BEFORE UPDATE ON cards
    FOR EACH ROW EXECUTE FUNCTION handle_updated_at();

CREATE TRIGGER set_updated_at_bills
    BEFORE UPDATE ON bills
    FOR EACH ROW EXECUTE FUNCTION handle_updated_at();

-- 6. INSERÇÃO DE DADOS INICIAIS
-- ============================================================

-- Planos de Assinatura
INSERT INTO subscription_plans (name, price, description) VALUES
('Cofry Start', 0.00, 'Plano gratuito com funcionalidades básicas'),
('Cofry Pro', 7.77, 'Plano intermediário com recursos avançados'),
('Cofry Black', 47.99, 'Plano premium com todos os recursos');

-- Categorias de Transação
INSERT INTO transaction_categories (name, icon_code) VALUES
('Alimentação', 'food'),
('Transporte', 'transport'),
('Moradia', 'home'),
('Saúde', 'health'),
('Educação', 'education'),
('Lazer', 'leisure'),
('Compras', 'shopping'),
('Contas', 'bills'),
('Salário', 'salary'),
('Outros', 'other');

-- Categorias de Investimento
INSERT INTO investments.asset_category (name) VALUES
('Ações BR'),
('Cripto'),
('Renda Fixa / FIIs');

-- Ativos de Investimento
INSERT INTO investments.asset (ticker, name, category_id, api_identifier, is_active) VALUES
-- Ações Brasileiras
('PETR4', 'Petrobras PN', 1, 'PETR4.SA', TRUE),
('VALE3', 'Vale S.A.', 1, 'VALE3.SA', TRUE),
('ITUB4', 'Itaú Unibanco PN', 1, 'ITUB4.SA', TRUE),
('BBDC4', 'Bradesco PN', 1, 'BBDC4.SA', TRUE),
('ELET3', 'Eletrobras ON', 1, 'ELET3.SA', TRUE),
('WEGE3', 'Weg S.A.', 1, 'WEGE3.SA', TRUE),
('MGLU3', 'Magazine Luiza ON', 1, 'MGLU3.SA', TRUE),
('BBAS3', 'Banco do Brasil ON', 1, 'BBAS3.SA', TRUE),
-- Criptomoedas
('BTC', 'Bitcoin', 2, 'bitcoin', TRUE),
('ETH', 'Ethereum', 2, 'ethereum', TRUE),
('SOL', 'Solana', 2, 'solana', TRUE),
-- Fundos Imobiliários
('MXRF11', 'Maxi Renda FII', 3, 'MXRF11.SA', TRUE),
('KNRI11', 'Kinea Renda Imobiliária', 3, 'KNRI11.SA', TRUE),
('TESSELIC', 'Tesouro Selic', 3, 'FIXO_SELIC', TRUE);

-- 7. ROW LEVEL SECURITY (RLS) - POLÍTICAS SEGURAS
-- ============================================================

-- Habilitar RLS em todas as tabelas
ALTER TABLE subscription_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE savings_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE cards ENABLE ROW LEVEL SECURITY;
ALTER TABLE bills ENABLE ROW LEVEL SECURITY;
ALTER TABLE investments.asset_category ENABLE ROW LEVEL SECURITY;
ALTER TABLE investments.asset ENABLE ROW LEVEL SECURITY;
ALTER TABLE investments.user_asset ENABLE ROW LEVEL SECURITY;
ALTER TABLE investments.transaction ENABLE ROW LEVEL SECURITY;

-- POLÍTICAS PÚBLICAS (Leitura para todos)
-- ============================================================

-- Planos: todos podem ver
CREATE POLICY "Planos visíveis para todos"
    ON subscription_plans FOR SELECT
    USING (true);

-- Categorias de transação: todos podem ver
CREATE POLICY "Categorias visíveis para todos"
    ON transaction_categories FOR SELECT
    USING (true);

-- Categorias de investimento: todos podem ver
CREATE POLICY "Categorias de investimento visíveis para todos"
    ON investments.asset_category FOR SELECT
    USING (true);

-- Ativos: todos podem ver
CREATE POLICY "Ativos visíveis para todos"
    ON investments.asset FOR SELECT
    USING (true);

-- POLÍTICAS PRIVADAS (Apenas dados do próprio usuário)
-- ============================================================

-- Users: usuários podem ver e atualizar seus próprios dados
CREATE POLICY "Usuários veem próprios dados"
    ON users FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios dados"
    ON users FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Addresses: usuários gerenciam próprios endereços
CREATE POLICY "Usuários veem próprios endereços"
    ON addresses FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprios endereços"
    ON addresses FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios endereços"
    ON addresses FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprios endereços"
    ON addresses FOR DELETE
    USING (auth.uid() = user_id);

-- Accounts: usuários gerenciam próprias contas
CREATE POLICY "Usuários veem próprias contas"
    ON accounts FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprias contas"
    ON accounts FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprias contas"
    ON accounts FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprias contas"
    ON accounts FOR DELETE
    USING (auth.uid() = user_id);

-- Transactions: usuários veem transações de suas contas
CREATE POLICY "Usuários veem próprias transações"
    ON transactions FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
        OR
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.destination_account_id 
            AND user_id = auth.uid()
        )
    );

CREATE POLICY "Usuários criam transações de suas contas"
    ON transactions FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
    );

CREATE POLICY "Usuários atualizam próprias transações"
    ON transactions FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
    );

CREATE POLICY "Usuários deletam próprias transações"
    ON transactions FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE account_id = transactions.source_account_id 
            AND user_id = auth.uid()
        )
    );

-- Budgets: usuários gerenciam próprios orçamentos
CREATE POLICY "Usuários veem próprios orçamentos"
    ON budgets FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprios orçamentos"
    ON budgets FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios orçamentos"
    ON budgets FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprios orçamentos"
    ON budgets FOR DELETE
    USING (auth.uid() = user_id);

-- Savings Goals: usuários gerenciam próprias metas
CREATE POLICY "Usuários veem próprias metas"
    ON savings_goals FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprias metas"
    ON savings_goals FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprias metas"
    ON savings_goals FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprias metas"
    ON savings_goals FOR DELETE
    USING (auth.uid() = user_id);

-- Cards: usuários gerenciam próprios cartões
CREATE POLICY "Usuários veem próprios cartões"
    ON cards FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprios cartões"
    ON cards FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios cartões"
    ON cards FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprios cartões"
    ON cards FOR DELETE
    USING (auth.uid() = user_id);

-- Bills: usuários gerenciam próprios boletos
CREATE POLICY "Usuários veem próprios boletos"
    ON bills FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprios boletos"
    ON bills FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios boletos"
    ON bills FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprios boletos"
    ON bills FOR DELETE
    USING (auth.uid() = user_id);

-- User Assets: usuários gerenciam próprios investimentos
CREATE POLICY "Usuários veem próprios investimentos"
    ON investments.user_asset FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprios investimentos"
    ON investments.user_asset FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprios investimentos"
    ON investments.user_asset FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprios investimentos"
    ON investments.user_asset FOR DELETE
    USING (auth.uid() = user_id);

-- Investment Transactions: usuários gerenciam próprias transações de investimento
CREATE POLICY "Usuários veem próprias transações de investimento"
    ON investments.transaction FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Usuários criam próprias transações de investimento"
    ON investments.transaction FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários atualizam próprias transações de investimento"
    ON investments.transaction FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Usuários deletam próprias transações de investimento"
    ON investments.transaction FOR DELETE
    USING (auth.uid() = user_id);

-- 8. ÍNDICES PARA PERFORMANCE
-- ============================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tax_id ON users(tax_id);
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest_account ON transactions(destination_account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_savings_goals_user_id ON savings_goals(user_id);
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_due_date ON bills(due_date);
CREATE INDEX idx_inv_user_asset_user_id ON investments.user_asset(user_id);
CREATE INDEX idx_inv_transaction_user_id ON investments.transaction(user_id);

-- ============================================================
-- SCRIPT COMPLETO - ARQUITETURA MODERNA SUPABASE
-- UUID + Auth + RLS Seguro
-- ============================================================
