# Phase 11 – report-service quick start

This document explains how to run and validate the **report-service** introduced in MobileSec‑MS Phase 11.

## Prerequisites

1. **Java 17** and **Maven 3.9+** installed.
2. Services running locally:
   * `analysis-service` on port **8082**.
   * `gateway-service` on port **8083** (optional for now, direct calls go to report-service).

## Configuration

`src/main/resources/application.yml` sets:

```yaml
spring:
  application:
    name: report-service
server:
  port: 8084
mobilesec:
  analysis:
    base-url: http://localhost:8082
```

Change the base URL if analysis-service is hosted elsewhere.

## Build & run

```bash
./mvnw spring-boot:run
```

The application exposes `/reports/**` endpoints.

## API overview

| Endpoint | Description |
| --- | --- |
| `POST /reports/by-id/{id}?format=JSON|SARIF|PDF` | Generate report using analysis ID. |
| `POST /reports/by-package/{packageName}?format=...` | Generate report using package name. |

Formats:

* `JSON` – original analysis JSON.
* `SARIF` – SARIF 2.1.0 for CI/security tooling.
* `PDF` – Printable summary with metadata and highlights.

## Example responses

### JSON

```json
{
  "id": 13,
  "packageName": "com.example.app",
  "versionName": "1.0.0",
  "riskLevel": "MEDIUM",
  "riskReasons": [
    "allowBackup=true",
    "permission=ACCESS_FINE_LOCATION"
  ],
  "permissions": ["android.permission.ACCESS_FINE_LOCATION"],
  "manifestFlags": {
    "debuggable": false,
    "allowBackup": true,
    "cleartextTrafficPermitted": false
  },
  "exportedComponents": [
    {
      "name": "com.example.app.MainActivity",
      "type": "activity",
      "exported": true,
      "permission": null,
      "intentFilters": ["android.intent.action.MAIN"]
    }
  ],
  "createdAt": "2025-11-18T12:30:27Z"
}
```

### SARIF (excerpt)

```json
{
  "version": "2.1.0",
  "runs": [
    {
      "tool": { "driver": { "name": "MobileSec-MS Analysis" } },
      "results": [
        {
          "ruleId": "MOB-RISK-MEDIUM",
          "level": "warning",
          "message": { "text": "allowBackup=true" }
        }
      ]
    }
  ]
}
```

### PDF

The PDF response is binary. Expect a downloadable file named `analysis-report-13.pdf` with summary, manifest flags, permissions, and exported components laid out for auditors.

## Postman verification

1. Create a collection called **MobileSec Phase 11 – report-service**.
2. Add requests:
   * `POST http://localhost:8084/reports/by-id/13?format=JSON`
   * `POST http://localhost:8084/reports/by-id/13?format=SARIF`
   * `POST http://localhost:8084/reports/by-id/13?format=PDF`
3. Set **Accept** header to match the format (`application/json` or `application/pdf`).
4. For PDF request, in Postman switch to “Send and Download” to save the file.

## Next steps (Phase 12+ preview)

* Add Spring Cloud Gateway route to forward `/api/reports/**` to port 8084.
* Introduce persistence for generated reports if caching is required.
* Enrich PDF styling and add localization if needed.

## Phase 11 recap

Report-service now consumes analysis-service outputs and exposes JSON, SARIF, and PDF reports. This enables developers to triage issues quickly, auditors to obtain printable summaries, and CI/CD pipelines to ingest SARIF for automated policy checks.

