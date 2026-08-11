-- Flyway Migration V2: Seed Additional Test Policies (Active and Inactive)

INSERT INTO policies (
    policy_id, policy_number, customer_name, carrier_name, policy_name,
    start_date, end_date, sum_insured, max_coverage_amount, co_pay_percentage,
    deductible_amount, room_rent_capping_per_day, icu_rent_capping_per_day, status, created_at
) VALUES 
-- POL-10002: Inactive Policy (Priya Sharma) required for Test 2 (Policy Inactive Rejection)
('PID-10002', 'POL-10002', 'Priya Sharma', 'Aseuro Health Insurance', 'Family Care', 
 '2025-01-01', '2025-12-31', 300000.00, 300000.00, 10.00, 0.00, 4000.00, 8000.00, 'INACTIVE', CURRENT_TIMESTAMP),

-- POL-10009: Active Policy (Rohan Verma) required for Test Cases
('PID-10009', 'POL-10009', 'Rohan Verma', 'Aseuro Health Insurance', 'Health Secure Plus', 
 '2026-01-01', '2026-12-31', 400000.00, 400000.00, 5.00, 0.00, 5000.00, 10000.00, 'ACTIVE', CURRENT_TIMESTAMP)

ON CONFLICT (policy_number) DO NOTHING;
