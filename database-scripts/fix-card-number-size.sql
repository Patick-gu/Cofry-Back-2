-- Script para corrigir o tamanho do campo card_number na tabela cards
-- O campo precisa aceitar números mascarados que podem ter até 25 caracteres
-- Exemplo: "**** **** **** 2222" = 19 caracteres

-- Aumentar o tamanho do campo card_number de VARCHAR(16) para VARCHAR(25)
ALTER TABLE cards 
ALTER COLUMN card_number TYPE VARCHAR(25);

-- Verificar se a alteração foi aplicada
SELECT 
    column_name, 
    data_type, 
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'cards' 
AND column_name = 'card_number';

