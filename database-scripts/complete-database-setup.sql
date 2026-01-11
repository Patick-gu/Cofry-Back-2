-- ============================================================
-- SCRIPT COMPLETO PARA SUPABASE - ESTRUTURA LEGADA
-- Compatível com RLS e políticas de segurança do Supabase
-- ============================================================

-- 1. LIMPEZA TOTAL (RESET)
-- ============================================================
-- Primeiro desabilitar RLS temporariamente para limpeza
DO $$ 
DECLARE 
    tbl record; 
BEGIN 
    FOR tbl IN 
        SELECT schemaname, tablename 
        FROM pg_tables 
        WHERE schemaname IN ('public', 'investments') 
          AND tableowner = current_user
    LOOP 
        EXECUTE format('ALTER TABLE %I.%I DISABLE ROW LEVEL SECURITY;', tbl.schemaname, tbl.tablename);
        EXECUTE format('DROP TABLE IF EXISTS %I.%I CASCADE;', tbl.schemaname, tbl.tablename);
    END LOOP;
END $$;

-- Limpar schemas e tipos
DROP SCHEMA IF EXISTS investments CASCADE;

-- Limpeza de Enums (precisa ser após as tabelas)
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

-- 3. CRIAÇÃO DAS TABELAS (PUBLIC) - ID NUMÉRICO + SENHA
-- ============================================================

-- Subscription Plans
CREATE TABLE subscription_plans (
    plan_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users (com password_hash)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    plan_id INT DEFAULT 1,
    
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(14) UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    
    password_hash VARCHAR(255),
    
    date_of_birth DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
);

-- Addresses
CREATE TABLE addresses (
    address_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) DEFAULT 'Brazil',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_address FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Accounts
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    bank_code VARCHAR(3),
    bank_name VARCHAR(100),
    account_number VARCHAR(20) NOT NULL,
    agency_number VARCHAR(10) NOT NULL,
    account_type account_type_enum NOT NULL DEFAULT 'CHECKING',
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    
    CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Transaction Categories
CREATE TABLE transaction_categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    source_account_id INT NOT NULL,
    destination_account_id INT,
    category_id INT,
    amount NUMERIC(15, 2) NOT NULL,
    transaction_type transaction_type_enum NOT NULL,
    description VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    is_recurring BOOLEAN DEFAULT FALSE,
    installment_current INT,
    installment_total INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_positive_amount CHECK (amount > 0),
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_dest_account FOREIGN KEY (destination_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_trans_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
);

-- Budgets
CREATE TABLE budgets (
    budget_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount_limit NUMERIC(15, 2) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_budget UNIQUE (user_id, category_id, period_month, period_year),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
);

-- Savings Goals
CREATE TABLE savings_goals (
    goal_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount NUMERIC(15, 2) NOT NULL,
    current_amount NUMERIC(15, 2) DEFAULT 0.00,
    target_date DATE,
    status goal_status_enum DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Cards
CREATE TABLE cards (
    card_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT,
    card_number VARCHAR(25) NOT NULL,
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(4),
    card_type card_type_enum NOT NULL,
    brand VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    limit_amount NUMERIC(15, 2),
    current_balance NUMERIC(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- Bills
CREATE TABLE bills (
    bill_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status bill_status_enum NOT NULL DEFAULT 'OPEN',
    bank_code VARCHAR(3) NOT NULL,
    wallet_code VARCHAR(5) NOT NULL,
    our_number VARCHAR(23) NOT NULL,
    bill_code VARCHAR(48) NOT NULL UNIQUE,
    user_id INT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
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
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity >= 0),
    average_price NUMERIC(18, 8) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (user_id, asset_id),
    CONSTRAINT fk_user_asset_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_asset_asset FOREIGN KEY (asset_id) REFERENCES investments.asset (id)
);

-- Investment Transactions
CREATE TABLE investments.transaction (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('Compra', 'Venda')),
    price NUMERIC(18, 8) NOT NULL CHECK (price > 0),
    quantity NUMERIC(18, 8) NOT NULL CHECK (quantity > 0),
    total_value NUMERIC(18, 8) NOT NULL CHECK (total_value > 0),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_asset FOREIGN KEY (asset_id) REFERENCES investments.asset (id)
);

-- 5. INSERÇÃO DE DADOS INICIAIS
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

-- Boletos de Exemplo (20 boletos)
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code) VALUES
('Conta de Energia Elétrica', 185.50, CURRENT_DATE + INTERVAL '5 days', 'OPEN', '104', '001', '000000000000000000001', '10490010012345678901234500234500018550000000001'),
('Conta de Água e Esgoto', 87.30, CURRENT_DATE + INTERVAL '7 days', 'OPEN', '001', '17', '000000000000000000002', '00190170123456789012345600234500008730000000002'),
('Internet Fibra Ótica', 129.90, CURRENT_DATE + INTERVAL '10 days', 'OPEN', '033', '126', '000000000000000000003', '03391260198765432109876500234500012990000000003'),
('Telefone Celular', 49.99, CURRENT_DATE + INTERVAL '12 days', 'OPEN', '341', '109', '000000000000000000004', '3419109000000000000000040234500004999000000004'),
('Plano de Saúde Mensal', 450.00, CURRENT_DATE + INTERVAL '15 days', 'OPEN', '001', '17', '000000000000000000005', '0019017000000000000000050234500045000000000005'),
('Seguro Veicular', 320.00, CURRENT_DATE + INTERVAL '18 days', 'OPEN', '341', '109', '000000000000000000006', '3419109000000000000000060234500032000000000006'),
('TV por Assinatura', 89.90, CURRENT_DATE + INTERVAL '20 days', 'OPEN', '033', '126', '000000000000000000007', '0339126000000000000000070234500008990000000007'),
('Academia Mensalidade', 120.00, CURRENT_DATE + INTERVAL '22 days', 'OPEN', '104', '001', '000000000000000000008', '1049001000000000000000080234500012000000000008'),
('Fatura Cartão de Crédito', 580.75, CURRENT_DATE + INTERVAL '25 days', 'OPEN', '001', '17', '000000000000000000009', '0019017000000000000000090234500058075000000009'),
('Condomínio Residencial', 380.00, CURRENT_DATE + INTERVAL '3 days', 'OPEN', '341', '109', '000000000000000000010', '3419109000000000000000100234500038000000000010'),
('Mensalidade Faculdade', 650.00, CURRENT_DATE + INTERVAL '8 days', 'OPEN', '033', '126', '000000000000000000011', '0339126000000000000000110234500065000000000011'),
('Parcela Financiamento', 850.50, CURRENT_DATE + INTERVAL '28 days', 'OPEN', '104', '001', '000000000000000000012', '1049001000000000000000120234500085050000000012'),
('IPTU - Imposto Predial', 420.00, CURRENT_DATE + INTERVAL '30 days', 'OPEN', '001', '17', '000000000000000000013', '0019017000000000000000130234500042000000000013'),
('IPVA - Imposto Veicular', 680.00, CURRENT_DATE + INTERVAL '35 days', 'OPEN', '341', '109', '000000000000000000014', '3419109000000000000000140234500068000000000014'),
('Licenciamento Veículo', 125.00, CURRENT_DATE + INTERVAL '38 days', 'OPEN', '033', '126', '000000000000000000015', '0339126000000000000000150234500012500000000015'),
('Gás Natural Residencial', 65.80, CURRENT_DATE + INTERVAL '14 days', 'OPEN', '104', '001', '000000000000000000016', '1049001000000000000000160234500006580000000016'),
('Mensalidade Escola', 520.00, CURRENT_DATE + INTERVAL '17 days', 'OPEN', '001', '17', '000000000000000000017', '0019017000000000000000170234500052000000000017'),
('Assinatura Streaming', 39.90, CURRENT_DATE + INTERVAL '1 day', 'OPEN', '341', '109', '000000000000000000018', '3419109000000000000000180234500003990000000018'),
('Material Escolar', 280.50, CURRENT_DATE + INTERVAL '40 days', 'OPEN', '033', '126', '000000000000000000019', '0339126000000000000000190234500028050000000019'),
('Compra Supermercado', 450.75, CURRENT_DATE + INTERVAL '45 days', 'OPEN', '104', '001', '000000000000000000020', '1049001000000000000000200234500045075000000020');

-- 6. CONFIGURAÇÃO DE POLÍTICAS RLS (ACESSO TOTAL PARA DESENVOLVIMENTO)
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

-- Criar políticas de acesso total (ALLOW ALL) para desenvolvimento
-- NOTA: Em produção, substitua por políticas baseadas em auth.uid()

-- PUBLIC SCHEMA
CREATE POLICY allow_all_subscription_plans ON subscription_plans FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_users ON users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_addresses ON addresses FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_accounts ON accounts FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_transaction_categories ON transaction_categories FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_transactions ON transactions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_budgets ON budgets FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_savings_goals ON savings_goals FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_cards ON cards FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_bills ON bills FOR ALL USING (true) WITH CHECK (true);

-- INVESTMENTS SCHEMA
CREATE POLICY allow_all_asset_category ON investments.asset_category FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_asset ON investments.asset FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_user_asset ON investments.user_asset FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY allow_all_inv_transaction ON investments.transaction FOR ALL USING (true) WITH CHECK (true);

-- 7. PERMISSÕES ANON/AUTHENTICATED
-- ============================================================

-- Permitir acesso anônimo e autenticado (necessário para JDBC)
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT USAGE ON SCHEMA investments TO anon, authenticated;

GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL TABLES IN SCHEMA investments TO anon, authenticated;

GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL SEQUENCES IN SCHEMA investments TO anon, authenticated;

-- 8. ÍNDICES PARA PERFORMANCE
-- ============================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tax_id ON users(tax_id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest_account ON transactions(destination_account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_due_date ON bills(due_date);
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_inv_user_asset_user_id ON investments.user_asset(user_id);

-- ============================================================
-- SCRIPT COMPLETO - PRONTO PARA EXECUÇÃO NO SUPABASE
-- ============================================================
