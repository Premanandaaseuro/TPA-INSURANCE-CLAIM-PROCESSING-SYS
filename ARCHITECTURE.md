# ARCHITECTURE SPECIFICATION: TPA Insurance Claim Processing System

**Architecture Pattern:** Modular Monolith  
**Frontend Stack:** React, Vite, TypeScript, Tailwind CSS, Lucide React  
**Backend Stack:** Java 21, Spring Boot 3.5.x, Spring Web, Spring Data JPA, Hibernate, Jackson, Bean Validation  
**Database:** PostgreSQL 17  
**Document Engine:** Apache PDFBox (Native extraction & PDF generation) + Tess4J / Tesseract OCR (Scanned fallback)

---

## 1. High-Level Architecture

The system is structured as a **Modular Monolith**. It runs as a single deployable backend artifact alongside a decoupled React Single Page Application (SPA). The backend is internally divided into strictly bounded domains (modules) with clean internal interfaces, ensuring high maintainability without microservice deployment overhead.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           REACT SPA (FRONTEND)                                  │
│  ┌──────────────────┬─────────────────┬───────────────────┬──────────────────┐  │
│  │ Claim Submission │ Claims Dashboard│ Audit Detail View │ PDF Report Export│  │
│  └─────────┬────────┴────────┬────────┴─────────┬─────────┴─────────┬────────┘  │
└────────────┼─────────────────┼──────────────────┼───────────────────┼───────────┘
             │ JSON            │ JSON             │ JSON              │ PDF Stream
             ▼                 ▼                  ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                       SPRING BOOT BACKEND (REST API)                            │
│                                                                                 │
│  ┌───────────────────────────── MODULAR MONOLITH ────────────────────────────┐  │
│  │                                                                           │  │
│  │   ┌─────────────────────┐             ┌───────────────────────────────┐   │  │
│  │   │  Ingestion Module   │────────────>│      Extraction Module        │   │  │
│  │   │ - 2-File Validator  │             │ - Apache PDFBox Text Extractor│   │  │
│  │   │ - Storage Engine    │             │ - Tess4J OCR Fallback         │   │  │
│  │   └─────────────────────┘             │ - Structured Field Parser     │   │  │
│  │                                       └───────────────┬───────────────┘   │  │
│  │                                                       │ Extracted Data    │  │
│  │                                                       ▼                   │  │
│  │   ┌─────────────────────┐             ┌───────────────────────────────┐   │  │
│  │   │   Decision Module   │<────────────│         Rule Engine           │   │  │
│  │   │ - Priority Resolver │  Rule Flags │ - R01..R10 Business Handlers  │   │  │
│  │   │   (REJ > REV > APP) │             │ - Policy Matching Service     │   │  │
│  │   └──────────┬──────────┘             └───────────────────────────────┘   │  │
│  │              │ Final Status                                               │  │
│  │              ▼                                                            │  │
│  │   ┌─────────────────────┐             ┌───────────────────────────────┐   │  │
│  │   │   Export Module     │             │    Policy & Audit Domain     │   │  │
│  │   │ - PDF Summary Gen   │             │ - Active Policy Directory     │   │  │
│  │   └─────────────────────┘             └───────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │ JDBC / JPA
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           POSTGRESQL DATABASE                                   │
│  ┌───────────────┬───────────────────┬───────────────────┬───────────────────┐  │
│  │ claims        │ claim_documents   │ extracted_data    │ rule_results      │  │
│  └───────────────┴───────────────────┴───────────────────┴───────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

| Component / Module | Architectural Role & Responsibilities |
|---|---|
| **Frontend SPA** | React 18+ app handling user workflows: upload forms, live status updates, search/filters, side-by-side audit view, and trigger PDF downloads. |
| **Ingestion Engine** | Validates uploaded payload (exactly 2 PDF files), computes file SHA-256 hashes, generates `CLM-YYYYMMDD-XXXX`, stores files on storage path. |
| **Document Extraction Engine** | Leverages Apache PDFBox to read textual streams. Executes Tess4J OCR if page contains non-selectable image streams. Runs Regex pipelines to produce `ExtractedClaimData` DTO. |
| **Policy Registry Domain** | Provides lookup for active insurance policies, verifying policy status, coverage limits, and policyholder details. |
| **Rule Engine** | Executes 10 independent rules (`R01` through `R10`). Each rule inspects `ExtractedClaimData` + `Policy` data and produces a `RuleEvaluationResult`. |
| **Decision Engine** | Gathers all `RuleEvaluationResult` objects, applies priority algorithm (`REJECTED` > `NEEDS_MANUAL_REVIEW` > `APPROVED`), and stores final status. |
| **PDF Summary Generator** | Synthesizes claim decisions, extracted values, and rule evaluation audit logs into a formatted PDF document using PDFBox. |

---

## 3. Frontend Architecture

### Technology Stack
- **Framework:** React 18 + Vite (TypeScript)
- **Styling:** Tailwind CSS (Modern, dark/light theme, clean slate/indigo palette)
- **Icons:** Lucide React
- **State Management:** React Context API + TanStack Query / custom custom fetch hooks.

### Component Tree & Layout Structure
```
src/
├── components/
│   ├── common/
│   │   ├── Header.tsx
│   │   ├── StatusBadge.tsx
│   │   ├── FileUploader.tsx
│   │   └── MetricCard.tsx
│   ├── dashboard/
│   │   ├── ClaimFilterBar.tsx
│   │   └── ClaimsTable.tsx
│   ├── claim-detail/
│   │   ├── DocumentViewer.tsx
│   │   ├── ExtractedFieldsList.tsx
│   │   ├── RuleAuditTimeline.tsx
│   │   └── DecisionBanner.tsx
│   └── upload/
│       └── UploadWizard.tsx
├── services/
│   └── api.ts
├── types/
│   └── claim.ts
└── App.tsx
```

---

## 4. Backend Architecture (Modular Monolith)

### Spring Boot Package Layout
```
com.tpa.claimprocessor/
├── common/
│   ├── config/
│   ├── exception/
│   └── dto/
├── ingestion/
│   ├── controller/ClaimIngestionController.java
│   ├── service/ClaimIngestionService.java
│   └── model/ClaimDocument.java
├── extraction/
│   ├── service/PdfExtractionService.java
│   ├── service/OcrFallbackService.java
│   ├── service/StructuredDataParser.java
│   └── model/ExtractedClaimData.java
├── rules/
│   ├── handler/RuleHandler.java
│   ├── handler/impl/R01_ClaimFormMissingRule.java
│   ├── handler/impl/R02_CombinedDocMissingRule.java
│   ├── ... (R03 - R10 Handlers)
│   └── service/RuleEngineService.java
├── decision/
│   ├── service/DecisionEngineService.java
│   └── model/ClaimDecision.java
├── domain/
│   ├── entity/Claim.java
│   ├── entity/ClaimRuleResult.java
│   ├── entity/Policy.java
│   └── repository/ClaimRepository.java
└── export/
    └── service/PdfExportService.java
```

---

## 5. Database Architecture (PostgreSQL 17 Schema)

### Entity-Relationship Diagram (Logical)

```
┌───────────────────────────┐          ┌───────────────────────────┐
│          policies         │          │          claims           │
├───────────────────────────┤          ├───────────────────────────┤
│ PK policy_number (VARCHAR)│◄─────────│ PK claim_id (VARCHAR)     │
│    patient_name (VARCHAR) │          │    policy_number (FK)     │
│    start_date (DATE)      │          │    patient_name (VARCHAR) │
│    end_date (DATE)        │          │    hospital_name(VARCHAR) │
│    status (ACTIVE/INACT)  │          │    admission_date (DATE)  │
└───────────────────────────┘          │    discharge_date (DATE)  │
                                       │    claimed_amount(NUMERIC)│
                                       │    status (VARCHAR)       │
                                       │    created_at (TIMESTAMP) │
                                       └─────────────┬─────────────┘
                                                     │ 1
                                       ┌─────────────┴─────────────┐
                                       │                           │ N
                                       ▼                           ▼
                        ┌───────────────────────────┐ ┌───────────────────────────┐
                        │      claim_documents      │ │       rule_results        │
                        ├───────────────────────────┤ ├───────────────────────────┤
                        │ PK document_id (UUID)     │ │ PK result_id (UUID)       │
                        │ FK claim_id (VARCHAR)     │ │ FK claim_id (VARCHAR)     │
                        │    doc_type (FORM/COMBINED│ │    rule_code (VARCHAR)    │
                        │    file_path (VARCHAR)    │ │    rule_name (VARCHAR)    │
                        │    checksum_sha256 (TEXT) │ │    passed (BOOLEAN)       │
                        └───────────────────────────┘ │    severity (VARCHAR)     │
                                                      │    details (TEXT)         │
                                                      └───────────────────────────┘
```

---

## 6. OCR & Text Extraction Flow

```
   [ Uploaded PDF File ]
             │
             ▼
   Is Digital Text Present? (Apache PDFBox PDFTextStripper)
       ├─── YES ──> Extract Raw Plaintext Stream
       │
       └─── NO ───> Rasterize PDF Pages to BufferedImage ──> Execute Tess4J OCR
                                                                    │
                                 ┌──────────────────────────────────┘
                                 ▼
                     [ Combined Raw Plaintext ]
                                 │
                                 ▼
                     [ Regex & Pattern Matcher ]
             ├── Policy Number:  (?i)Policy\s*(?:No|Number|#)?[\s:]*([A-Z0-9\-]+)
             ├── Patient Name:   (?i)Patient\s*Name[\s:]*([A-Za-z\s\.]+)
             ├── Hospital Name:  (?i)Hospital\s*Name[\s:]*([A-Za-z0-9\s\,\.\-]+)
             ├── Admission Date: (?i)Admission\s*Date[\s:]*(\d{2}[\/\-]\d{2}[\/\-]\d{4})
             ├── Discharge Date: (?i)Discharge\s*Date[\s:]*(\d{2}[\/\-]\d{2}[\/\-]\d{4})
             └── Amounts:        (?i)(?:Claimed|Total)\s*Amount[\s:]*₹?\s*([\d\,\.]+)
                                 │
                                 ▼
                     [ ExtractedClaimData DTO ]
```

---

## 7. Rule Engine & Decision Engine Flow

### 10 Business Rules Execution Pipeline

```
  [ ExtractedClaimData DTO ] + [ Verified Policy Record ]
                             │
                             ▼
  ┌───────────────────────────────────────────────────────────┐
  │                 Rule Engine Evaluator                     │
  │                                                           │
  │  R01: Is Claim Form PDF present?              ──> PASS/REJ│
  │  R02: Is Combined Document PDF present?       ──> PASS/REJ│
  │  R03: Is Policy active on admission date?     ──> PASS/REJ│
  │  R04: Is Policy Number non-blank?             ──> PASS/REV│
  │  R05: Does Patient Name match Policy & Bill?  ──> PASS/REV│
  │  R06: Does Hospital Name match Form & Bill?   ──> PASS/REV│
  │  R07: Do Admission/Discharge Dates match?     ──> PASS/REV│
  │  R08: Is Claimed Amount <= Total Bill Amount? ──> PASS/REV│
  │  R09: Is Claimed Amount <= ₹50,000?           ──> PASS/REV│
  │  R10: Is Claim non-duplicate (no active dup)? ──> PASS/REV│
  └──────────────────────────┬────────────────────────────────┘
                             │ List<RuleEvaluationResult>
                             ▼
  ┌───────────────────────────────────────────────────────────┐
  │                 Decision Engine Matrix                    │
  │                                                           │
  │   1. Any Rule evaluated to REJECTED?                      │
  │      └── YES ──> Final Decision = REJECTED                │
  │      └── NO  ──> Proceed to Check 2                       │
  │                                                           │
  │   2. Any Rule evaluated to NEEDS_MANUAL_REVIEW?           │
  │      └── YES ──> Final Decision = NEEDS_MANUAL_REVIEW     │
  │      └── NO  ──> Proceed to Check 3                       │
  │                                                           │
  │   3. All 10 Rules PASSED clean                            │
  │      └── YES ──> Final Decision = APPROVED                │
  └──────────────────────────┬────────────────────────────────┘
                             │
                             ▼
                 [ Save Claim to PostgreSQL ]
```

---

## 8. Complete End-to-End Claim Processing Workflow

1. **User Action:** User navigates to Claim Upload page in React SPA and attaches:
   - `Claim_Form.pdf`
   - `Combined_Discharge_Summary_Bill.pdf`
2. **Ingestion REST Call:** SPA posts `multipart/form-data` to `POST /api/v1/claims/upload`.
3. **Validation & Storage:** Backend verifies exactly 2 files present. Generates `CLM-20260811-0001`. Saves PDF binaries to disk storage directory.
4. **Extraction Service:** Backend runs PDFBox / Tess4J OCR to extract structured values into `ExtractedClaimData`.
5. **Database Storage:** Saves initial claim entity and extracted structured data JSON into PostgreSQL.
6. **Rule Engine Execution:** Evaluates rules **R01** through **R10** sequentially against extracted values and registered policy database.
7. **Decision Resolution:** Determines outcome status (`APPROVED`, `REJECTED`, or `NEEDS_MANUAL_REVIEW`). Writes `rule_results` to database.
8. **REST Response:** Returns complete processed JSON response to Frontend with Claim ID, Extracted Fields, Rule Execution Matrix, and Decision Outcome.
9. **Dashboard & Audit View:** User reviews claim on Dashboard, inspects rule breakdown in Detail View, and downloads PDF Summary report.
