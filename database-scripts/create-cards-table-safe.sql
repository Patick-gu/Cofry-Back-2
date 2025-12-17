-- =============================================
-- Script SEGURO para criar a tabela de Cartões
-- Este script verifica e corrige o enum antes de criar a tabela
-- =============================================

-- 1. Remover tabela cards se existir (APENAS SE NÃO HOUVER DADOS IMPORTANTES)
-- Se você tem dados importantes, NÃO execute esta linha
-- DROP TABLE IF EXISTS cards CASCADE;

-- 2. Remover enum com nome errado se existir
DROP TYPE IF EXISTS card_type_enun CASCADE;

-- 3. Remover enum correto se existir (para recriar)
DROP TYPE IF EXISTS card_type_enum CASCADE;

-- 4. Criar o enum CORRETO
CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');

-- 5. Criar a tabela cards (se não existir)
CREATE TABLE IF NOT EXISTS cards (
    card_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT,
    card_number VARCHAR(25) NOT NULL, -- Aumentado para 25 caracteres para suportar números mascarados
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(4),
    card_type card_type_enum NOT NULL, -- Usa o enum CORRETO
    brand VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    limit_amount NUMERIC(15, 2),
    current_balance NUMERIC(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- 6. Criar índices
CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);
CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_status ON cards(status);

-- 7. Verificar se foi criado corretamente
SELECT 
    'Enum criado:' as info,
    typname as enum_name
FROM pg_type 
WHERE typname = 'card_type_enum';

SELECT 
    'Tabela criada:' as info,
    table_name,
    column_name,
    udt_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'cards'
ORDER BY ordinal_position;

-- Mensagem de sucesso
DO $$ 
BEGIN
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'Tabela cards criada com sucesso!';
    RAISE NOTICE 'Enum card_type_enum criado corretamente!';
    RAISE NOTICE '=========================================';
END $$;

