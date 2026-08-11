# TPA Health Insurance Claim Processing System

Production-grade, modular monolith system for automated health insurance claim ingestion, OCR & text extraction, Flyway schema migrations, business rule validation, decision adjudication, and PDF report export.

---

## 📌 Architecture & Dedicated Port Allocation

> [!IMPORTANT]
> **Ports 3000 and 8080 are intentionally NOT used anywhere in this system** because those ports are reserved by existing applications (e.g., Jenkins on 8080).

| Service | Dedicated Host Port | Internal Container Port | Access URL / Connection String |
|---|---|---|---|
| **Frontend SPA** | `7001` | `7001` | [http://localhost:7001](http://localhost:7001) |
| **Backend REST API** | `7002` | `7002` | [http://localhost:7002](http://localhost:7002) |
| **PostgreSQL Database** | `7003` | `5432` | `localhost:7003` (`tpa_claim_db`) |

---

## 🐘 Local pgAdmin Connection Settings

To inspect or manage the database from your **Local Windows pgAdmin**:

| Setting Key | Parameter Value |
|---|---|
| **Host Name / Address** | `localhost` |
| **Port** | `7003` |
| **Maintenance Database** | `tpa_claim_db` |
| **Username** | `tpa_user` |
| **Password** | `tpa_password` (configured in `.env`) |

### **Connection Routing Explanation:**
- **Local Host Access (pgAdmin / Host Apps):** Connects to `localhost:7003` via Docker port mapping (`7003:5432`). Host port `5432` is intentionally NOT exposed to prevent conflicts with local PostgreSQL installations.
- **Docker Network Access (Spring Boot inside Docker):** Connects directly to container hostname `postgres:5432`:
  `jdbc:postgresql://postgres:5432/tpa_claim_db`

---

## 🗄️ Database Flyway Migrations (`db/migration/V1__init_schema.sql`)

Automated database schema migrations are managed via **Flyway**. On application launch, Flyway executes `V1__init_schema.sql` creating the following 7 core tables:

1. **`policies`**: Policy master data, coverage caps, and copay rates.
2. **`claims`**: Core claim records, patient details, and decision statuses.
3. **`claim_documents`**: Ingested document metadata, paths, and SHA-256 checksums.
4. **`discharge_details`**: Hospitalization dates, diagnosis, and doctor details.
5. **`hospital_bill_details`**: Room rent, ICU fee, doctor fee, and total bill amounts.
6. **`claim_rule_results`**: Deterministic 10-rule evaluation audit log (`R01`-`R10`).
7. **`claim_json`**: Extracted text payloads and full JSON representation.

---

## 🚀 Getting Started

### Option 1: Run with Docker Compose (Recommended)

1. **Environment Setup:**
   ```bash
   cp .env.example .env
   ```

2. **Build and Launch Containers:**
   ```bash
   docker compose build
   docker compose up -d
   ```

3. **Verify Container & Database Health:**
   ```bash
   docker compose ps
   ```

4. **Access Applications:**
   - **Frontend Dashboard:** [http://localhost:7001](http://localhost:7001)
   - **Backend REST API:** [http://localhost:7002/api/claims](http://localhost:7002/api/claims)
   - **pgAdmin / Database:** `localhost:7003` (`tpa_claim_db`, user: `tpa_user`, pass: `tpa_password`)

5. **Stop Containers:**
   ```bash
   docker compose down
   ```

---

### Option 2: Run Locally (Without Docker)

#### Prerequisites
- Java 21 LTS
- Maven 3.9+
- Node.js 20+
- PostgreSQL database listening on `localhost:7003` (or H2 test profile)

#### 1. Backend Application (Port 7002)
```bash
# Run unit & rule engine tests (uses H2 in-memory scope)
mvn clean test

# Package standalone executable JAR
mvn clean package

# Run Spring Boot backend
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar
# OR use run script:
.\run.bat    # Windows
./run.sh     # Linux / Mac
```

#### 2. Frontend SPA (Port 7001)
```bash
cd frontend
npm install
npm run dev
```

---

## 🔍 Core Workflow & 10 Business Validation Rules (R01 - R10)

Users upload **exactly two mandatory PDF documents**:
1. **Claim Form PDF**
2. **Combined Hospital Document PDF** (Discharge Summary + Final Bill)

### Decision Priority Matrix
$$\text{REJECTED} > \text{NEEDS\_MANUAL\_REVIEW} > \text{APPROVED}$$

| Rule ID | Name | Severity on Failure | Description |
|---|---|---|---|
| **R01** | Claim Form Missing | `REJECTED` | Claim Form PDF must be attached and non-empty |
| **R02** | Combined Document Missing | `REJECTED` | Combined Discharge & Bill PDF must be attached |
| **R03** | Policy Inactive | `REJECTED` | Policy must exist, be `ACTIVE`, and cover admission date |
| **R04** | Policy Number Missing | `NEEDS_MANUAL_REVIEW` | Policy number must be parseable from claim form |
| **R05** | Patient Name Mismatch | `NEEDS_MANUAL_REVIEW` | Patient name must match across policy & hospital bill |
| **R06** | Hospital Name Mismatch | `NEEDS_MANUAL_REVIEW` | Hospital name must be verified across documents |
| **R07** | Date Mismatch | `NEEDS_MANUAL_REVIEW` | Admission date must be $\le$ Discharge date |
| **R08** | Claimed > Total Bill | `NEEDS_MANUAL_REVIEW` | Claimed amount cannot exceed final hospital bill |
| **R09** | High Value Claim | `NEEDS_MANUAL_REVIEW` | Claims exceeding ₹50,000 flagged for audit |
| **R10** | Possible Duplicate Claim | `NEEDS_MANUAL_REVIEW` | Prevents duplicate submissions for same patient/date |

---

## 📄 Export PDF Report

Every claim includes automated PDF adjudication report generation via Apache PDFBox:
`GET /api/claims/{claimId}/pdf`
