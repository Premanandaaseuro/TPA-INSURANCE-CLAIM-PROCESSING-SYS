-- Flyway Migration V1: Initial Schema Creation for TPA Insurance Claim Processing System

-- 1. Policies Table
CREATE TABLE IF NOT EXISTS policies (
    id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(100) UNIQUE,
    policy_number VARCHAR(100) NOT NULL UNIQUE,
    customer_name VARCHAR(255),
    carrier_name VARCHAR(255),
    policy_name VARCHAR(255),
    start_date DATE,
    end_date DATE,
    sum_insured NUMERIC(15, 2),
    max_coverage_amount NUMERIC(15, 2),
    co_pay_percentage NUMERIC(5, 2),
    deductible_amount NUMERIC(15, 2),
    room_rent_capping_per_day NUMERIC(15, 2),
    icu_rent_capping_per_day NUMERIC(15, 2),
    status VARCHAR(50),
    created_at TIMESTAMP
);

-- 2. Claims Table
CREATE TABLE IF NOT EXISTS claims (
    id BIGSERIAL PRIMARY KEY,
    claim_id VARCHAR(100) NOT NULL UNIQUE,
    claim_number VARCHAR(100),
    policy_number VARCHAR(100),
    policy_id VARCHAR(100),
    customer_name VARCHAR(255),
    carrier_name VARCHAR(255),
    policy_name VARCHAR(255),
    patient_name VARCHAR(255),
    hospital_name VARCHAR(255),
    admission_date DATE,
    discharge_date DATE,
    claimed_amount NUMERIC(15, 2),
    claim_type VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    decision_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);

-- Index on claim_id for fast lookup
CREATE INDEX IF NOT EXISTS idx_claim_id ON claims(claim_id);

-- 3. Claim Documents Table
CREATE TABLE IF NOT EXISTS claim_documents (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    checksum_sha256 VARCHAR(64),
    uploaded_at TIMESTAMP NOT NULL
);

-- 4. Discharge Details Table
CREATE TABLE IF NOT EXISTS discharge_details (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    patient_name VARCHAR(255),
    hospital_name VARCHAR(255),
    admission_date DATE,
    discharge_date DATE,
    diagnosis VARCHAR(255),
    treatment_given TEXT,
    treating_doctor VARCHAR(255),
    room_type VARCHAR(100)
);

-- 5. Hospital Bill Details Table
CREATE TABLE IF NOT EXISTS hospital_bill_details (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    bill_number VARCHAR(100),
    bill_date DATE,
    hospital_name VARCHAR(255),
    patient_name VARCHAR(255),
    room_rent_charges NUMERIC(15, 2),
    icu_charges NUMERIC(15, 2),
    doctor_fee NUMERIC(15, 2),
    medicine_charges NUMERIC(15, 2),
    investigation_charges NUMERIC(15, 2),
    total_bill_amount NUMERIC(15, 2)
);

-- 6. Claim Rule Results Table
CREATE TABLE IF NOT EXISTS claim_rule_results (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    rule_code VARCHAR(50) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    passed BOOLEAN NOT NULL,
    severity VARCHAR(50),
    details TEXT,
    evaluated_at TIMESTAMP NOT NULL
);

-- 7. Claim JSON Table
CREATE TABLE IF NOT EXISTS claim_jsons (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    extracted_payload TEXT,
    created_at TIMESTAMP
);

-- 8. Seed Initial Active Policies
INSERT INTO policies (policy_id, policy_number, customer_name, carrier_name, policy_name, start_date, end_date, sum_insured, max_coverage_amount, co_pay_percentage, deductible_amount, room_rent_capping_per_day, icu_rent_capping_per_day, status, created_at)
VALUES 
('PID-10001', 'POL-10001', 'Rahul Kumar', 'Aseuro Health Insurance', 'Health Secure Plus', '2026-01-01', '2026-12-31', 500000.00, 500000.00, 10.00, 5000.00, 5000.00, 10000.00, 'ACTIVE', CURRENT_TIMESTAMP),
('PID-10002', 'POL-10002', 'Priya Sharma', 'Aseuro Health Insurance', 'Family Care', '2026-01-01', '2026-03-31', 300000.00, 300000.00, 5.00, 0.00, 5000.00, 10000.00, 'INACTIVE', CURRENT_TIMESTAMP),
('PID-8899', 'POL-2026-8899', 'Rahul Sharma', 'Star Health Insurance', 'Comprehensive Health Care', '2026-01-01', '2026-12-31', 300000.00, 300000.00, 0.00, 0.00, 3000.00, 6000.00, 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (policy_number) DO NOTHING;
