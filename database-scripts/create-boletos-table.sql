-- =============================================
-- Criação da tabela de Boletos
-- =============================================

-- Criar enum para status do boleto
DROP TYPE IF EXISTS boleto_status_enum;
CREATE TYPE boleto_status_enum AS ENUM ('OPEN', 'OVERDUE', 'PAID');

-- Criar tabela de boletos
CREATE TABLE IF NOT EXISTS boletos (
    boleto_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status boleto_status_enum NOT NULL DEFAULT 'OPEN',
    bank_code VARCHAR(3) NOT NULL,
    wallet_code VARCHAR(5) NOT NULL,
    our_number VARCHAR(23) NOT NULL,
    boleto_code VARCHAR(48) NOT NULL UNIQUE,
    user_id INT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_boleto_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Índices para melhor performance
CREATE INDEX idx_boletos_user_id ON boletos(user_id);
CREATE INDEX idx_boletos_status ON boletos(status);
CREATE INDEX idx_boletos_due_date ON boletos(due_date);
CREATE INDEX idx_boletos_created_at ON boletos(created_at);

