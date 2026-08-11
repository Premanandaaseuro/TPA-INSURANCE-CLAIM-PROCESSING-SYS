# TPA Health Insurance Claim Processing System

Production-grade, modular monolith system for automated health insurance claim ingestion, OCR & text extraction, business rule validation, decision adjudication, and PDF report export.

---

## 📌 Architecture & Technology Stack

- **Backend:** Java 21 LTS, Spring Boot 3.4.3 / 3.5, Spring Web, Spring Data JPA, Hibernate, Jackson, Bean Validation
- **Database:** PostgreSQL 17
- **Extraction Engine:** Apache PDFBox 3.0.3 + Tess4J / Tesseract OCR
- **Frontend:** React 18, Vite 6, TypeScript 5, Tailwind CSS 3, Lucide React
- **Containerization:** Multi-stage Docker & Docker Compose

---

## ⚡ Important Infrastructure & Port Notice

> [!IMPORTANT]
> **Port 8080 is intentionally NOT used anywhere in this system** because port 8080 is reserved by local infrastructure (such as Jenkins).

| Service | Host Port | Internal Container Port | Access URL |
|---|---|---|---|
| **Frontend SPA** | `3000` | `3000` | [http://localhost:3000](http://localhost:3000) |
| **Backend REST API** | `8081` | `8081` | [http://localhost:8081](http://localhost:8081) |
| **PostgreSQL Database** | `5433` | `5432` | `localhost:5433` |

### **Why PostgreSQL Host Port is 5433:**
Host port `5433` is mapped to internal container port `5432` (`5433:5432`) to prevent port conflicts with any locally running PostgreSQL instance or existing service on host port 5432. Inside the Docker network, the backend container connects directly to `postgres:5432`.

---

## 🚀 Getting Started

### Option 1: Run with Docker Compose (Recommended)

1. **Clone & Environment Setup:**
   ```bash
   cp .env.example .env
   ```

2. **Build and Launch Containers:**
   ```bash
   docker compose build
   docker compose up -d
   ```

3. **Verify Container Status:**
   ```bash
   docker compose ps
   ```

4. **Access Applications:**
   - **Frontend Dashboard:** [http://localhost:3000](http://localhost:3000)
   - **Backend API:** [http://localhost:8081/api/claims](http://localhost:8081/api/claims)
   - **PostgreSQL Database:** `localhost:5433` (`tpa_claims_db`)

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
- PostgreSQL listening on `localhost:5433` (or H2 test profile)

#### 1. Backend Application (Port 8081)
```bash
# Build standalone executable JAR
mvn clean package

# Run Spring Boot backend
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar
# OR use run script:
.\run.bat    # Windows
./run.sh     # Linux / Mac
```

#### 2. Frontend SPA (Port 3000)
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
