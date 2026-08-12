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

-- 3. Seed/Ensure Policy Master Records (POL-10001, POL-10002, POL-10003)
INSERT INTO policies (
    policy_id, policy_number, customer_name, carrier_name, policy_name,
    start_date, end_date, sum_insured, max_coverage_amount, co_pay_percentage,
    deductible_amount, room_rent_capping_per_day, icu_rent_capping_per_day, status, created_at
) VALUES 
('PID-10001', 'POL-10001', 'Rahul Kumar', 'Aseuro Health Insurance', 'Aseuro Health Gold', 
 '2026-01-01', '2026-12-31', 500000.00, 500000.00, 10.00, 5000.00, 5000.00, 10000.00, 'ACTIVE', NOW()),

('PID-10002', 'POL-10002', 'Rohan Verma', 'Aseuro Health Insurance', 'Aseuro Health Silver', 
 '2026-01-01', '2026-12-31', 400000.00, 400000.00, 5.00, 0.00, 5000.00, 10000.00, 'ACTIVE', NOW()),

('PID-10003', 'POL-10003', 'Test Patient', 'Aseuro Health Insurance', 'Health Basic Plan', 
 '2025-01-01', '2026-03-31', 200000.00, 200000.00, 10.00, 0.00, 3000.00, 6000.00, 'INACTIVE', NOW())

ON CONFLICT (policy_number) DO UPDATE SET 
    customer_name = EXCLUDED.customer_name,
    status = EXCLUDED.status,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date;

COMMIT;
