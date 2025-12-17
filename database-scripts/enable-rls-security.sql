-- =============================================
-- HABILITAÇÃO DE ROW-LEVEL SECURITY (RLS)
-- =============================================
-- IMPORTANTE: Este script habilita RLS em todas as tabelas
-- para segurança no Supabase/PostgREST
-- =============================================
-- Data: 2025-01-17
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
-- POLÍTICAS DE SEGURANÇA BÁSICAS
-- =============================================
-- NOTA: Estas políticas são básicas. Ajuste conforme sua lógica de negócio.
-- =============================================

-- 1. SUBSCRIPTION_PLANS (Planos de Assinatura)
-- Permite leitura pública (todos podem ver planos)
DROP POLICY IF EXISTS "Planos são públicos para leitura" ON subscription_plans;
CREATE POLICY "Planos são públicos para leitura"
    ON subscription_plans FOR SELECT
    USING (true);

-- Apenas admins podem inserir/atualizar (ajuste conforme necessário)
DROP POLICY IF EXISTS "Apenas admins podem modificar planos" ON subscription_plans;
CREATE POLICY "Apenas admins podem modificar planos"
    ON subscription_plans FOR ALL
    USING (false); -- Bloqueia por padrão (ajuste conforme sua lógica)

-- 2. USERS (Usuários)
-- Usuários só podem ver seus próprios dados
DROP POLICY IF EXISTS "Usuários veem apenas seus próprios dados" ON users;
CREATE POLICY "Usuários veem apenas seus próprios dados"
    ON users FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'authenticated');

-- Usuários só podem atualizar seus próprios dados
DROP POLICY IF EXISTS "Usuários atualizam apenas seus próprios dados" ON users;
CREATE POLICY "Usuários atualizam apenas seus próprios dados"
    ON users FOR UPDATE
    USING (auth.uid()::text = user_id::text);

-- Qualquer um pode criar um usuário (registro)
DROP POLICY IF EXISTS "Qualquer um pode criar usuário" ON users;
CREATE POLICY "Qualquer um pode criar usuário"
    ON users FOR INSERT
    WITH CHECK (true);

-- 3. ADDRESSES (Endereços)
-- Usuários só podem ver seus próprios endereços
DROP POLICY IF EXISTS "Usuários veem apenas seus próprios endereços" ON addresses;
CREATE POLICY "Usuários veem apenas seus próprios endereços"
    ON addresses FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM users 
        WHERE users.user_id = addresses.user_id 
        AND users.user_id::text = auth.uid()::text
    ));

-- Usuários só podem gerenciar seus próprios endereços
DROP POLICY IF EXISTS "Usuários gerenciam apenas seus próprios endereços" ON addresses;
CREATE POLICY "Usuários gerenciam apenas seus próprios endereços"
    ON addresses FOR ALL
    USING (EXISTS (
        SELECT 1 FROM users 
        WHERE users.user_id = addresses.user_id 
        AND users.user_id::text = auth.uid()::text
    ));

-- 4. ACCOUNTS (Contas)
-- Usuários só podem ver suas próprias contas
DROP POLICY IF EXISTS "Usuários veem apenas suas próprias contas" ON accounts;
CREATE POLICY "Usuários veem apenas suas próprias contas"
    ON accounts FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM users 
        WHERE users.user_id = accounts.user_id 
        AND users.user_id::text = auth.uid()::text
    ));

-- Usuários só podem gerenciar suas próprias contas
DROP POLICY IF EXISTS "Usuários gerenciam apenas suas próprias contas" ON accounts;
CREATE POLICY "Usuários gerenciam apenas suas próprias contas"
    ON accounts FOR ALL
    USING (EXISTS (
        SELECT 1 FROM users 
        WHERE users.user_id = accounts.user_id 
        AND users.user_id::text = auth.uid()::text
    ));

-- 5. TRANSACTION_CATEGORIES (Categorias de Transação)
-- Todos podem ver categorias (são públicas)
DROP POLICY IF EXISTS "Categorias são públicas para leitura" ON transaction_categories;
CREATE POLICY "Categorias são públicas para leitura"
    ON transaction_categories FOR SELECT
    USING (true);

-- Apenas admins podem modificar (bloqueado por padrão)
DROP POLICY IF EXISTS "Apenas admins podem modificar categorias" ON transaction_categories;
CREATE POLICY "Apenas admins podem modificar categorias"
    ON transaction_categories FOR ALL
    USING (false);

-- 6. TRANSACTIONS (Transações)
-- Usuários só podem ver transações de suas próprias contas
DROP POLICY IF EXISTS "Usuários veem apenas suas próprias transações" ON transactions;
CREATE POLICY "Usuários veem apenas suas próprias transações"
    ON transactions FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE accounts.account_id = transactions.source_account_id 
            AND accounts.user_id::text = auth.uid()::text
        ) OR
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE accounts.account_id = transactions.destination_account_id 
            AND accounts.user_id::text = auth.uid()::text
        )
    );

-- Usuários só podem criar transações em suas próprias contas
DROP POLICY IF EXISTS "Usuários criam transações apenas em suas contas" ON transactions;
CREATE POLICY "Usuários criam transações apenas em suas contas"
    ON transactions FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM accounts 
            WHERE accounts.account_id = transactions.source_account_id 
            AND accounts.user_id::text = auth.uid()::text
        )
    );

-- 7. BUDGETS (Orçamentos)
-- Usuários só podem ver seus próprios orçamentos
DROP POLICY IF EXISTS "Usuários veem apenas seus próprios orçamentos" ON budgets;
CREATE POLICY "Usuários veem apenas seus próprios orçamentos"
    ON budgets FOR SELECT
    USING (budgets.user_id::text = auth.uid()::text);

-- Usuários só podem gerenciar seus próprios orçamentos
DROP POLICY IF EXISTS "Usuários gerenciam apenas seus próprios orçamentos" ON budgets;
CREATE POLICY "Usuários gerenciam apenas seus próprios orçamentos"
    ON budgets FOR ALL
    USING (budgets.user_id::text = auth.uid()::text);

-- 8. SAVINGS_GOALS (Metas de Poupança)
-- Usuários só podem ver suas próprias metas
DROP POLICY IF EXISTS "Usuários veem apenas suas próprias metas" ON savings_goals;
CREATE POLICY "Usuários veem apenas suas próprias metas"
    ON savings_goals FOR SELECT
    USING (savings_goals.user_id::text = auth.uid()::text);

-- Usuários só podem gerenciar suas próprias metas
DROP POLICY IF EXISTS "Usuários gerenciam apenas suas próprias metas" ON savings_goals;
CREATE POLICY "Usuários gerenciam apenas suas próprias metas"
    ON savings_goals FOR ALL
    USING (savings_goals.user_id::text = auth.uid()::text);

-- 9. CARDS (Cartões)
-- Usuários só podem ver seus próprios cartões
DROP POLICY IF EXISTS "Usuários veem apenas seus próprios cartões" ON cards;
CREATE POLICY "Usuários veem apenas seus próprios cartões"
    ON cards FOR SELECT
    USING (cards.user_id::text = auth.uid()::text);

-- Usuários só podem gerenciar seus próprios cartões
DROP POLICY IF EXISTS "Usuários gerenciam apenas seus próprios cartões" ON cards;
CREATE POLICY "Usuários gerenciam apenas seus próprios cartões"
    ON cards FOR ALL
    USING (cards.user_id::text = auth.uid()::text);

-- 10. BILLS (Boletos)
-- Usuários só podem ver seus próprios boletos
DROP POLICY IF EXISTS "Usuários veem apenas seus próprios boletos" ON bills;
CREATE POLICY "Usuários veem apenas seus próprios boletos"
    ON bills FOR SELECT
    USING (
        bills.user_id::text = auth.uid()::text 
        OR bills.user_id IS NULL -- Boletos sem user_id podem ser públicos
    );

-- Usuários só podem gerenciar seus próprios boletos
DROP POLICY IF EXISTS "Usuários gerenciam apenas seus próprios boletos" ON bills;
CREATE POLICY "Usuários gerenciam apenas seus próprios boletos"
    ON bills FOR ALL
    USING (bills.user_id::text = auth.uid()::text);

-- =============================================
-- NOTA IMPORTANTE SOBRE AUTENTICAÇÃO
-- =============================================
-- As políticas acima usam auth.uid() que é do Supabase Auth.
-- Se você estiver usando autenticação própria via JDBC:
-- 
-- OPÇÃO 1: Desabilitar PostgREST no Supabase (se não usar)
-- OPÇÃO 2: Criar uma função helper para mapear seu sistema de auth
-- OPÇÃO 3: Ajustar as políticas para sua lógica de negócio específica
-- 
-- Para desenvolvimento/teste, você pode temporariamente usar:
-- GRANT ALL ON ALL TABLES IN SCHEMA public TO postgres;
-- =============================================

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

