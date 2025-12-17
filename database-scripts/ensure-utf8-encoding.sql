-- Script para garantir que o database está usando UTF-8 encoding
-- Execute este script para verificar e corrigir o encoding do database se necessário

-- Verifica o encoding atual do database
SELECT datname, pg_encoding_to_char(encoding) as encoding 
FROM pg_database 
WHERE datname = 'Cofry-local';

-- Se o encoding não for UTF8, você precisará recriar o database:
-- 1. Fazer backup dos dados
-- 2. DROP DATABASE "Cofry-local";
-- 3. CREATE DATABASE "Cofry-local" WITH ENCODING 'UTF8' LC_COLLATE='pt_BR.UTF-8' LC_CTYPE='pt_BR.UTF-8' TEMPLATE=template0;
-- 4. Restaurar os dados

-- Verifica o encoding das colunas de texto na tabela accounts
SELECT 
    column_name, 
    data_type, 
    character_set_name,
    collation_name
FROM information_schema.columns 
WHERE table_name = 'accounts' 
  AND table_schema = 'public'
  AND data_type LIKE '%character%'
ORDER BY ordinal_position;

-- Para garantir UTF-8 na coluna bank_name (se necessário recriar)
-- ALTER TABLE accounts ALTER COLUMN bank_name TYPE VARCHAR(100) COLLATE "C";


