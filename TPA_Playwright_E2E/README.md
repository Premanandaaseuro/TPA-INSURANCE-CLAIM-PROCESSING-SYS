# TPA Insurance Claim Processing System - Playwright E2E Test Framework

This isolated test suite provides automated End-to-End (E2E), UI, API, Rule-Engine, and Regression coverage for the TPA Insurance Claim Processing System.

---

## 📁 Framework Directory Structure

```text
TPA_Playwright_E2E/
├── package.json
├── playwright.config.js
├── README.md
├── fixtures/
│   ├── generate_pdfs.js                  # PDF generator script using pdf-lib
│   └── test-data/                        # Realistic PDF test datasets (R01-R10 + Golden)
├── pages/
│   ├── DashboardPage.js                  # Page Object Model for Dashboard
│   ├── SubmitClaimPage.js                 # Page Object Model for Claim Upload Modal
│   ├── ClaimDetailsPage.js               # Page Object Model for Claim Details View
│   ├── RuleAuditPage.js                  # Page Object Model for R01-R10 Audit Table
│   └── PdfExportPage.js                  # Page Object Model for PDF Summary Export
├── tests/
│   ├── e2e/                              # End-to-End Rule Verification Specs
│   │   ├── 01_approved.spec.js
│   │   ├── 02_r01_missing_claim_form.spec.js
│   │   ├── 03_r02_missing_combined.spec.js
│   │   ├── 04_r03_inactive_policy.spec.js
│   │   ├── 05_r04_missing_policy_number.spec.js
│   │   ├── 06_r05_patient_mismatch.spec.js
│   │   ├── 07_r06_hospital_mismatch.spec.js
│   │   ├── 08_r07_date_mismatch.spec.js
│   │   ├── 09_r08_claim_amount.spec.js
│   │   ├── 10_r09_high_claim.spec.js
│   │   └── 11_r10_duplicate.spec.js
│   ├── ui/                               # Frontend Interface Specs
│   │   ├── dashboard.spec.js
│   │   ├── submit_claim.spec.js
│   │   ├── claim_details.spec.js
│   │   ├── navigation.spec.js
│   │   ├── validation.spec.js
│   │   ├── file_upload.spec.js
│   │   ├── pdf_export.spec.js
│   │   └── search_filter.spec.js
│   ├── api/                              # Backend REST API Specs
│   │   ├── claims.api.spec.js
│   │   ├── rules.api.spec.js
│   │   ├── dashboard.api.spec.js
│   │   └── pdf.api.spec.js
│   └── regression/                       # Regression & Safety Specs
│       ├── golden_claim.spec.js
│       └── negative_regression.spec.js
└── utils/
    ├── api_client.js                     # Playwright REST API Client
    └── db_validator.js                   # State & Rule Results Verifier
```

---

## 🎯 R01-R10 Test Mapping Matrix

| Rule | Description | Trigger Condition | Expected Result | Test Spec Location |
| :--- | :--- | :--- | :--- | :--- |
| **R01** | Claim Form Missing | Claim Form PDF not uploaded | `REJECTED` | `tests/e2e/02_r01_missing_claim_form.spec.js` |
| **R02** | Combined Document Missing | Combined Hospital PDF not uploaded | `REJECTED` | `tests/e2e/03_r02_missing_combined.spec.js` |
| **R03** | Policy Inactive | Policy in DB is INACTIVE | `REJECTED` | `tests/e2e/04_r03_inactive_policy.spec.js` |
| **R04** | Policy Number Missing | Claim Form PDF lacks Policy Number | `NEEDS_MANUAL_REVIEW` | `tests/e2e/05_r04_missing_policy_number.spec.js` |
| **R05** | Patient Name Mismatch | Patient name differs across docs | `NEEDS_MANUAL_REVIEW` | `tests/e2e/06_r05_patient_mismatch.spec.js` |
| **R06** | Hospital Name Mismatch | Hospital name differs across docs | `NEEDS_MANUAL_REVIEW` | `tests/e2e/07_r06_hospital_mismatch.spec.js` |
| **R07** | Date Mismatch | Admission/discharge dates differ | `NEEDS_MANUAL_REVIEW` | `tests/e2e/08_r07_date_mismatch.spec.js` |
| **R08** | Claim > Bill Amount | Claimed amount > Total Bill amount | `NEEDS_MANUAL_REVIEW` | `tests/e2e/09_r08_claim_amount.spec.js` |
| **R09** | High Claim Amount | Claimed amount > ₹50,000 | `NEEDS_MANUAL_REVIEW` | `tests/e2e/10_r09_high_claim.spec.js` |
| **R10** | Possible Duplicate | Duplicate (Policy + Patient + Hospital + Date) | `NEEDS_MANUAL_REVIEW` | `tests/e2e/11_r10_duplicate.spec.js` |

---

## 🛠️ Installation & Setup

1. Navigate to `TPA_Playwright_E2E` directory:
   ```bash
   cd TPA_Playwright_E2E
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Install Playwright browser binaries:
   ```bash
   npx playwright install --with-deps chromium
   ```

4. Generate PDF Test Fixtures:
   ```bash
   npm run generate-fixtures
   ```

---

## 🚀 Running Test Suites

- **Run All Tests**:
  ```bash
  npm test
  ```

- **Run Tests in Headed Mode**:
  ```bash
  npm run headed
  ```

- **Run Business Rules Suite (R01 - R10)**:
  ```bash
  npm run rules
  ```

- **Run Golden Valid Claim Spec**:
  ```bash
  npm run golden
  ```

- **Run Frontend UI Specs**:
  ```bash
  npm run ui
  ```

- **Run Backend API Specs**:
  ```bash
  npm run api
  ```

- **Run Regression Suite**:
  ```bash
  npm run regression
  ```

- **View HTML Test Report**:
  ```bash
  npm run report
  ```

---

## 📊 Reports & Artifacts

All Playwright reports and artifacts are saved inside the `TPA_Playwright_E2E` directory:
- HTML Report: `TPA_Playwright_E2E/reports/html/index.html`
- Traces & Screenshots (on failure): `TPA_Playwright_E2E/reports/artifacts/`
