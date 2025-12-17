-- Script para adicionar campos de banco na tabela accounts
-- Executar este script no banco de dados antes de usar as novas funcionalidades

-- Adiciona coluna bank_code (código do banco FEBRABAN - 3 dígitos)
ALTER TABLE accounts 
ADD COLUMN IF NOT EXISTS bank_code VARCHAR(3);

-- Adiciona coluna bank_name (nome do banco)
ALTER TABLE accounts 
ADD COLUMN IF NOT EXISTS bank_name VARCHAR(100);

-- Cria índice para facilitar buscas por código do banco
CREATE INDEX IF NOT EXISTS idx_accounts_bank_code ON accounts(bank_code);

-- Comentários para documentação
COMMENT ON COLUMN accounts.bank_code IS 'Código FEBRABAN do banco (ex: 001 para Banco do Brasil)';
COMMENT ON COLUMN accounts.bank_name IS 'Nome do banco (ex: Banco do Brasil)';


