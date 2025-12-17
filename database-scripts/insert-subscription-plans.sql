-- =============================================
-- INSERÇÃO DE PLANOS DE ASSINATURA
-- =============================================
-- Este script insere os planos padrão do sistema Cofry
-- Execute este script após criar a tabela subscription_plans
-- =============================================

-- Remove planos existentes (opcional - apenas se quiser recriar)
-- DELETE FROM subscription_plans;

-- Insere os planos de assinatura
INSERT INTO subscription_plans (name, price, description, created_at) VALUES
('Cofry Start', 0.00, 'Plano gratuito com funcionalidades básicas', CURRENT_TIMESTAMP),
('Cofry Pro', 7.77, 'Plano intermediário com recursos avançados', CURRENT_TIMESTAMP),
('Cofry Black', 47.99, 'Plano premium com todos os recursos disponíveis', CURRENT_TIMESTAMP);

-- Verifica os planos inseridos
SELECT * FROM subscription_plans ORDER BY plan_id;

