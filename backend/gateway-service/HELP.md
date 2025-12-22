# MobileSec-MS Gateway-Service

This document supplements the default Spring Boot help content and captures the MobileSec-MS specific guidance for running and testing the gateway-service.

## Phase 12 – Gateway Integration

The gateway-service now fronts both analysis-service (port `8082`) and report-service (port `8084`). All client traffic should enter via the gateway on port `8083`.

### Prerequisites

1. **analysis-service** running locally on `http://localhost:8082`
2. **report-service** running locally on `http://localhost:8084`
3. Maven installed / available via wrapper

### Running the gateway

```bash
mvn spring-boot:run
```

The gateway listens on `http://localhost:8083`. If the port is occupied, either stop the conflicting process or temporarily adjust `server.port` in `application.yml`.

### Config summary

* `/api/analysis/upload` → proxies to `http://localhost:8082/analysis/upload`
* `/api/analysis/results/**` → proxies to `http://localhost:8082/analysis/results/**`
* `/reports/by-id/{id}?format=JSON|SARIF|PDF` → proxies to `http://localhost:8084/reports/by-id/{id}?format=...`
* Global CORS allows `GET`, `POST`, and `OPTIONS` from any origin, ready to tighten once auth is introduced.
* Gateway emits standardized JSON error payloads via `GatewayErrorAttributes` with clear `503` messaging when downstream services are unreachable.

### Verification checklist (Postman / curl)

1. **Upload**
   * Request: `POST http://localhost:8083/api/analysis/upload`
   * Body: same as direct analysis-service payload.
   * Expectation: identical JSON to calling `http://localhost:8082/analysis/upload`.

2. **Analysis results page**
   * Request: `GET http://localhost:8083/api/analysis/results?page=0&size=5`
   * Expectation: same paginated list as direct call `:8082`.

3. **Analysis result detail**
   * Request: `GET http://localhost:8083/api/analysis/results/{id}`
   * Expectation: full record matches analysis-service response.

4. **Report generation (all formats)**
   * Request: `POST http://localhost:8083/reports/by-id/13?format=JSON`
   * Repeat for SARIF and PDF variants.
   * Expectation: payloads (JSON/SARIF) and downloadable PDF mirror results from `http://localhost:8084`.

5. **Failure scenarios**
   * Stop analysis-service or report-service.
   * Reissue any gateway call dependent on the stopped service.
   * Expectation: gateway returns `503` with message `"Downstream service is unavailable. Please try again later."` and no stack trace leakage.

### Troubleshooting

| Symptom | Likely cause | Resolution |
| --- | --- | --- |
| `503 Service Unavailable` | Downstream service offline or port mismatch | Ensure corresponding microservice is running on the expected port. |
| `404 Not Found` for `/api/...` | Request path missing `/api` prefix | Use the documented gateway paths; the StripPrefix filter removes `/api` before forwarding. |
| Gateway startup fails (port in use) | Another service already bound to `8083` | Stop the conflicting process or change `server.port`. |
| CORS blocked in browser | Origin not matching eventual production allow-list | Update `allowed-origin-patterns` in `application.yml` once auth is in place. |

### Preparing for future authentication

* The gateway currently operates without authentication but headers—including `Authorization`—are preserved. Once JWT or API keys are required, introduce the appropriate filter beans and tighten CORS accordingly.
* Centralized error handling is already prepared to surface consistent responses.

---

For additional Spring documentation, consult the links below:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.7/maven-plugin)
* [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/reference/)
