# PROJECT PLAN: TPA Insurance Claim Processing System

**Target Submission Date:** Friday  
**Architecture Pattern:** Modular Monolith  
**Primary Goal:** Automated extraction, validation, rule evaluation, and decision making for Third-Party Administrator (TPA) health insurance claims.

---

## 1. Project Objective

The **TPA Insurance Claim Processing System** is designed to streamline and automate the adjudication of health insurance claims submitted by policyholders or hospitals. The system ingests exactly two PDF documents per claim submission:
1. **Claim Form**
2. **Combined Document** (containing both Discharge Summary and Final Hospital Bill)

The application extracts raw text via Apache PDFBox / Tess4J OCR, parses structured claim attributes, verifies policy and bill data against business rules via a rule engine, calculates an automated decision (`APPROVED`, `REJECTED`, or `NEEDS_MANUAL_REVIEW`), and provides a modern, clean web portal for claim review, decision auditing, and PDF summary exports.

---

## 2. Functional Requirements

### 2.1 Claim Ingestion & Storage
- **Two-File Upload Enforcement:** Web UI and REST API strictly require exactly 2 PDF files (Claim Form + Combined Document).
- **Claim ID Generation:** Auto-generate unique Claim IDs formatted as `CLM-YYYYMMDD-XXXX` upon upload.
- **Document Storage:** Persist raw uploaded PDFs in disk-backed binary storage (`/storage/claims/{claimId}/`) with file checksums (SHA-256) and record metadata in PostgreSQL.

### 2.2 Text Extraction & Structuring
- **PDF Text Extraction:** Extract text layers from uploaded PDFs using Apache PDFBox.
- **OCR Fallback:** Fallback to Tess4J OCR if a PDF page lacks digital text layers (scanned documents).
- **Structured Data Parsing:** Regex and NLP pattern matching to extract key parameters:
  - Policy Number, Patient Name, Hospital Name, Admission Date, Discharge Date
  - Claimed Amount, Total Bill Amount, Diagnosis / Line-item breakdown
  - Claim Submission Date

### 2.3 Rule Engine & Decision Engine
- Evaluate extracted data against 10 deterministic business rules (**R01** to **R10**).
- **Decision Hierarchy:** `REJECTED` > `NEEDS_MANUAL_REVIEW` > `APPROVED`
  - If any rule triggers a `REJECTED` status, the claim is finalized as `REJECTED`.
  - If no `REJECTED` rules fire but one or more `NEEDS_MANUAL_REVIEW` rules fire, the claim becomes `NEEDS_MANUAL_REVIEW`.
  - If zero rules trigger, the claim is auto-`APPROVED`.
- Record detailed rule evaluation logs (Rule Code, Pass/Fail Flag, Description, Trigger Reason) for auditability.

### 2.4 User Interface (UI) & Management
- **Dashboard:** Overview of key claim processing metrics (Total Claims, Approval Rate, Manual Review Queue, Rejection Rate) and search/filter list.
- **Claim Submission Wizard:** Streamlined, drag-and-drop 2-file uploader with real-time file validation.
- **Claim Details View:** Deep-dive audit view showing extracted structured fields side-by-side with original document previews, detailed rule execution log, and manual override capabilities.
- **PDF Export:** Export claim summary reports with adjudication breakdown as downloadable PDFs.

---

## 3. Business Rules Specification (R01 - R10)

| Rule ID | Rule Name | Description / Logic | Severity / Output Status |
|---|---|---|---|
| **R01** | Claim Form Missing | Claim Form PDF not attached or empty | `REJECTED` |
| **R02** | Combined Document Missing | Combined Discharge Summary + Bill PDF missing or empty | `REJECTED` |
| **R03** | Policy Inactive on Admission | Policy end date < Admission date OR status != ACTIVE | `REJECTED` |
| **R04** | Policy Number Missing | Extracted Policy Number is blank / unparseable | `NEEDS_MANUAL_REVIEW` |
| **R05** | Patient Name Mismatch | Claim Form patient name != Hospital Bill patient name | `NEEDS_MANUAL_REVIEW` |
| **R06** | Hospital Name Mismatch | Claim Form hospital name != Hospital Bill hospital name | `NEEDS_MANUAL_REVIEW` |
| **R07** | Admission/Discharge Date Mismatch | Dates on Claim Form mismatch Discharge Summary / Bill | `NEEDS_MANUAL_REVIEW` |
| **R08** | Claimed Amount > Total Bill | Claimed amount exceeds total bill amount | `NEEDS_MANUAL_REVIEW` |
| **R09** | High Value Claim | Claimed amount > ₹50,000 | `NEEDS_MANUAL_REVIEW` |
| **R10** | Possible Duplicate Claim | Active claim exists for same Policy, Patient & Admission Date | `NEEDS_MANUAL_REVIEW` |

---

## 4. Implementation Phases & Schedule

```
               [ PHASE 1 ] Backend Core & Database Setup
                                   │
                                   ▼
               [ PHASE 2 ] Extraction Engine (PDFBox & Regex)
                                   │
                                   ▼
               [ PHASE 3 ] Rule & Decision Engines (R01-R10)
                                   │
                                   ▼
               [ PHASE 4 ] Modern React Frontend & API Wireup
                                   │
                                   ▼
               [ PHASE 5 ] PDF Report Export & End-to-End Testing
                                   │
                                   ▼
               [ PHASE 6 ] Submission Verification & Polish
```

---

## 5. Development Order

1. **Phase 1: Database & Backend Architecture Initialization**
   - Setup PostgreSQL database schema (`claims`, `documents`, `extracted_data`, `rule_results`, `policies`).
   - Create Spring Boot 3.5 project with Modular Monolith structure (`ingestion`, `extraction`, `rules`, `decision`, `analytics`, `export`).
   - Configure Spring Data JPA entities, repositories, and Liquibase/Flyway or JPA DDL auto-generation.

2. **Phase 2: Ingestion & Text Extraction Core**
   - Build `/api/v1/claims/upload` endpoint handling multi-part file upload for 2 files.
   - Implement `PdfExtractionService` using Apache PDFBox to extract raw text.
   - Implement `StructuredDataParserService` using robust Regex pattern extractors for fields (Policy No, Patient Name, Hospital Name, Dates, Amounts).
   - Integrate Tess4J OCR fallback for scanned images inside PDFs.

3. **Phase 3: Business Rule Engine & Decision Processor**
   - Implement Rule Engine interface and individual handlers for `R01` through `R10`.
   - Implement `DecisionEngineService` applying priority resolution (`REJECTED` > `NEEDS_MANUAL_REVIEW` > `APPROVED`).
   - Create mock policy verification service (storing active policy database records for lookup).

4. **Phase 4: Modern React Frontend Application**
   - Setup Vite + React + TypeScript + Tailwind CSS project.
   - Create clean, professional components:
     - Navigation Bar & Layout
     - Ingestion Wizard (2-file drag and drop)
     - Claims Dashboard Table (Status Badges, Search, Filter)
     - Claim Detail View with Rule Execution Timeline & Field Match visualizer
     - Manual Review Action Modal

5. **Phase 5: PDF Export & End-to-End Wireup**
   - Implement PDF adjudication report generator using Apache PDFBox in backend.
   - Connect Frontend to Backend REST endpoints.
   - Seed realistic sample test datasets (Mock PDFs & Sample Claims).

6. **Phase 6: Verification & Final Polish**
   - Verify all 10 rules against edge cases.
   - Check UI responsiveness and modern minimal aesthetic.
   - Package executable JAR & prepare run scripts for submission.

---

## 6. Testing Strategy

- **Unit Tests:** JUnit 5 & AssertJ tests for each Rule Handler (`R01`-`R10`) and Decision Engine logic.
- **Extraction Tests:** Integration tests verifying regex parsing on sample PDF texts.
- **API Tests:** `@SpringBootTest` & MockMvc tests for file upload and claim query APIs.
- **End-to-End Workflow Verification:** End-to-end processing verification using real test PDF sets covering all three outcome statuses (`APPROVED`, `REJECTED`, `NEEDS_MANUAL_REVIEW`).

---

## 7. Friday Submission Checklist

- [ ] PostgreSQL Database scripts / schema migrations ready and auto-executed on launch.
- [ ] Backend Spring Boot application builds cleanly (`mvn clean package`).
- [ ] Frontend React application builds cleanly (`npm run build`).
- [ ] 2-Document Upload flow validated end-to-end.
- [ ] All 10 Rules (`R01`-`R10`) verified with explicit log traces.
- [ ] Decision Priority (`REJECTED` > `NEEDS_MANUAL_REVIEW` > `APPROVED`) strictly satisfied.
- [ ] PDF Adjudication Summary Export operational.
- [ ] README.md with quick-start instructions (`run.sh` / `run.bat`).
