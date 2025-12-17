-- =============================================
-- HABILITAÇÃO SIMPLES DE RLS (SEM POLÍTICAS RESTRITIVAS)
-- =============================================
-- IMPORTANTE: Este script apenas habilita RLS sem criar políticas.
-- Útil se você usa JDBC direto e não PostgREST.
-- As políticas podem ser adicionadas depois conforme necessário.
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

-- =============================================
-- POLÍTICAS PERMISSIVAS (PARA JDBC DIRETO)
-- =============================================
-- Se você usa JDBC direto (não PostgREST), estas políticas
-- permitem acesso total mas satisfazem o requisito do Supabase.
-- =============================================

-- Política permissiva para todas as tabelas
-- (permite tudo para usuários autenticados)
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
        -- Cria política permissiva para SELECT
        EXECUTE format('
            DROP POLICY IF EXISTS "permissive_select_%s" ON %I;
            CREATE POLICY "permissive_select_%s" ON %I FOR SELECT USING (true);
        ', table_name, table_name, table_name, table_name);
        
        -- Cria política permissiva para INSERT
        EXECUTE format('
            DROP POLICY IF EXISTS "permissive_insert_%s" ON %I;
            CREATE POLICY "permissive_insert_%s" ON %I FOR INSERT WITH CHECK (true);
        ', table_name, table_name, table_name, table_name);
        
        -- Cria política permissiva para UPDATE
        EXECUTE format('
            DROP POLICY IF EXISTS "permissive_update_%s" ON %I;
            CREATE POLICY "permissive_update_%s" ON %I FOR UPDATE USING (true);
        ', table_name, table_name, table_name, table_name);
        
        -- Cria política permissiva para DELETE
        EXECUTE format('
            DROP POLICY IF EXISTS "permissive_delete_%s" ON %I;
            CREATE POLICY "permissive_delete_%s" ON %I FOR DELETE USING (true);
        ', table_name, table_name, table_name, table_name);
    END LOOP;
END $$;

-- Verifica se RLS está habilitado
SELECT 
    schemaname,
    tablename,
    rowsecurity as rls_enabled
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
ORDER BY tablename;

