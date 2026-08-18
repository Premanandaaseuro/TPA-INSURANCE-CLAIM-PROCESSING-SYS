TPA RULE TEST PDF PACK — GENERATED FROM THE PROVIDED POLICIES SQL

IMPORTANT:
The supplied SQL defines policy master data only. It does NOT create claims or duplicate claim history.
Therefore:
- R10 cannot trigger in a fresh DB from the policy INSERT alone. A prior claim with the same
  policy + patient + hospital + admission date must exist.
- R02 is a missing-document rule. To test it, the application must receive only the Claim Form.
  It cannot be tested by uploading two valid documents.
- R03 requires the admission date to fall outside the policy validity period. Case 02 uses
  admission 2026-04-10 while POL-10002 ends 2026-03-31.
- R04 is tested by omitting Policy Number from the Claim Form even though the policy master row exists.

Expected isolated rule cases:
01 Approved
02 R03 Policy inactive
03 R05 Patient mismatch
04 R06 Hospital mismatch
05 R07 Date mismatch
06 R08 Claimed > Bill
07 R09 High claim
08 R04 Missing policy number
09 R02 Missing combined document
10 R10 Duplicate (requires prior matching claim)

For R05/R06/R07/R08/R09, all other values are intentionally kept valid so the intended
rule is the primary failure.

Case 10 note:
The provided SQL comment says CASE 10 is a duplicate of CASE 01, but CASE 10 uses POL-10010
while CASE 01 uses POL-10001. Under the stated duplicate rule, policy number is part of the
duplicate key, so these two records are NOT duplicates. To make R10 deterministic, create
two claims using POL-10010 + Rahul Kumar + Manipal Hospital Bengaluru + 2026-04-10, or
change the CASE 10 policy number to POL-10001.
