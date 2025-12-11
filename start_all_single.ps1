# MobileSec-MS Single-Window Startup
# Requires Node.js (npx)
# Runs all services in one terminal window with color-coded logs.

Write-Host "Starting MobileSec-MS in Single Window Mode..." -ForegroundColor Cyan

# Define commands
$cmd_gateway = "cd backend/gateway-service && mvn spring-boot:run"
$cmd_analysis = "cd backend/analysis-service/analysis-service && mvn spring-boot:run"
$cmd_apk = "cd backend/apk-scanner && uvicorn app.main:app --port 8088"
$cmd_secrets = "cd backend/secret-hunter && uvicorn app.main:app --port 8089"
$cmd_crypto = "cd backend/crypto-check && mvn spring-boot:run"
$cmd_network = "cd backend/network-inspector && uvicorn app.main:app --port 8087"
$cmd_frontend = "cd frontend/mobilesec-react && npm start"

# Execute using npx concurrently
# We use backticks ` for PowerShell line continuation
# We use \" for inner quotes
npx concurrently -k -p "name" -n "GATEWAY,ANALYSIS,APK,SECRETS,CRYPTO,NET,FRONT" -c "blue,magenta,yellow,red,green,cyan,white" `
    "$cmd_gateway" `
    "$cmd_analysis" `
    "$cmd_apk" `
    "$cmd_secrets" `
    "$cmd_crypto" `
    "$cmd_network" `
    "$cmd_frontend"
