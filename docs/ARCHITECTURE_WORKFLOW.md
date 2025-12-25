# MobileSec-MS Architecture & Workflow

## Overview

MobileSec-MS combines a React frontend, Supabase authentication, and a set of coordinated backend microservices orchestrated behind a Spring Cloud Gateway. The platform streamlines automated APK security reviews by routing uploads through an analysis pipeline, enriching the results with secret hunting, crypto checks, and reporting capabilities.

## Key Components

| Layer | Description |
| --- | --- |
| **Frontend** | `mobilesec-react` SPA handles drag-and-drop APK uploads, invokes Supabase auth, shows progress using steppers, and calls analysis endpoints through `services/api.js`. |
| **Gateway** | Spring Cloud Gateway exposes a unified entry point (`http://localhost:8083/api/*`), forwards requests to the right service, and applies CORS/default filter rules. |
| **Microservices** | - **APK Scanner (FastAPI)**: Handles uploads, manifest parsing, string extraction, and stores metadata (SQLite).<br>- **Secret Hunter (FastAPI)**: Runs GitLeaks/YARA/regex-based secrets analysis on extracted strings or raw files.<br>- **Crypto Check (Spring Boot)**: Uses SAST rules to flag weak crypto usage.<br>- **ReportGen (Spring Boot)**: Aggregates findings into PDF/SARIF/JSON artifacts.<br>- **Network Inspector**: Observes live network sessions via `mitmproxy` for runtime leaks (supports analysis service dashboards).<br>- **FixSuggest** (if enabled): Suggests fixes for detected issues.<br>- **CIConnector**: Triggers scans in CI pipelines. |
| **Data Layer** | SQLite (APK Scanner) holds scan metadata and user linkage; Supabase (auth) stores user identities and JWT secrets (used by backend services). |

## Workflow (BPMN)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions"
                  targetNamespace="https://mobilesec-ms">
  <bpmn:process id="MobileSecMSProcess" isExecutable="false">
    <bpmn:startEvent id="Start" name="User opens portal"/>
    <bpmn:task id="Auth" name="Authenticate via Supabase"/>
    <bpmn:exclusiveGateway id="AuthDecision" name="Auth success?"/>
    <bpmn:task id="Upload" name="Drag & drop APK upload"/>
    <bpmn:task id="UploadGateway" name="Frontend → Gateway (/api/scan/analyze)"/>
    <bpmn:task id="StoreMetadata" name="Persist metadata & user_id"/>
    <bpmn:task id="ManifestExtraction" name="APK manifest & string extraction"/>
    <bpmn:parallelGateway id="DeepScanSplit" name="Fan-out deep scans"/>
    <bpmn:task id="SecretHunter" name="SecretHunter analysis"/>
    <bpmn:task id="CryptoCheck" name="CryptoCheck analysis"/>
    <bpmn:parallelGateway id="DeepScanJoin"/>
    <bpmn:task id="ReportGen" name="ReportGen aggregates findings"/>
    <bpmn:task id="Dashboard" name="Dashboard stats (GET /api/dashboard/stats)"/>
    <bpmn:endEvent id="SuccessEnd" name="Results returned to UI"/>
    <bpmn:task id="AuthFail" name="Show auth error"/>
    <bpmn:endEvent id="FailureEnd" name="Authentication stopped"/>

    <bpmn:sequenceFlow id="flow1" sourceRef="Start" targetRef="Auth"/>
    <bpmn:sequenceFlow id="flow2" sourceRef="Auth" targetRef="AuthDecision"/>
    <bpmn:sequenceFlow id="flow3" sourceRef="AuthDecision" targetRef="Upload">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">valid</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="flow4" sourceRef="Upload" targetRef="UploadGateway"/>
    <bpmn:sequenceFlow id="flow5" sourceRef="UploadGateway" targetRef="StoreMetadata"/>
    <bpmn:sequenceFlow id="flow6" sourceRef="StoreMetadata" targetRef="ManifestExtraction"/>
    <bpmn:sequenceFlow id="flow7" sourceRef="ManifestExtraction" targetRef="DeepScanSplit"/>
    <bpmn:sequenceFlow id="flow8" sourceRef="DeepScanSplit" targetRef="SecretHunter"/>
    <bpmn:sequenceFlow id="flow9" sourceRef="DeepScanSplit" targetRef="CryptoCheck"/>
    <bpmn:sequenceFlow id="flow10" sourceRef="SecretHunter" targetRef="DeepScanJoin"/>
    <bpmn:sequenceFlow id="flow11" sourceRef="CryptoCheck" targetRef="DeepScanJoin"/>
    <bpmn:sequenceFlow id="flow12" sourceRef="DeepScanJoin" targetRef="ReportGen"/>
    <bpmn:sequenceFlow id="flow13" sourceRef="ReportGen" targetRef="Dashboard"/>
    <bpmn:sequenceFlow id="flow14" sourceRef="Dashboard" targetRef="SuccessEnd"/>
    <bpmn:sequenceFlow id="flow15" sourceRef="AuthDecision" targetRef="AuthFail">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">invalid</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="flow16" sourceRef="AuthFail" targetRef="FailureEnd"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_MobileSecMS">
    <bpmndi:BPMNPlane bpmnElement="MobileSecMSProcess">
      <bpmndi:BPMNShape id="shape_Start" bpmnElement="Start">
        <dc:Bounds x="100" y="40" width="40" height="40"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_Auth" bpmnElement="Auth">
        <dc:Bounds x="180" y="40" width="100" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_AuthDecision" bpmnElement="AuthDecision">
        <dc:Bounds x="310" y="40" width="50" height="50"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_Upload" bpmnElement="Upload">
        <dc:Bounds x="390" y="40" width="110" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_UploadGateway" bpmnElement="UploadGateway">
        <dc:Bounds x="520" y="40" width="140" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_StoreMetadata" bpmnElement="StoreMetadata">
        <dc:Bounds x="680" y="40" width="150" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_ManifestExtraction" bpmnElement="ManifestExtraction">
        <dc:Bounds x="850" y="40" width="170" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_DeepScanSplit" bpmnElement="DeepScanSplit">
        <dc:Bounds x="1040" y="40" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_SecretHunter" bpmnElement="SecretHunter">
        <dc:Bounds x="1130" y="10" width="140" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_CryptoCheck" bpmnElement="CryptoCheck">
        <dc:Bounds x="1130" y="80" width="140" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_DeepScanJoin" bpmnElement="DeepScanJoin">
        <dc:Bounds x="1310" y="40" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_ReportGen" bpmnElement="ReportGen">
        <dc:Bounds x="1390" y="40" width="150" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_Dashboard" bpmnElement="Dashboard">
        <dc:Bounds x="1560" y="40" width="180" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_SuccessEnd" bpmnElement="SuccessEnd">
        <dc:Bounds x="1760" y="40" width="40" height="40"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_AuthFail" bpmnElement="AuthFail">
        <dc:Bounds x="360" y="140" width="160" height="60"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_FailureEnd" bpmnElement="FailureEnd">
        <dc:Bounds x="540" y="150" width="40" height="40"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="edge_flow1" bpmnElement="flow1">
        <di:waypoint x="140" y="60"/>
        <di:waypoint x="180" y="70"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow2" bpmnElement="flow2">
        <di:waypoint x="280" y="70"/>
        <di:waypoint x="310" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow3" bpmnElement="flow3">
        <di:waypoint x="360" y="65"/>
        <di:waypoint x="390" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow4" bpmnElement="flow4">
        <di:waypoint x="500" y="65"/>
        <di:waypoint x="520" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow5" bpmnElement="flow5">
        <di:waypoint x="660" y="65"/>
        <di:waypoint x="680" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow6" bpmnElement="flow6">
        <di:waypoint x="830" y="65"/>
        <di:waypoint x="850" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow7" bpmnElement="flow7">
        <di:waypoint x="1020" y="65"/>
        <di:waypoint x="1040" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow8" bpmnElement="flow8">
        <di:waypoint x="1065" y="40"/>
        <di:waypoint x="1130" y="40"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow9" bpmnElement="flow9">
        <di:waypoint x="1065" y="90"/>
        <di:waypoint x="1130" y="110"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow10" bpmnElement="flow10">
        <di:waypoint x="1270" y="40"/>
        <di:waypoint x="1310" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow11" bpmnElement="flow11">
        <di:waypoint x="1270" y="140"/>
        <di:waypoint x="1310" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow12" bpmnElement="flow12">
        <di:waypoint x="1360" y="65"/>
        <di:waypoint x="1390" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow13" bpmnElement="flow13">
        <di:waypoint x="1540" y="65"/>
        <di:waypoint x="1560" y="65"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow14" bpmnElement="flow14">
        <di:waypoint x="1740" y="65"/>
        <di:waypoint x="1760" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow15" bpmnElement="flow15">
        <di:waypoint x="340" y="65"/>
        <di:waypoint x="360" y="140"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow16" bpmnElement="flow16">
        <di:waypoint x="440" y="170"/>
        <di:waypoint x="540" y="170"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
```

### Notes

1. **Supabase JWT**: `services/api.js` injects the token into every request so the backend can look up the `user_id` for row-level security in `APKMetadata`.
2. **Scan Result Storage**: The APK metadata is stored per user (`user_id`) allowing the dashboard and `/api/scan/recents` to return "my data only" results.
3. **Async Ingestion**: While the BPMN stops at the dashboard, the services also notify `ReportGen`, `NetworkInspector`, and `FixSuggest` (if enabled) via HTTP events or shared queues, enabling reporting and remediation suggestions outside this core flow.
