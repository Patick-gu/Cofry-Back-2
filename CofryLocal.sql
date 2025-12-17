-- =============================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS COFRY
-- =============================================
-- IMPORTANTE: Este script está preparado para UTF-8
-- Certifique-se de que o banco de dados PostgreSQL está configurado com encoding UTF-8
-- 
-- Para verificar o encoding atual:
--   SELECT datname, pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'nome_do_banco';
--
-- Para criar o banco com UTF-8 (se ainda não existe):
--   CREATE DATABASE nome_do_banco 
--     WITH ENCODING 'UTF8' 
--     LC_COLLATE='pt_BR.UTF-8' 
--     LC_CTYPE='pt_BR.UTF-8' 
--     TEMPLATE template0;
-- =============================================

-- =============================================
-- 1. LIMPEZA DO AMBIENTE (DROP)
-- =============================================
-- Drop do schema investments e suas tabelas
DROP SCHEMA IF EXISTS investments CASCADE;

DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS budgets CASCADE;
DROP TABLE IF EXISTS savings_goals CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS transaction_categories CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS subscription_plans CASCADE;

DROP TYPE IF EXISTS card_type_enum;
DROP TYPE IF EXISTS account_type_enum;
DROP TYPE IF EXISTS transaction_type_enum;
DROP TYPE IF EXISTS goal_status_enum;

-- =============================================
-- 2. CRIAÇÃO DE ENUMS
-- =============================================
CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
CREATE TYPE account_type_enum AS ENUM ('CHECKING', 'SAVINGS');
CREATE TYPE transaction_type_enum AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT');
CREATE TYPE goal_status_enum AS ENUM ('IN_PROGRESS', 'COMPLETED', 'PAUSED');

-- =============================================
-- 3. CRIAÇÃO DAS TABELAS (ESTRUTURA ATUALIZADA)
-- =============================================

-- Tabela: Planos
CREATE TABLE subscription_plans (
                                    plan_id SERIAL PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL UNIQUE,
                                    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                                    description TEXT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela: Usuários (Com senha opcional, conforme solicitado)
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       plan_id INT DEFAULT 1,

                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       tax_id VARCHAR(14) NOT NULL UNIQUE, -- CPF
                       email VARCHAR(150) NOT NULL UNIQUE,
                       phone_number VARCHAR(20),

    -- Atualização: Campo NULLABLE para permitir usuários sem senha (login social)
                       password_hash VARCHAR(255),

                       date_of_birth DATE NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
);

-- Tabela: Endereços
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

-- Tabela: Contas Bancárias
-- Certifique-se de que o database está com encoding UTF-8
-- Para verificar: SELECT datname, pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'Cofry-local';
CREATE TABLE accounts (
                          account_id SERIAL PRIMARY KEY,
                          user_id INT NOT NULL,
                          bank_code VARCHAR(3),                          -- Código FEBRABAN do banco (ex: 001)
                          bank_name VARCHAR(100) COLLATE "C",            -- Nome do banco (ex: Banco do Brasil) - UTF-8
                          account_number VARCHAR(20) NOT NULL UNIQUE,
                          agency_number VARCHAR(10) NOT NULL,
                          account_type account_type_enum NOT NULL DEFAULT 'CHECKING',
                          balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          status VARCHAR(20) DEFAULT 'ACTIVE',

                          CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- Índice para facilitar buscas por código do banco
CREATE INDEX IF NOT EXISTS idx_accounts_bank_code ON accounts(bank_code);

-- Tabela: Categorias
CREATE TABLE transaction_categories (
                                        category_id SERIAL PRIMARY KEY,
                                        name VARCHAR(50) NOT NULL UNIQUE,
                                        icon_code VARCHAR(50),
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela: Transações (Versão Final com Parcelas e Recorrência)
CREATE TABLE transactions (
                              transaction_id SERIAL PRIMARY KEY,
                              source_account_id INT NOT NULL,
                              destination_account_id INT,
                              category_id INT,

                              amount NUMERIC(15, 2) NOT NULL,
                              transaction_type transaction_type_enum NOT NULL,
                              description VARCHAR(255) NOT NULL,

    -- Campos UI
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

-- Tabela: Orçamentos
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

-- Tabela: Metas de Poupança
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

-- Tabela: Cartões
CREATE TABLE cards (
    card_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT, -- Opcional: vinculado a uma conta
    card_number VARCHAR(25) NOT NULL, -- Número mascarado (ex: "**** **** **** 2222") - aceita até 25 caracteres
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(4), -- Não armazenar em produção sem criptografia adequada
    card_type card_type_enum NOT NULL,
    brand VARCHAR(50), -- Visa, Mastercard, Elo, etc.
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, EXPIRED
    limit_amount NUMERIC(15, 2), -- Limite do cartão (para crédito)
    current_balance NUMERIC(15, 2) DEFAULT 0.00, -- Saldo usado (para crédito)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);
CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_status ON cards(status);

-- =============================================
-- 4. NOTA SOBRE DADOS BASE
-- =============================================
-- Usuários, planos e categorias de transações devem ser cadastrados através da API do sistema
-- Este script apenas cria a estrutura das tabelas e insere dados de investimentos (ativos)

-- =============================================
-- 5. DADOS DE INVESTIMENTOS (ATIVOS)
-- =============================================
-- Script preparado para UTF-8
-- Certifique-se de que o banco de dados está configurado com encoding UTF-8

-- 5.1 Criação do Esquema de Investimentos
CREATE SCHEMA IF NOT EXISTS investments;

-- 5.2 Criação da Tabela asset_category (Catálogo de Categorias)
-- UTF-8: Nomes de categorias podem conter caracteres especiais
CREATE TABLE investments.asset_category (
                                            id SERIAL PRIMARY KEY,
                                            name VARCHAR(50) NOT NULL UNIQUE -- UTF-8: Aceita caracteres especiais
);

-- 5.3 Criação da Tabela asset (Catálogo de Ativos Negociáveis)
-- UTF-8: Nomes de ativos podem conter acentos e caracteres especiais
CREATE TABLE investments.asset (
                                   id SERIAL PRIMARY KEY,
                                   ticker VARCHAR(10) NOT NULL UNIQUE, -- Código do ativo (ex: PETR4, BTC)
                                   name VARCHAR(100) NOT NULL, -- UTF-8: Nome completo do ativo (pode ter acentos)
                                   category_id INT NOT NULL,
                                   api_identifier VARCHAR(50) NOT NULL, -- Identificador para API externa
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Chave estrangeira para a categoria
                                   CONSTRAINT fk_asset_category
                                       FOREIGN KEY (category_id)
                                           REFERENCES investments.asset_category (id)
);

-- 5.4 Criação da Tabela user_asset (Posição Atual do Usuário)
-- Assume que a tabela 'users' existe no esquema padrão (ou 'public') com PK 'user_id'
-- UTF-8: Preparado para armazenar dados de investimentos
CREATE TABLE investments.user_asset (
                                        id SERIAL PRIMARY KEY, -- Use INT PRIMARY KEY AUTO_INCREMENT para MySQL
                                        user_id INT NOT NULL,
                                        asset_id INT NOT NULL,
                                        quantity NUMERIC(18, 8) NOT NULL CHECK (quantity >= 0),
                                        average_price NUMERIC(18, 8) NOT NULL,
                                        last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Garante que um usuário só tenha uma linha por ativo (posição única)
                                        UNIQUE (user_id, asset_id),

    -- Chaves estrangeiras
                                        CONSTRAINT fk_user_asset_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES users (user_id),
                                        CONSTRAINT fk_user_asset_asset
                                            FOREIGN KEY (asset_id)
                                                REFERENCES investments.asset (id)
);

-- 5.5 Criação da Tabela transaction (Histórico de Ordens de Investimento)
-- UTF-8: Campo 'type' aceita valores 'Compra' e 'Venda' com caracteres especiais
CREATE TABLE investments.transaction (
                                         id SERIAL PRIMARY KEY,
                                         user_id INT NOT NULL,
                                         asset_id INT NOT NULL,
                                         type VARCHAR(10) NOT NULL CHECK (type IN ('Compra', 'Venda')), -- UTF-8: Valores em português
                                         price NUMERIC(18, 8) NOT NULL CHECK (price > 0),
                                         quantity NUMERIC(18, 8) NOT NULL CHECK (quantity > 0),
                                         total_value NUMERIC(18, 8) NOT NULL CHECK (total_value > 0),
                                         transaction_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         status VARCHAR(20) NOT NULL, -- UTF-8: Status pode conter caracteres especiais

    -- Chaves estrangeiras
                                         CONSTRAINT fk_transaction_user
                                             FOREIGN KEY (user_id)
                                                 REFERENCES users (user_id),
                                         CONSTRAINT fk_transaction_asset
                                             FOREIGN KEY (asset_id)
                                                 REFERENCES investments.asset (id)
);

-- =============================================
-- 6. INSERÇÃO DOS DADOS DE INVESTIMENTOS
-- =============================================
-- Todos os dados de investimentos são inseridos aqui
-- Certifique-se de que o banco está com encoding UTF-8

-- 6.1 Inserção das Categorias de Ativos
INSERT INTO investments.asset_category (name) VALUES
                                                  ('Ações BR'),
                                                  ('Cripto'),
                                                  ('Renda Fixa / FIIs');


-- 6.2 Inserção das Ações Brasileiras (category_id = 1) - 20 Ativos
INSERT INTO investments.asset (ticker, name, category_id, api_identifier, is_active) VALUES
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

-- 6.3 Inserção das Criptomoedas (category_id = 2) - 20 Ativos
INSERT INTO investments.asset (ticker, name, category_id, api_identifier, is_active) VALUES
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

-- 6.4 Inserção de Renda Fixa e FIIs (category_id = 3) - 10 Ativos
-- Estes não usarão API de cotação diária, mas são essenciais para o cálculo da distribuição.
INSERT INTO investments.asset (ticker, name, category_id, api_identifier, is_active) VALUES
                                                                                         ('CDB1', 'CDB Banco Alfa 100% CDI', 3, 'FIXO_CDB1', TRUE),
                                                                                         ('LCI2', 'LCI Banco Beta 95% CDI', 3, 'FIXO_LCI2', TRUE),
                                                                                         ('LCA3', 'LCA Banco Gama', 3, 'FIXO_LCA3', TRUE),
                                                                                         ('TESSELIC', 'Tesouro Selic', 3, 'FIXO_SELIC', TRUE),
                                                                                         ('TESIPCA', 'Tesouro IPCA+ 2035', 3, 'FIXO_IPCA', TRUE),
                                                                                         ('MXRF11', 'Maxi Renda FII', 3, 'MXRF11.SA', TRUE),
                                                                                         ('KNRI11', 'Kinea Renda Imobiliária', 3, 'KNRI11.SA', TRUE),
                                                                                         ('HGLG11', 'CSHG Logística FII', 3, 'HGLG11.SA', TRUE),
                                                                                         ('PETR3F', 'Opção de Compra PETR3', 3, 'OPC_PETR3', TRUE), -- Exemplo de um derivativo/opção
                                                                                         ('COEBR1', 'COE Bancos BR', 3, 'EST_COE1', TRUE);

-- =============================================
-- FIM DO SCRIPT
-- =============================================
-- Script concluído com sucesso!
-- 
-- IMPORTANTE: Certifique-se de que o banco de dados está configurado com UTF-8
-- Para verificar no PostgreSQL:
--   SELECT datname, pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'nome_do_banco';
-- 
-- Para garantir UTF-8:
--   CREATE DATABASE nome_do_banco WITH ENCODING 'UTF8' LC_COLLATE='pt_BR.UTF-8' LC_CTYPE='pt_BR.UTF-8';
--
-- Usuários, planos e categorias de transações devem ser cadastrados através da API do sistema.
-- Este script apenas cria a estrutura das tabelas e insere dados de investimentos (ativos).