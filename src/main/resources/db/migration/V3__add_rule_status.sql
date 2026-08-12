-- Flyway Migration V3: Add status column to claim_rule_results (PASS, FAIL, NOT_EVALUATED)

ALTER TABLE claim_rule_results ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PASS';

UPDATE claim_rule_results 
SET status = CASE 
    WHEN passed = true THEN 'PASS' 
    ELSE 'FAIL' 
END 
WHERE status IS NULL;
