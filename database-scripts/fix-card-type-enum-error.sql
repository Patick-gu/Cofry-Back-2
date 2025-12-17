-- =============================================
-- Script para corrigir o erro do enum card_type_enum
-- Erro: type "card_type_enun" does not exist
-- =============================================

-- 1. Verificar se existe algum enum com nome incorreto
SELECT typname, oid 
FROM pg_type 
WHERE typname LIKE '%card_type%';

-- 2. Se existir "card_type_enun" (com erro), vamos renomeá-lo ou recriar
-- Primeiro, vamos verificar se a tabela cards existe e usa algum enum
DO $$
BEGIN
    -- Se existir o enum com nome errado, vamos renomeá-lo
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'card_type_enun') THEN
        -- Verificar se há alguma tabela usando esse enum
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'cards' 
                   AND udt_name = 'card_type_enun') THEN
            -- Se a tabela existe e usa o enum errado, precisamos recriar
            RAISE NOTICE 'Enum com nome errado encontrado. Recriando...';
            
            -- Remover dependências temporariamente (não podemos fazer isso facilmente)
            -- Melhor solução: recriar o enum correto
        END IF;
    END IF;
END $$;

-- 3. Criar o enum correto se não existir
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'card_type_enum') THEN
        CREATE TYPE card_type_enum AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
        RAISE NOTICE 'Enum card_type_enum criado com sucesso!';
    ELSE
        RAISE NOTICE 'Enum card_type_enum já existe.';
    END IF;
END $$;

-- 4. Se a tabela cards não existir ou tiver o enum errado, vamos corrigir
-- Primeiro, vamos verificar a estrutura atual
SELECT 
    column_name,
    udt_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'cards' 
AND column_name = 'card_type';

-- 5. Se a coluna card_type usar o enum errado, precisamos alterar
-- Isso requer recriar a coluna (cuidado: isso apaga dados se houver)
-- COMENTADO POR SEGURANÇA - Descomente apenas se necessário
/*
-- Verificar se há dados na tabela
SELECT COUNT(*) FROM cards;

-- Se não houver dados, podemos recriar a coluna:
ALTER TABLE cards DROP COLUMN IF EXISTS card_type;
ALTER TABLE cards ADD COLUMN card_type card_type_enum NOT NULL;
*/

-- 6. Script completo para recriar a tabela (APENAS SE NÃO HOUVER DADOS IMPORTANTES)
-- DESCOMENTE APENAS SE VOCÊ TEM CERTEZA QUE PODE PERDER OS DADOS DA TABELA
/*
DROP TABLE IF EXISTS cards CASCADE;

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

CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);
CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_status ON cards(status);
*/

-- 7. Verificar resultado final
SELECT 
    typname as enum_name,
    oid
FROM pg_type 
WHERE typname = 'card_type_enum';

SELECT 
    table_name,
    column_name,
    udt_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'cards';

