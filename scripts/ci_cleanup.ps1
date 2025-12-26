# scripts/ci_cleanup.ps1
# Usage: powershell -File scripts/ci_cleanup.ps1

Write-Host "--- Starting CI Port Cleanup ---"

# 1. Function to stop Docker container by port
function Stop-ContainerOnPort {
    param (
        [int]$Port
    )
    $containerId = docker ps -q --filter "publish=$Port"
    if ($containerId) {
        Write-Host "Found Docker container ($containerId) using port $Port. Stopping..."
        docker rm -f $containerId | Out-Null
        Write-Host "Successfully removed container ($containerId)."
    }
}

# 2. Function to stop local Windows process by port
function Stop-ProcessOnPort {
    param (
        [int]$Port
    )
    # Find TCP connections on the port that are in 'Listen' state
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    
    foreach ($conn in $connections) {
        $pidVal = $conn.OwningProcess
        if ($pidVal -and $pidVal -gt 0) {
            try {
                $proc = Get-Process -Id $pidVal -ErrorAction Stop
                Write-Host "Found local process '$($proc.ProcessName)' (PID: $pidVal) listening on port $Port. Killing..."
                Stop-Process -Id $pidVal -Force -ErrorAction Continue
                Write-Host "Successfully stopped process ID $pidVal."
            } catch {
                Write-Host "Warning: Found PID $pidVal on port $Port but could not stop it. Access denied or process already gone."
            }
        }
    }
}

# List of critical ports used by MobileSec-MS
$CriticalPorts = @(
    3000,  # Frontend (React)
    8083,  # Gateway Service
    8088,  # APK Scanner
    8089,  # Secret Hunter
    8087,  # Network Inspector
    8080,  # Crypto Check
    8081,  # Report generator
    8085   # Fix Suggest
)

# Execute cleanup
foreach ($port in $CriticalPorts) {
    Write-Host "Cleaning port $port..."
    Stop-ContainerOnPort -Port $port
    Stop-ProcessOnPort -Port $port
}

# Standard Docker Compose Cleanup
Write-Host "Ensuring standard docker-compose down logic..."
docker-compose down --volumes --remove-orphans 2>$null

Write-Host "--- CI Cleanup Complete ---"
exit 0
