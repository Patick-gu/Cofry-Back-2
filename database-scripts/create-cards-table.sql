-- =============================================
-- Criação da tabela de Cartões
-- =============================================

-- Criar enum para tipo de cartão
DROP TYPE IF EXISTS card_type_enum;
CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');

-- Criar tabela de cartões
CREATE TABLE IF NOT EXISTS cards (
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
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_status ON cards(status);

