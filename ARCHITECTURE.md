# Architecture Document - TPA Insurance Claim Processing System

## High-Level Architecture

```
[ Browser / Client ]
       │
  (Port 7001)
       ▼
┌─────────────────────────────────────────────────────────┐
│              React SPA Frontend (Port 7001)             │
│        Dashboard • New Claim Modal • Audit View         │
└────────────────────────────┬────────────────────────────┘
                             │
                    (HTTP / API Port 7002)
                             ▼
┌─────────────────────────────────────────────────────────┐
│        Spring Boot Backend Monolith (Port 7002)         │
│  ingestion │ extraction │ rules │ decision │ export     │
└────────────────────────────┬────────────────────────────┘
                             │
                  (PostgreSQL Port 7003 / 5432)
                             ▼
┌─────────────────────────────────────────────────────────┐
│                 PostgreSQL 17 Database                  │
│       claims • claim_documents • policies • rules       │
└─────────────────────────────────────────────────────────┘
```

---

## 🌐 Network & Port Allocation

- **Frontend Application:** `http://localhost:7001`
- **Backend REST Service:** `http://localhost:7002` (`server.port=7002`)
- **PostgreSQL Database:** `localhost:7003` (`7003:5432` container mapping)
- **Prohibited Ports:** Ports `3000` and `8080` are explicitly NOT used.

---

## Technical Stack
- **Backend:** Java 21, Spring Boot 3.4.3 / 3.5, Spring Data JPA, PostgreSQL
- **Text & OCR Engine:** Apache PDFBox 3.0.3 + Tess4J Tesseract
- **Frontend:** React 18, Vite 6, TypeScript 5, Tailwind CSS 3, Lucide React
- **Containerization:** Multi-stage Dockerfiles + `docker-compose.yml`
