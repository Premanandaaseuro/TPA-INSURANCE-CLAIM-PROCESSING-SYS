-- TPA Insurance Claim Processing System - Database Reset Script
-- Safely truncates claim data, resets sequences, preserves schema & Flyway history, and ensures policy master data.

BEGIN;

-- 1. Safely truncate all claim-related tables without destroying Flyway history
TRUNCATE TABLE 
    claim_rule_results,
    claim_jsons,
    claim_documents,
    discharge_details,
    hospital_bill_details,
    claims
RESTART IDENTITY CASCADE;

-- 2. Reset Claim ID sequence if exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'claim_id_seq') THEN
        ALTER SEQUENCE claim_id_seq RESTART WITH 1;
    ELSIF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'claims_id_seq') THEN
        ALTER SEQUENCE claims_id_seq RESTART WITH 1;
    END IF;
END $$;

-- 3. Seed/Ensure Policy Master Records (POL-10001 to POL-10010)
INSERT INTO policies (
    policy_id, policy_number, customer_name, carrier_name, policy_name,
    start_date, end_date, sum_insured, max_coverage_amount, co_pay_percentage,
    deductible_amount, room_rent_capping_per_day, icu_rent_capping_per_day, status, created_at
) VALUES 
('PID-10001', 'POL-10001', 'Rahul Kumar', 'Aseuro Health Insurance', 'Aseuro Health Gold', '2026-01-01', '2026-12-31', 500000.00, 500000.00, 10.00, 5000.00, 5000.00, 10000.00, 'ACTIVE', NOW()),
('PID-10002', 'POL-10002', 'Rohan Verma', 'Aseuro Health Insurance', 'Aseuro Health Silver', '2026-01-01', '2026-12-31', 400000.00, 400000.00, 5.00, 0.00, 5000.00, 10000.00, 'ACTIVE', NOW()),
('PID-10003', 'POL-10003', 'Test Patient', 'Aseuro Health Insurance', 'Health Basic Plan', '2025-01-01', '2026-03-31', 200000.00, 200000.00, 10.00, 0.00, 3000.00, 6000.00, 'INACTIVE', NOW()),
('PID-10004', 'POL-10004', 'Priya Sharma', 'Star Health Insurance', 'Comprehensive Care', '2026-01-01', '2026-12-31', 600000.00, 600000.00, 0.00, 0.00, 6000.00, 12000.00, 'ACTIVE', NOW()),
('PID-10005', 'POL-10005', 'Amit Patel', 'HDFC ERGO General Insurance', 'Optima Restore', '2026-01-01', '2026-12-31', 1000000.00, 1000000.00, 5.00, 0.00, 8000.00, 15000.00, 'ACTIVE', NOW()),
('PID-10006', 'POL-10006', 'Suresh Menon', 'ICICI Lombard Insurance', 'Health Shield', '2026-01-01', '2026-12-31', 350000.00, 350000.00, 10.00, 2000.00, 4000.00, 8000.00, 'ACTIVE', NOW()),
('PID-10007', 'POL-10007', 'Kavita Singh', 'Care Health Insurance', 'Care Advantage', '2026-01-01', '2026-12-31', 750000.00, 750000.00, 0.00, 0.00, 7000.00, 14000.00, 'ACTIVE', NOW()),
('PID-10008', 'POL-10008', 'Vikram Reddy', 'Niva Bupa Health Insurance', 'ReAssure 2.0', '2026-01-01', '2026-12-31', 500000.00, 500000.00, 0.00, 0.00, 5000.00, 10000.00, 'ACTIVE', NOW()),
('PID-10009', 'POL-10009', 'Ananya Roy', 'Aseuro Health Insurance', 'Health Secure Plus', '2026-01-01', '2026-12-31', 450000.00, 450000.00, 5.00, 0.00, 5000.00, 10000.00, 'ACTIVE', NOW()),
('PID-10010', 'POL-10010', 'Deepak Joshi', 'Bajaj Allianz General Insurance', 'Health Guard', '2025-01-01', '2025-12-31', 300000.00, 300000.00, 10.00, 0.00, 3000.00, 6000.00, 'INACTIVE', NOW())

ON CONFLICT (policy_number) DO UPDATE SET 
    customer_name = EXCLUDED.customer_name,
    status = EXCLUDED.status,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date;

COMMIT;
