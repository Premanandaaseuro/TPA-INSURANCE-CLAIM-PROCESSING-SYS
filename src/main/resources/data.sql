-- Initial seed data for local and test profiles
INSERT INTO policies (policy_id, policy_number, customer_name, carrier_name, policy_name, start_date, end_date, sum_insured, status)
VALUES 
('PID-10001', 'POL-10001', 'Rahul Kumar', 'Aseuro Health Insurance', 'Aseuro Health Gold', '2026-01-01', '2026-12-31', 500000.00, 'ACTIVE'),
('PID-10002', 'POL-10002', 'Rohan Verma', 'Aseuro Health Insurance', 'Aseuro Health Silver', '2026-01-01', '2026-12-31', 400000.00, 'INACTIVE'),
('PID-10003', 'POL-10003', 'Test Patient', 'Aseuro Health Insurance', 'Health Basic Plan', '2025-01-01', '2026-03-31', 200000.00, 'INACTIVE'),
('PID-10004', 'POL-10004', 'Priya Sharma', 'Star Health Insurance', 'Comprehensive Care', '2026-01-01', '2026-12-31', 600000.00, 'ACTIVE'),
('PID-10005', 'POL-10005', 'Amit Patel', 'HDFC ERGO General Insurance', 'Optima Restore', '2026-01-01', '2026-12-31', 1000000.00, 'ACTIVE'),
('PID-10006', 'POL-10006', 'Suresh Menon', 'ICICI Lombard Insurance', 'Health Shield', '2026-01-01', '2026-12-31', 350000.00, 'ACTIVE'),
('PID-10007', 'POL-10007', 'Kavita Singh', 'Care Health Insurance', 'Care Advantage', '2026-01-01', '2026-12-31', 750000.00, 'ACTIVE'),
('PID-10008', 'POL-10008', 'Vikram Reddy', 'Niva Bupa Health Insurance', 'ReAssure 2.0', '2026-01-01', '2026-12-31', 500000.00, 'ACTIVE'),
('PID-10009', 'POL-10009', 'Ananya Roy', 'Aseuro Health Insurance', 'Health Secure Plus', '2026-01-01', '2026-12-31', 450000.00, 'ACTIVE'),
('PID-10010', 'POL-10010', 'Deepak Joshi', 'Bajaj Allianz General Insurance', 'Health Guard', '2025-01-01', '2025-12-31', 300000.00, 'INACTIVE');
