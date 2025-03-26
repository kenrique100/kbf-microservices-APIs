-- Create transaction table
CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    created_by VARCHAR(100) NOT NULL,

-- Indexes for optimized queries
    CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME', 'EXPENSE', 'INVESTMENT'))
    );

-- Create indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_transaction_reference ON transaction(reference_id);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transaction(transaction_date);
