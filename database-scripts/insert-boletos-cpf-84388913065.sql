-- =============================================
-- Script para inserir 5 boletos para o CPF 843.889.130-65
-- =============================================
-- Data: 16 de Janeiro de 2025
-- 
-- IMPORTANTE: Execute este script apenas se o usuário com CPF 843.889.130-65 já existir no banco.
-- O script irá buscar o user_id automaticamente pelo CPF.
-- =============================================

-- Primeiro, vamos obter o user_id do CPF informado
DO $$
DECLARE
    v_user_id INT;
    v_boleto_code_1 VARCHAR(48);
    v_boleto_code_2 VARCHAR(48);
    v_boleto_code_3 VARCHAR(48);
    v_boleto_code_4 VARCHAR(48);
    v_boleto_code_5 VARCHAR(48);
BEGIN
    -- Busca o user_id pelo CPF
    SELECT user_id INTO v_user_id
    FROM users
    WHERE tax_id = '843.889.130-65';
    
    -- Verifica se o usuário existe
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Usuário com CPF 843.889.130-65 não encontrado. Por favor, crie o usuário primeiro.';
    END IF;
    
    RAISE NOTICE 'Usuário encontrado com user_id: %', v_user_id;
    
    -- Gera os códigos de boleto (48 dígitos cada)
    -- Formato: Bank(3) + Currency(1) + Wallet(5) + OurNumber1(15) + DV(1) + DueDate(5) + Value(10) + OurNumber2(8)
    -- Nota: Os dígitos verificadores (DV) são aproximados para demonstração
    
    -- Boleto 1: Claro Residencial - Banco 001 (BB) - R$ 149,90 - Vence 15/10/2025
    -- Fator de vencimento (desde 07/10/1997): ~10247 dias
    -- Valor em centavos: 14990
    v_boleto_code_1 := '001917001234567890123451024700014990012345678';
    
    -- Boleto 2: Seguro Auto - Banco 341 (Itaú) - R$ 250,00 - Vence 20/10/2025
    -- Fator de vencimento: ~10252 dias
    -- Valor em centavos: 25000
    v_boleto_code_2 := '341910998765432109876541025200002500009876543';
    
    -- Boleto 3: Internet Fibra - Banco 033 (Santander) - R$ 89,90 - Vence 18/10/2025
    -- Fator de vencimento: ~10250 dias
    -- Valor em centavos: 8990
    v_boleto_code_3 := '033912601112223334445551025000000899001112233';
    
    -- Boleto 4: Energia Elétrica - Banco 104 (Caixa) - R$ 180,50 - Vence 25/10/2025
    -- Fator de vencimento: ~10257 dias
    -- Valor em centavos: 18050
    v_boleto_code_4 := '104910955566677788899901025700001805005556667';
    
    -- Boleto 5: Plano de Saúde - Banco 001 (BB) - R$ 350,00 - Vence 30/10/2025
    -- Fator de vencimento: ~10262 dias
    -- Valor em centavos: 35000
    v_boleto_code_5 := '001917009998887776665551026200003500009998887';
    
    -- Insere os 5 boletos
    INSERT INTO bills (title, amount, due_date, status, bank_code, wallet_code, our_number, bill_code, user_id, created_at, updated_at)
    VALUES 
    -- Boleto 1: Claro Residencial (Vencido)
    (
        'Claro Residencial',
        149.90,
        '2025-10-15',
        CASE 
            WHEN CURRENT_DATE > '2025-10-15' THEN 'OVERDUE'::bill_status_enum
            ELSE 'OPEN'::bill_status_enum
        END,
        '001',
        '17',
        '00000012345678901234567',
        v_boleto_code_1,
        v_user_id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    
    -- Boleto 2: Seguro Auto
    (
        'Seguro Auto',
        250.00,
        '2025-10-20',
        CASE 
            WHEN CURRENT_DATE > '2025-10-20' THEN 'OVERDUE'::bill_status_enum
            ELSE 'OPEN'::bill_status_enum
        END,
        '341',
        '109',
        '00000098765432109876543',
        v_boleto_code_2,
        v_user_id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    
    -- Boleto 3: Internet Fibra
    (
        'Internet Fibra',
        89.90,
        '2025-10-18',
        CASE 
            WHEN CURRENT_DATE > '2025-10-18' THEN 'OVERDUE'::bill_status_enum
            ELSE 'OPEN'::bill_status_enum
        END,
        '033',
        '126',
        '00000011122233344455566',
        v_boleto_code_3,
        v_user_id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    
    -- Boleto 4: Energia Elétrica
    (
        'Energia Elétrica',
        180.50,
        '2025-10-25',
        CASE 
            WHEN CURRENT_DATE > '2025-10-25' THEN 'OVERDUE'::bill_status_enum
            ELSE 'OPEN'::bill_status_enum
        END,
        '104',
        '109',
        '00000055566677788899900',
        v_boleto_code_4,
        v_user_id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    
    -- Boleto 5: Plano de Saúde
    (
        'Plano de Saúde',
        350.00,
        '2025-10-30',
        CASE 
            WHEN CURRENT_DATE > '2025-10-30' THEN 'OVERDUE'::bill_status_enum
            ELSE 'OPEN'::bill_status_enum
        END,
        '001',
        '17',
        '00000099988877766655544',
        v_boleto_code_5,
        v_user_id,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    RAISE NOTICE '5 boletos inseridos com sucesso para o usuário com CPF 843.889.130-65';
    
END $$;

-- Verifica os boletos inseridos
SELECT 
    b.bill_id,
    b.title,
    b.amount,
    b.due_date,
    b.status,
    b.bank_code,
    u.tax_id as cpf,
    u.first_name || ' ' || u.last_name as usuario
FROM bills b
INNER JOIN users u ON b.user_id = u.user_id
WHERE u.tax_id = '843.889.130-65'
ORDER BY b.due_date ASC;

