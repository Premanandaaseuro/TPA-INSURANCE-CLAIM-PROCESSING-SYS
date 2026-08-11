# Project Plan - TPA Insurance Claim Processing System

## Project Objective
Build a production-grade, modular monolith TPA (Third-Party Administrator) Health Insurance Claim Processing System.

---

## ⚡ Infrastructure Port Specification (NO 3000 / NO 8080)

To prevent port conflicts with local services (e.g. Jenkins on 8080 or dev apps on 3000), this system strictly operates on dedicated ports:

- **Frontend SPA Port:** `7001`
- **Backend Service Port:** `7002` (`server.port=7002`)
- **PostgreSQL Host Port:** `7003` (`7003:5432`)
- **PostgreSQL Docker Internal Port:** `5432` (`postgres:5432`)

---

## Functional Requirements
1. Ingest exactly 2 mandatory claim PDF documents (Claim Form + Combined Discharge/Bill).
2. Store files securely in `storage/claims/{claimId}/` with path traversal defense & SHA-256 calculation.
3. Extract text via Apache PDFBox with Tess4J OCR fallback.
4. Parse structured fields into PostgreSQL entities and raw JSON storage (`ClaimJson`).
5. Evaluate 10 business rules (`R01` to `R10`) and persist detailed audit results.
6. Apply decision engine priority hierarchy: $\text{REJECTED} > \text{NEEDS\_MANUAL\_REVIEW} > \text{APPROVED}$.
7. Render React SPA Dashboard on port `7001` connecting to REST API on port `7002`.
8. Download PDF summary report generated via Apache PDFBox (`GET /api/claims/{claimId}/pdf`).

---

## Development Phases & Friday Submission Checklist
- [x] **Phase 1:** Backend Modular Monolith Foundation & PostgreSQL setup
- [x] **Phase 2:** PDF text extraction, OCR fallback & structured data parser
- [x] **Phase 3:** Rule Engine audit (`R01`-`R10`) & Decision Priority Matrix
- [x] **Phase 4:** React SPA Frontend Integration Audit & PDF Summary Export
- [x] **Phase 5:** Dedicated Port Migration (7001 / 7002 / 7003) & Docker Compose
- [x] **Phase 6:** Clean Git commit history & Friday Submission Package
