-- Script para atualizar saldo do usuário mario@teste.com para R$ 50.000,00

-- Opção 1: Atualizar o saldo da primeira conta encontrada do usuário
UPDATE accounts
SET balance = 50000.00
WHERE user_id = (
    SELECT user_id 
    FROM users 
    WHERE email = 'mario@teste.com'
)
AND account_id = (
    SELECT MIN(account_id) 
    FROM accounts 
    WHERE user_id = (
        SELECT user_id 
        FROM users 
        WHERE email = 'mario@teste.com'
    )
);

-- Opção 2: Atualizar o saldo de TODAS as contas do usuário
-- (Descomente se quiser atualizar todas as contas ao invés de apenas a primeira)
/*
UPDATE accounts
SET balance = 50000.00
WHERE user_id = (
    SELECT user_id 
    FROM users 
    WHERE email = 'mario@teste.com'
);
*/

-- Opção 3: Se o usuário não tiver conta, criar uma nova conta com saldo de R$ 50.000,00
-- (Descomente se precisar criar uma conta)
/*
INSERT INTO accounts (
    user_id,
    account_number,
    agency_number,
    account_type,
    balance,
    status
)
SELECT 
    u.user_id,
    CONCAT('ACC-', EXTRACT(YEAR FROM CURRENT_DATE), '-', LPAD((SELECT COALESCE(MAX(account_id), 0) + 1 FROM accounts)::TEXT, 6, '0')),
    '0001',
    'CHECKING',
    50000.00,
    'ACTIVE'
FROM users u
WHERE u.email = 'mario@teste.com'
AND NOT EXISTS (
    SELECT 1 FROM accounts a WHERE a.user_id = u.user_id
);
*/

-- Verificar o resultado
SELECT 
    u.user_id,
    u.email,
    u.first_name,
    u.last_name,
    a.account_id,
    a.account_number,
    a.balance,
    a.status
FROM users u
LEFT JOIN accounts a ON a.user_id = u.user_id
WHERE u.email = 'mario@teste.com';

