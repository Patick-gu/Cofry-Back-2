-- Script para corrigir a coluna created_at na tabela transaction_categories
-- Execute este script no banco de dados antes de usar a aplicação

-- Primeiro, atualiza registros existentes com NULL para ter um timestamp
UPDATE transaction_categories 
SET created_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;

-- Agora, adiciona a constraint NOT NULL (se ainda não existir)
-- Note: Se a coluna já existe como nullable, precisamos torná-la NOT NULL
DO $$ 
BEGIN
    -- Verifica se a coluna já permite NULL e atualiza registros
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'transaction_categories' 
        AND column_name = 'created_at' 
        AND is_nullable = 'YES'
    ) THEN
        -- Atualiza registros NULL
        UPDATE transaction_categories 
        SET created_at = CURRENT_TIMESTAMP 
        WHERE created_at IS NULL;
        
        -- Altera coluna para NOT NULL
        ALTER TABLE transaction_categories 
        ALTER COLUMN created_at SET NOT NULL;
        
        -- Define default
        ALTER TABLE transaction_categories 
        ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

