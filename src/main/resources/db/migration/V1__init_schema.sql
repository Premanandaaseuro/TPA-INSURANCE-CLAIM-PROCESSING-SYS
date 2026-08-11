-- Flyway Migration V1: Initial Schema Creation for TPA Insurance Claim Processing System

-- 1. Policies Table
CREATE TABLE IF NOT EXISTS policies (
    id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(100) UNIQUE,
    policy_number VARCHAR(100) UNIQUE,
    customer_name VARCHAR(255),
    carrier_name VARCHAR(255),
    policy_name VARCHAR(255),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    max_coverage_amount NUMERIC(15, 2),
    co_pay_percentage NUMERIC(5, 2),
    deductible_amount NUMERIC(15, 2),
    room_rent_capping_per_day NUMERIC(15, 2),
    icu_rent_capping_per_day NUMERIC(15, 2),
    created_at TIMESTAMP
);

-- 2. Claims Table
CREATE TABLE IF NOT EXISTS claims (
    id BIGSERIAL PRIMARY KEY,
    claim_id VARCHAR(100) NOT NULL UNIQUE,
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
CREATE TABLE IF NOT EXISTS claim_json (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    raw_extracted_text TEXT,
    extracted_json TEXT,
    created_at TIMESTAMP NOT NULL
);
