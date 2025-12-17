-- =============================================
-- Creation of Bills Table (Boletos)
-- =============================================
-- IMPORTANT: All entities must be named in English
-- =============================================

-- Drop enum if exists
DROP TYPE IF EXISTS bill_status_enum;

-- Create enum for bill status
CREATE TYPE bill_status_enum AS ENUM ('OPEN', 'OVERDUE', 'PAID');

-- Drop table if exists
DROP TABLE IF EXISTS bills CASCADE;

-- Create bills table
CREATE TABLE bills (
    bill_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status bill_status_enum NOT NULL DEFAULT 'OPEN',
    bank_code VARCHAR(3) NOT NULL,
    wallet_code VARCHAR(5) NOT NULL,
    our_number VARCHAR(23) NOT NULL,
    bill_code VARCHAR(48) NOT NULL UNIQUE,
    user_id INT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT check_positive_amount CHECK (amount > 0)
);

-- Indexes for better performance
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_due_date ON bills(due_date);
CREATE INDEX idx_bills_created_at ON bills(created_at);
CREATE INDEX idx_bills_bill_code ON bills(bill_code);

-- Comments
COMMENT ON TABLE bills IS 'Table for storing bank bills (boletos)';
COMMENT ON COLUMN bills.bill_id IS 'Primary key - bill identifier';
COMMENT ON COLUMN bills.title IS 'Bill title/description';
COMMENT ON COLUMN bills.amount IS 'Bill amount in BRL';
COMMENT ON COLUMN bills.due_date IS 'Bill due date';
COMMENT ON COLUMN bills.status IS 'Bill status: OPEN, OVERDUE, or PAID';
COMMENT ON COLUMN bills.bank_code IS 'FEBRABAN bank code (3 digits)';
COMMENT ON COLUMN bills.wallet_code IS 'Bank wallet code (5 digits)';
COMMENT ON COLUMN bills.our_number IS 'Our number (up to 23 digits)';
COMMENT ON COLUMN bills.bill_code IS 'Bill barcode (48 digits)';
COMMENT ON COLUMN bills.user_id IS 'User ID - foreign key to users table';
COMMENT ON COLUMN bills.paid_at IS 'Payment date and time';

