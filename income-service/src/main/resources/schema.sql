-- Income table
CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    reason VARCHAR(255) NOT NULL,
    income_date DATE NOT NULL,
    quantity INT NOT NULL,
    amount_received DECIMAL(19,2) NOT NULL,
    expected_amount DECIMAL(19,2) NOT NULL,
    due_balance DECIMAL(19,2) NOT NULL,
    receipt VARCHAR(255),
    created_by VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,

    CONSTRAINT chk_income_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'RETRYING'))
    );

-- Processed data income table
CREATE TABLE IF NOT EXISTS processed_data_income (
    id BIGSERIAL PRIMARY KEY,
    income_id BIGINT NOT NULL REFERENCES income(id),
    processed_amount DECIMAL(19,2) NOT NULL,
    processed_at DATE NOT NULL,
    processed_by VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    attempts INT NOT NULL DEFAULT 1
    );

-- Transaction table
CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    created_by VARCHAR(100) NOT NULL,

    CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME', 'EXPENSE', 'INVESTMENT'))
    );

-- Indexes for optimized queries
CREATE INDEX IF NOT EXISTS idx_income_status ON income(status);
CREATE INDEX IF NOT EXISTS idx_income_date ON income(income_date);
CREATE INDEX IF NOT EXISTS idx_processed_data_income_id ON processed_data_income(income_id);
CREATE INDEX IF NOT EXISTS idx_transaction_reference ON transaction(reference_id);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transaction(transaction_date);