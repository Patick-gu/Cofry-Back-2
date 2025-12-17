-- =============================================
-- APLICAR RLS NO SUPABASE (BANCO EXISTENTE)
-- =============================================
-- Execute este script no Supabase SQL Editor
-- IMPORTANTE: Apenas habilita RLS, não recria tabelas
-- =============================================

-- Habilita RLS nas tabelas principais
ALTER TABLE subscription_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE savings_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE cards ENABLE ROW LEVEL SECURITY;
ALTER TABLE bills ENABLE ROW LEVEL SECURITY;

-- Remove políticas antigas se existirem (evita erros)
DO $$
DECLARE
    table_name text;
    policy_name text;
BEGIN
    FOR table_name IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename IN (
            'subscription_plans',
            'users',
            'addresses',
            'accounts',
            'transaction_categories',
            'transactions',
            'budgets',
            'savings_goals',
            'cards',
            'bills'
        )
    LOOP
        -- Remove todas as políticas antigas
        FOR policy_name IN 
            SELECT policyname 
            FROM pg_policies 
            WHERE schemaname = 'public' 
            AND tablename = table_name
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON %I', policy_name, table_name);
        END LOOP;
    END LOOP;
END $$;

-- Cria políticas permissivas (para JDBC direto)
DO $$
DECLARE
    table_name text;
BEGIN
    FOR table_name IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename IN (
            'subscription_plans',
            'users',
            'addresses',
            'accounts',
            'transaction_categories',
            'transactions',
            'budgets',
            'savings_goals',
            'cards',
            'bills'
        )
    LOOP
        -- Política para SELECT
        EXECUTE format('
            CREATE POLICY "permissive_select_%s" ON %I FOR SELECT USING (true);
        ', table_name, table_name);
        
        -- Política para INSERT
        EXECUTE format('
            CREATE POLICY "permissive_insert_%s" ON %I FOR INSERT WITH CHECK (true);
        ', table_name, table_name);
        
        -- Política para UPDATE
        EXECUTE format('
            CREATE POLICY "permissive_update_%s" ON %I FOR UPDATE USING (true);
        ', table_name, table_name);
        
        -- Política para DELETE
        EXECUTE format('
            CREATE POLICY "permissive_delete_%s" ON %I FOR DELETE USING (true);
        ', table_name, table_name);
    END LOOP;
END $$;

-- Verifica se RLS está habilitado
SELECT 
    schemaname,
    tablename,
    rowsecurity as rls_enabled,
    (SELECT count(*) FROM pg_policies WHERE schemaname = 'public' AND tablename = t.tablename) as policies_count
FROM pg_tables t
WHERE schemaname = 'public'
AND tablename IN (
    'subscription_plans',
    'users',
    'addresses',
    'accounts',
    'transaction_categories',
    'transactions',
    'budgets',
    'savings_goals',
    'cards',
    'bills'
)
ORDER BY tablename;

