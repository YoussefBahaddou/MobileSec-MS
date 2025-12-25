### Firestore Migration (APK Scanner & ReportGen)
Both the Python-based APK Scanner and Java ReportGen services now persist scan metadata to the shared `apk_reports` Firestore collection. Make sure these environment variables are available before each service starts:

```env
FIREBASE_PROJECT_ID=<your_project>
FIREBASE_CREDENTIALS_PATH=<absolute_path_to_service_account.json>
FIRESTORE_COLLECTION=apk_reports
```

ReportGen also reads `GOOGLE_APPLICATION_CREDENTIALS` if `FIRESTORE_CREDENTIALS_PATH` is unset, so the Spring Boot service can bootstrap the Cloud Firestore client on startup. The APK Scanner payload now includes `versionName`, `versionCode`, and `createdAt` so results/dashboards show the file version and the most recent scan timestamp.

# MobileSec-MS Feature User Auth

MobileSec-MS is an automated mobile penetration testing funnel that blends a polished React/Supabase frontend with a Spring Cloud Gateway and several focused backend microservices (APK Scanner, Secret Hunter, Crypto Check, Network Inspector, ReportGen, FixSuggest, etc.). The platform streamlines upload-to-report workflows, keeps user data isolated through Supabase JWTs, and centralizes findings in Firestore-backed Dashboard/Report services.

## Architecture & Workflow

See [docs/ARCHITECTURE_WORKFLOW.md](docs/ARCHITECTURE_WORKFLOW.md) for the BPMN diagram, component map, and detailed flow between frontend, gateway, and microservices.

## Getting Started

### Prerequisites
1. **Node.js** 18+ (frontend)
2. **npm** (frontend dependencies)
3. **Java 17+ & Maven** (Spring Boot microservices, Crypto Check, Gateway, ReportGen)
4. **Python 3.11+** with `uvicorn` and FastAPI (APK Scanner, Secret Hunter, Network Inspector)
5. **Supabase project** for authentication; capture the `SUPABASE_URL` and `SUPABASE_ANON_KEY` for the frontend.
6. **Optional**: Docker (for Network Inspector sandbox) and GitHub Actions/CI connectors if automating scans.

### Environment Configuration
Each backend microservice checks for a `.env` file or environment variables. At minimum:

```env
SUPABASE_JWT_SECRET=<your_supabase_secret>
SUPABASE_URL=<your_supabase_url>
SUPABASE_ANON_KEY=<your_supabase_anon_key>
```

The frontend consumes `REACT_APP_API_URL` (default `http://localhost:8083/api`) and `REACT_APP_SUPABASE_URL` / `REACT_APP_SUPABASE_ANON_KEY`.

### Quick Launch
1. Install dependencies per service:
   - Frontend: `cd frontend/mobilesec-react && npm install`
   - Gateway/Server: `cd backend/[service] && mvn clean install`
   - Python services: ensure `requirements.txt`/`pyproject.toml` dependencies are installed (use Poetry or pip).
2. Invoke the helper script: `start_all.ps1` (Windows, admin mode recommended) to open each service in a separate terminal.
3. Alternatively launch individually:
   - Gateway: `backend/gateway-service mvn spring-boot:run` (port 8083)
   - APK Scanner: `uvicorn app.main:app --port 8088 --reload`
   - Secret Hunter: `uvicorn app.main:app --port 8089 --reload`
   - Crypto Check: `mvn spring-boot:run` (port 8090)
   - Network Inspector: `uvicorn app.main:app --port 8087 --reload`
   - Frontend: `npm start` from `frontend/mobilesec-react`

## Frontend Highlights
- **Supabase auth integration**: tokens are auto-injected into `services/api.js` via axios interceptors.
- **Upload pipeline**: `UploadPage` orchestrates manifest analysis, string extraction, secret hunt, and crypto checks, with stepper animations and progress notifications.
- **Reporting**: `createReport`, `downloadReportById` helpers fetch PDF/SARIF/JSON exports from ReportGen.

## Backend Services Description
1. **Gateway (Spring Cloud Gateway)** – routes `/api/scan`, `/api/secrets`, `/api/crypto`, `/api/dashboard`, etc., while applying CORS and StripPrefix filters.
2. **APK Scanner (FastAPI)** – analyzes uploads (Androguard), stores scan manifests/flags in Firestore, exposes `/api/scan/analyze`, paginated `/analysis/results`, `/dashboard/stats`, and `/scan/recents`. The service now ensures each report carries `versionName`, `versionCode`, and `createdAt` for frontend tables.
3. **Secret Hunter (FastAPI)** – evaluates extracted strings/files for leaked secrets using GitLeak-style logic.
4. **Crypto Check (Spring Boot)** – enforces cryptographic hygiene rules (CWE-based) through `/api/crypto/analyze`.
5. **ReportGen (Spring Boot)** – collates findings into Firestore-backed multi-format reports via `/api/reports`.
6. **Network Inspector (FastAPI + mitmproxy)** – inspects TLS/HTTP traffic from sandboxed Android sessions.
7. **FixSuggest & CIConnector** – optional add-ons for remediation guidance and CI automation.

## Troubleshooting
- **Supabase auth fails?** Verify `supabase.auth.getSession()` returns a valid session; network errors should surface in the browser console since axios logs on error.
- **SQLite lock issues** (APK metadata): confirm only one APK Scanner instance uses `apk_metadata.db` or switch to Postgres and update `DATABASE_URL`.
- **Gateway routing mismatches**: ensure Microservices run on the ports defined in `backend/gateway-service/src/main/resources/application.yaml`.

## Reporting & Dashboards
Users can view scan progress in `/upload`, then generate PDF/SARIF/JSON exports via `createReport` or `downloadReportById`. The data shown is filtered by `user_id` extracted from the Supabase JWT.

## Further Work
- Hook up FixSuggest (if hosted) via `http://localhost:8085/api/suggest`.
- Enable CIConnector scripts to auto-trigger analyses from GitHub Actions or GitLab.
- Expand authentication to support SSO/logouts via Supabase.
