# MobileSec-MS Manual Launch Commands

Use these commands if you want to run each service in its own terminal window manually (useful for debugging).

### 1. Gateway Service (Port 8083)
```powershell
cd C:\DEV\Project\backend\gateway-service
mvn spring-boot:run
```

### 2. APK Scanner (Port 8088)
```powershell
cd C:\DEV\Project\backend\apk-scanner
uvicorn app.main:app --port 8088 --reload
```

### 3. Secret Hunter (Port 8089)
```powershell
cd C:\DEV\Project\backend\secret-hunter
uvicorn app.main:app --port 8089 --reload
```

### 4. Crypto Check (Port 8090)
```powershell
cd C:\DEV\Project\backend\crypto-check
mvn spring-boot:run
```

### 5. Network Inspector (Port 8087)
```powershell
cd C:\DEV\Project\backend\network-inspector
uvicorn app.main:app --port 8087 --reload
```

### 6. Frontend (Ports 3000/3001)
```powershell
cd C:\DEV\Project\frontend\mobilesec-react
npm start
```

---
**Tips:**
*   **Java/Spring Boot**: Wait for "Started [ApplicationName] in X seconds".
*   **Python/Uvicorn**: Wait for "Uvicorn running on http://...".
*   **Frontend**: Opens automatically in browser.
