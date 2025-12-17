-- =============================================
-- CRIAÇÃO DA TABELA subscription_plans
-- =============================================
-- Este script cria a tabela de planos de assinatura
-- Execute antes de inserir os planos
-- =============================================

-- Remove a tabela se já existir (CUIDADO: isso apagará todos os dados!)
-- DROP TABLE IF EXISTS subscription_plans CASCADE;

-- Cria a tabela de planos de assinatura
CREATE TABLE IF NOT EXISTS subscription_plans (
    plan_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Verifica se a tabela foi criada
SELECT 
    table_name, 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'subscription_plans'
ORDER BY ordinal_position;

-- =============================================
-- PRÓXIMOS PASSOS:
-- =============================================
-- 1. Execute o script insert-subscription-plans.sql para inserir os planos padrão
-- 2. Verifique se os planos foram inseridos corretamente:
--    SELECT * FROM subscription_plans ORDER BY plan_id;
-- =============================================

