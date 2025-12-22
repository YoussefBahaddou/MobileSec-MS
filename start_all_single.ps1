# MobileSec-MS Multi-Window Startup
# Launches each microservice in a separate PowerShell window.

Write-Host "Starting MobileSec-MS Services in Separate Windows..." -ForegroundColor Cyan

$root = Get-Location

# Function to spawn a new window
function Start-ServiceWindow {
    param (
        [string]$Name,
        [string]$Path,
        [string]$Command
    )
    Write-Host "Launching $Name..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "& {HOST.UI.RawUI.WindowTitle = '$Name'; cd '$root\$Path'; $Command}"
}

# 1. Gateway Service (Port 8083)
Start-ServiceWindow -Name "Gateway Service" -Path "backend\gateway-service" -Command "mvn spring-boot:run"

# 2. Analysis Service (Legacy - Removed, consolidated into APK Scanner)
# Start-ServiceWindow -Name "Analysis Service" -Path "backend\analysis-service\analysis-service" -Command "mvn spring-boot:run"

# 3. APK Scanner (Port 8088)
Start-ServiceWindow -Name "APK Scanner" -Path "backend\apk-scanner" -Command "uvicorn app.main:app --port 8088 --reload"

# 4. Secret Hunter (Port 8089)
Start-ServiceWindow -Name "Secret Hunter" -Path "backend\secret-hunter" -Command "uvicorn app.main:app --port 8089 --reload"

# 5. Crypto Check (Port 8080 or random)
Start-ServiceWindow -Name "Crypto Check" -Path "backend\crypto-check" -Command "mvn spring-boot:run"

# 6. Network Inspector (Port 8087)
Start-ServiceWindow -Name "Network Inspector" -Path "backend\network-inspector" -Command "uvicorn app.main:app --port 8087 --reload"

# 7. Frontend (Port 3000/3001)
Start-ServiceWindow -Name "Frontend React" -Path "frontend\mobilesec-react" -Command "npm start"

Write-Host "All services launched! Check the separate windows." -ForegroundColor Yellow
