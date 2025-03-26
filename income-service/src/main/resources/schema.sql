-- Drop existing tables if they exist (only for development)
DROP TABLE IF EXISTS processed_data_income;
DROP TABLE IF EXISTS income;

-- Create income table with all required columns
CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    reason VARCHAR(255) NOT NULL,
    income_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    amount_received DECIMAL(19,2) NOT NULL,
    expected_amount DECIMAL(19,2) NOT NULL,
    due_balance DECIMAL(19,2) DEFAULT 0,
    receipt VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    status VARCHAR(50),
    error_message TEXT
    );

CREATE TABLE IF NOT EXISTS processed_data_income (
    id BIGSERIAL PRIMARY KEY,
    income_id BIGINT REFERENCES income(id),
    processed_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    processed_at DATE NOT NULL,
    processed_by VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    attempts INTEGER NOT NULL DEFAULT 1
    );

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_income_reason_date ON income(reason, income_date);
CREATE INDEX IF NOT EXISTS idx_processed_data_income_id ON processed_data_income(income_id);