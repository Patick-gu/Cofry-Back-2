-- Script simples: Atualizar saldo do usuário mario@teste.com para R$ 50.000,00
-- Atualiza a primeira conta encontrada do usuário (a conta com menor account_id)

UPDATE accounts
SET balance = 50000.00
WHERE account_id = (
    SELECT MIN(account_id)
    FROM accounts
    WHERE user_id = (SELECT user_id FROM users WHERE email = 'mario@teste.com')
);

-- Verificar resultado
SELECT 
    u.email,
    u.first_name || ' ' || u.last_name AS nome,
    a.account_number AS conta,
    a.balance AS saldo
FROM users u
JOIN accounts a ON a.user_id = u.user_id
WHERE u.email = 'mario@teste.com';

