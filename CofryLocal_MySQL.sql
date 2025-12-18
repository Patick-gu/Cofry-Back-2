-- =============================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS COFRY (MySQL)
-- =============================================
-- IMPORTANTE: Este script está preparado para UTF-8
-- Certifique-se de que o banco de dados MySQL está configurado com charset utf8mb4
-- 
-- Para verificar o charset atual:
--   SHOW CREATE DATABASE nome_do_banco;
--
-- Para criar o banco com UTF-8 (se ainda não existe):
--   CREATE DATABASE nome_do_banco 
--     CHARACTER SET utf8mb4 
--     COLLATE utf8mb4_unicode_ci;
-- =============================================

-- =============================================
-- 1. LIMPEZA DO AMBIENTE (DROP)
-- =============================================

DROP DATABASE IF EXISTS investments;

DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS savings_goals;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS transaction_categories;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS subscription_plans;

-- =============================================
-- 2. CRIAÇÃO DAS TABELAS (COM ENUMS)
-- =============================================

-- Tabela: Planos
CREATE TABLE subscription_plans (
    plan_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Usuários (Com senha opcional, conforme solicitado)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    plan_id INT DEFAULT 1,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(14) NOT NULL UNIQUE COMMENT 'CPF',
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password_hash VARCHAR(255) COMMENT 'NULLABLE para permitir usuários sem senha (login social)',
    date_of_birth DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Endereços
CREATE TABLE addresses (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Contas Bancárias
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    bank_code VARCHAR(3) COMMENT 'Código FEBRABAN do banco (ex: 001)',
    bank_name VARCHAR(100) COMMENT 'Nome do banco (ex: Banco do Brasil) - UTF-8',
    account_number VARCHAR(20) NOT NULL UNIQUE,
    agency_number VARCHAR(10) NOT NULL,
    account_type ENUM('CHECKING', 'SAVINGS') NOT NULL DEFAULT 'CHECKING',
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice para facilitar buscas por código do banco
CREATE INDEX idx_accounts_bank_code ON accounts(bank_code);

-- Tabela: Categorias
CREATE TABLE transaction_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Transações (Versão Final com Parcelas e Recorrência)
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    source_account_id INT NOT NULL,
    destination_account_id INT,
    category_id INT,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT') NOT NULL,
    description VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    is_recurring BOOLEAN DEFAULT FALSE,
    installment_current INT,
    installment_total INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_positive_amount CHECK (amount > 0),
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_dest_account FOREIGN KEY (destination_account_id) REFERENCES accounts(account_id),
    CONSTRAINT fk_trans_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Orçamentos
CREATE TABLE budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount_limit DECIMAL(15, 2) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_budget UNIQUE (user_id, category_id, period_month, period_year),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES transaction_categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Metas de Poupança
CREATE TABLE savings_goals (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) DEFAULT 0.00,
    target_date DATE,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'PAUSED') DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: Cartões
CREATE TABLE cards (
    card_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT COMMENT 'Opcional: vinculado a uma conta',
    card_number VARCHAR(25) NOT NULL COMMENT 'Número mascarado (ex: "**** **** **** 2222") - aceita até 25 caracteres',
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(4) COMMENT 'Não armazenar em produção sem criptografia adequada',
    card_type ENUM('CREDIT', 'DEBIT', 'PREPAID') NOT NULL,
    brand VARCHAR(50) COMMENT 'Visa, Mastercard, Elo, etc.',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, BLOCKED, EXPIRED',
    limit_amount DECIMAL(15, 2) COMMENT 'Limite do cartão (para crédito)',
    current_balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Saldo usado (para crédito)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para melhor performance
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_status ON cards(status);

-- Tabela: Boletos (Bills)
CREATE TABLE bills (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('OPEN', 'OVERDUE', 'PAID') NOT NULL DEFAULT 'OPEN',
    bank_code VARCHAR(3) NOT NULL,
    wallet_code VARCHAR(5) NOT NULL,
    our_number VARCHAR(23) NOT NULL,
    bill_code VARCHAR(48) NOT NULL UNIQUE,
    user_id INT,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT check_positive_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para bills
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_due_date ON bills(due_date);
CREATE INDEX idx_bills_created_at ON bills(created_at);
CREATE INDEX idx_bills_bill_code ON bills(bill_code);

-- =============================================
-- 3. INSERÇÃO DE DADOS BASE
-- =============================================

-- 3.1 Planos de Assinatura
INSERT INTO subscription_plans (name, price, description, created_at) VALUES
('Cofry Start', 0.00, 'Plano gratuito com funcionalidades básicas', CURRENT_TIMESTAMP),
('Cofry Pro', 7.77, 'Plano intermediário com recursos avançados', CURRENT_TIMESTAMP),
('Cofry Black', 47.99, 'Plano premium com todos os recursos disponíveis', CURRENT_TIMESTAMP);

-- 3.2 Categorias de Transações
INSERT INTO transaction_categories (name, icon_code, created_at) VALUES
('Alimentação', 'food', CURRENT_TIMESTAMP),
('Transporte', 'transport', CURRENT_TIMESTAMP),
('Moradia', 'home', CURRENT_TIMESTAMP),
('Saúde', 'health', CURRENT_TIMESTAMP),
('Educação', 'education', CURRENT_TIMESTAMP),
('Lazer', 'leisure', CURRENT_TIMESTAMP),
('Compras', 'shopping', CURRENT_TIMESTAMP),
('Contas', 'bills', CURRENT_TIMESTAMP),
('Salário', 'salary', CURRENT_TIMESTAMP),
('Outros', 'other', CURRENT_TIMESTAMP);

-- =============================================
-- 4. DADOS DE INVESTIMENTOS (ATIVOS)
-- =============================================
-- Script preparado para UTF-8
-- Certifique-se de que o banco de dados está configurado com charset utf8mb4

-- 4.1 Criação do Banco/Schema de Investimentos (MySQL não usa schema, usaremos prefixo)
CREATE DATABASE IF NOT EXISTS investments CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE investments;

-- 4.2 Criação da Tabela asset_category (Catálogo de Categorias)
CREATE TABLE asset_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'UTF-8: Aceita caracteres especiais'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.3 Criação da Tabela asset (Catálogo de Ativos Negociáveis)
CREATE TABLE asset (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL UNIQUE COMMENT 'Código do ativo (ex: PETR4, BTC)',
    name VARCHAR(100) NOT NULL COMMENT 'UTF-8: Nome completo do ativo (pode ter acentos)',
    category_id INT NOT NULL,
    api_identifier VARCHAR(50) NOT NULL COMMENT 'Identificador para API externa',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_asset_category FOREIGN KEY (category_id) REFERENCES asset_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.4 Criação da Tabela user_asset (Posição Atual do Usuário)
-- Assume que a tabela 'users' existe no banco padrão com PK 'user_id'
CREATE TABLE user_asset (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    quantity DECIMAL(18, 8) NOT NULL CHECK (quantity >= 0),
    average_price DECIMAL(18, 8) NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, asset_id),
    CONSTRAINT fk_user_asset_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_asset_asset FOREIGN KEY (asset_id) REFERENCES asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4.5 Criação da Tabela transaction (Histórico de Ordens de Investimento)
CREATE TABLE transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('Compra', 'Venda')) COMMENT 'UTF-8: Valores em português',
    price DECIMAL(18, 8) NOT NULL CHECK (price > 0),
    quantity DECIMAL(18, 8) NOT NULL CHECK (quantity > 0),
    total_value DECIMAL(18, 8) NOT NULL CHECK (total_value > 0),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL COMMENT 'UTF-8: Status pode conter caracteres especiais',
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_transaction_asset FOREIGN KEY (asset_id) REFERENCES asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Voltar para o banco padrão
USE postgres;

-- =============================================
-- 5. INSERÇÃO DOS DADOS DE INVESTIMENTOS
-- =============================================
USE investments;

-- 5.1 Inserção das Categorias de Ativos
INSERT INTO asset_category (name) VALUES
('Ações BR'),
('Cripto'),
('Renda Fixa / FIIs');

-- 5.2 Inserção das Ações Brasileiras (category_id = 1) - 20 Ativos
INSERT INTO asset (ticker, name, category_id, api_identifier, is_active) VALUES
('PETR4', 'Petrobras PN', 1, 'PETR4.SA', TRUE),
('VALE3', 'Vale S.A.', 1, 'VALE3.SA', TRUE),
('ITUB4', 'Itaú Unibanco PN', 1, 'ITUB4.SA', TRUE),
('BBDC4', 'Bradesco PN', 1, 'BBDC4.SA', TRUE),
('ELET3', 'Eletrobras ON', 1, 'ELET3.SA', TRUE),
('WEGE3', 'Weg S.A.', 1, 'WEGE3.SA', TRUE),
('MGLU3', 'Magazine Luiza ON', 1, 'MGLU3.SA', TRUE),
('BBAS3', 'Banco do Brasil ON', 1, 'BBAS3.SA', TRUE),
('SUZB3', 'Suzano ON', 1, 'SUZB3.SA', TRUE),
('GGBR4', 'Gerdau PN', 1, 'GGBR4.SA', TRUE),
('LREN3', 'Lojas Renner ON', 1, 'LREN3.SA', TRUE),
('RADL3', 'Raia Drogasil ON', 1, 'RADL3.SA', TRUE),
('RENT3', 'Localiza ON', 1, 'RENT3.SA', TRUE),
('B3SA3', 'B3 S.A. - Brasil Bolsa Balcão', 1, 'B3SA3.SA', TRUE),
('SANB11', 'Banco Santander Unit', 1, 'SANB11.SA', TRUE),
('AZUL4', 'Azul S.A. PN', 1, 'AZUL4.SA', TRUE),
('VIVT3', 'Vivo ON', 1, 'VIVT3.SA', TRUE),
('CMIG4', 'Cemig PN', 1, 'CMIG4.SA', TRUE),
('HAPV3', 'Hapvida ON', 1, 'HAPV3.SA', TRUE),
('ENEV3', 'Eneva ON', 1, 'ENEV3.SA', TRUE);

-- 5.3 Inserção das Criptomoedas (category_id = 2) - 20 Ativos
INSERT INTO asset (ticker, name, category_id, api_identifier, is_active) VALUES
('BTC', 'Bitcoin', 2, 'bitcoin', TRUE),
('ETH', 'Ethereum', 2, 'ethereum', TRUE),
('SOL', 'Solana', 2, 'solana', TRUE),
('BNB', 'Binance Coin', 2, 'binancecoin', TRUE),
('ADA', 'Cardano', 2, 'cardano', TRUE),
('XRP', 'XRP', 2, 'ripple', TRUE),
('DOGE', 'Dogecoin', 2, 'dogecoin', TRUE),
('AVAX', 'Avalanche', 2, 'avalanche-2', TRUE),
('DOT', 'Polkadot', 2, 'polkadot', TRUE),
('LINK', 'Chainlink', 2, 'chainlink', TRUE),
('LTC', 'Litecoin', 2, 'litecoin', TRUE),
('UNI', 'Uniswap', 2, 'uniswap', TRUE),
('MATIC', 'Polygon', 2, 'matic-network', TRUE),
('SHIB', 'Shiba Inu', 2, 'shiba-inu', TRUE),
('TRX', 'TRON', 2, 'tron', TRUE),
('ETC', 'Ethereum Classic', 2, 'ethereum-classic', TRUE),
('XMR', 'Monero', 2, 'monero', TRUE),
('BCH', 'Bitcoin Cash', 2, 'bitcoin-cash', TRUE),
('VET', 'VeChain', 2, 'vechain', TRUE),
('ALGO', 'Algorand', 2, 'algorand', TRUE);

-- 5.4 Inserção de Renda Fixa e FIIs (category_id = 3) - 10 Ativos
INSERT INTO asset (ticker, name, category_id, api_identifier, is_active) VALUES
('CDB1', 'CDB Banco Alfa 100% CDI', 3, 'FIXO_CDB1', TRUE),
('LCI2', 'LCI Banco Beta 95% CDI', 3, 'FIXO_LCI2', TRUE),
('LCA3', 'LCA Banco Gama', 3, 'FIXO_LCA3', TRUE),
('TESSELIC', 'Tesouro Selic', 3, 'FIXO_SELIC', TRUE),
('TESIPCA', 'Tesouro IPCA+ 2035', 3, 'FIXO_IPCA', TRUE),
('MXRF11', 'Maxi Renda FII', 3, 'MXRF11.SA', TRUE),
('KNRI11', 'Kinea Renda Imobiliária', 3, 'KNRI11.SA', TRUE),
('HGLG11', 'CSHG Logística FII', 3, 'HGLG11.SA', TRUE),
('PETR3F', 'Opção de Compra PETR3', 3, 'OPC_PETR3', TRUE),
('COEBR1', 'COE Bancos BR', 3, 'EST_COE1', TRUE);

-- Voltar para o banco padrão
USE postgres;

-- =============================================
-- 6. INSERÇÃO DE 20 BOLETOS DE EXEMPLO
-- =============================================
-- IMPORTANTE: Os boletos são inseridos sem user_id (podem ser atribuídos depois via CPF)
-- Os códigos de boleto seguem o padrão FEBRABAN (48 dígitos)

-- Boleto 1: Conta de Luz - Banco Caixa (104)
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Conta de Energia Elétrica', 185.50, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'OPEN', '104', '001', '000000000000000000001', '10490010012345678901234500234500018550000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 2: Conta de Água - Banco do Brasil (001)
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Conta de Água e Esgoto', 87.30, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 'OPEN', '001', '17', '000000000000000000002', '00190170123456789012345600234500008730000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 3: Internet - Banco Santander (033)
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Internet Fibra Ótica', 129.90, DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), 'OPEN', '033', '126', '000000000000000000003', '03391260198765432109876500234500012990000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 4: Telefone
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Telefone Celular', 49.99, DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), 'OPEN', '341', '109', '000000000000000000004', '3419109000000000000000040234500004999000000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 5: Plano de Saúde
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Plano de Saúde Mensal', 450.00, DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 'OPEN', '001', '17', '000000000000000000005', '0019017000000000000000050234500045000000000005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 6: Seguro Auto
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Seguro Veicular', 320.00, DATE_ADD(CURRENT_DATE, INTERVAL 18 DAY), 'OPEN', '341', '109', '000000000000000000006', '3419109000000000000000060234500032000000000006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 7: TV por Assinatura
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('TV por Assinatura', 89.90, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 'OPEN', '033', '126', '000000000000000000007', '0339126000000000000000070234500008990000000007', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 8: Academia
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Academia Mensalidade', 120.00, DATE_ADD(CURRENT_DATE, INTERVAL 22 DAY), 'OPEN', '104', '001', '000000000000000000008', '1049001000000000000000080234500012000000000008', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 9: Cartão de Crédito
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Fatura Cartão de Crédito', 580.75, DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 'OPEN', '001', '17', '000000000000000000009', '0019017000000000000000090234500058075000000009', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 10: Condomínio
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Condomínio Residencial', 380.00, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'OPEN', '341', '109', '000000000000000000010', '3419109000000000000000100234500038000000000010', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 11: Faculdade
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Mensalidade Faculdade', 650.00, DATE_ADD(CURRENT_DATE, INTERVAL 8 DAY), 'OPEN', '033', '126', '000000000000000000011', '0339126000000000000000110234500065000000000011', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 12: Financiamento
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Parcela Financiamento', 850.50, DATE_ADD(CURRENT_DATE, INTERVAL 28 DAY), 'OPEN', '104', '001', '000000000000000000012', '1049001000000000000000120234500085050000000012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 13: IPTU
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('IPTU - Imposto Predial', 420.00, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 'OPEN', '001', '17', '000000000000000000013', '0019017000000000000000130234500042000000000013', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 14: IPVA
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('IPVA - Imposto Veicular', 680.00, DATE_ADD(CURRENT_DATE, INTERVAL 35 DAY), 'OPEN', '341', '109', '000000000000000000014', '3419109000000000000000140234500068000000000014', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 15: Licenciamento
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Licenciamento Veículo', 125.00, DATE_ADD(CURRENT_DATE, INTERVAL 38 DAY), 'OPEN', '033', '126', '000000000000000000015', '0339126000000000000000150234500012500000000015', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 16: Gás
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Gás Natural Residencial', 65.80, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'OPEN', '104', '001', '000000000000000000016', '1049001000000000000000160234500006580000000016', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 17: Escola
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Mensalidade Escola', 520.00, DATE_ADD(CURRENT_DATE, INTERVAL 17 DAY), 'OPEN', '001', '17', '000000000000000000017', '0019017000000000000000170234500052000000000017', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 18: Streaming
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Assinatura Streaming', 39.90, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'OPEN', '341', '109', '000000000000000000018', '3419109000000000000000180234500003990000000018', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 19: Material Escolar
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Material Escolar', 280.50, DATE_ADD(CURRENT_DATE, INTERVAL 40 DAY), 'OPEN', '033', '126', '000000000000000000019', '0339126000000000000000190234500028050000000019', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Boleto 20: Supermercado
INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, created_at, updated_at) VALUES
('Compra Supermercado', 450.75, DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), 'OPEN', '104', '001', '000000000000000000020', '1049001000000000000000200234500045075000000020', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================
-- FIM DO SCRIPT
-- =============================================
-- Script concluído com sucesso!
-- 
-- IMPORTANTE: Certifique-se de que o banco de dados está configurado com utf8mb4
-- Para verificar no MySQL:
--   SHOW CREATE DATABASE nome_do_banco;
-- 
-- Para garantir UTF-8:
--   CREATE DATABASE nome_do_banco CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--
-- NOTA: MySQL não possui ROW LEVEL SECURITY como PostgreSQL
-- A segurança deve ser implementada na camada de aplicação
--
-- Usuários, planos e categorias de transações devem ser cadastrados através da API do sistema.
-- Este script apenas cria a estrutura das tabelas e insere dados de investimentos (ativos).

