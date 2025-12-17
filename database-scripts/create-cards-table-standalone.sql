-- =============================================
-- Script para criar a tabela de Cartões
-- Execute este script se a tabela cards não existir no banco
-- =============================================

-- Criar enum para tipo de cartão (se não existir)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'card_type_enum') THEN
        CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
    END IF;
END $$;

-- Criar tabela de cartões (se não existir)
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

-- Criar índices (se não existirem)
CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);
CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_status ON cards(status);

-- Verificar se a tabela foi criada corretamente
SELECT 
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'cards'
ORDER BY ordinal_position;

-- Mensagem de sucesso
DO $$ 
BEGIN
    RAISE NOTICE 'Tabela cards criada com sucesso!';
END $$;

