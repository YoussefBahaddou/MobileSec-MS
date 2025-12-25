# MobileSec-MS All-in-One Startup Script (Admin Mode)
# Ensures all 6 microservices + Frontend are running.

$services = @(
    @{ Name="1. Gateway Service (8083)"; Path="backend/gateway-service"; Command="./mvnw spring-boot:run" },
    @{ Name="3. APK Scanner (8088)"; Path="backend/apk-scanner"; Command="uvicorn app.main:app --port 8088 --reload" },
    @{ Name="4. Secret Hunter (8089)"; Path="backend/secret-hunter"; Command="uvicorn app.main:app --port 8089 --reload" },
    @{ Name="5. Crypto Check (8090)"; Path="backend/crypto-check"; Command="./mvnw spring-boot:run" },
    @{ Name="6. Network Inspector (8087)"; Path="backend/network-inspector"; Command="uvicorn app.main:app --port 8087 --reload" },
    @{ Name="7. Frontend (3000)"; Path="frontend/mobilesec-react"; Command="npm start" }
)

Write-Host "Starting MobileSec-MS Full Suite..." -ForegroundColor Cyan

foreach ($svc in $services) {
    Write-Host "Launching $($svc.Name)..."
    try {
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $($svc.Path); $($svc.Command)" -Verb RunAs
    } catch {
        Write-Warning "Failed to launch $($svc.Name) as Admin. Trying normal mode..."
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $($svc.Path); $($svc.Command)"
    }
    Start-Sleep -Seconds 1
}

Write-Host "All services launched!" -ForegroundColor Green
Write-Host "Frontent: http://localhost:3000 (or 3001 if 3000 is busy)" -ForegroundColor Yellow
Write-Host "Gateway: http://localhost:8083" -ForegroundColor Yellow
