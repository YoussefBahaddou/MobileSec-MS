# scripts/ci_cleanup.ps1
# Usage: powershell -File scripts/ci_cleanup.ps1

Write-Host "--- Starting CI Port Cleanup ---"

# Function to stop container by port
function Stop-ContainerOnPort {
    param (
        [int]$Port
    )
    Write-Host "Checking for containers on port $Port..."
    
    # Get container ID for the specific published port
    # Format: 0.0.0.0:3000->80/tcp or similar. We look for the port mapping.
    $containerId = docker ps -q --filter "publish=$Port"
    
    if ($containerId) {
        Write-Host "Found container ($containerId) using port $Port. Stopping..."
        docker rm -f $containerId
        if ($?) {
            Write-Host "Successfully removed container ($containerId)."
        } else {
            Write-Error "Failed to remove container ($containerId)."
            exit 1
        }
    } else {
        Write-Host "No containers found on port $Port."
    }
}

# Clean specific ports used by MobileSec-MS
Stop-ContainerOnPort -Port 3000  # Frontend
Stop-ContainerOnPort -Port 8083  # Gateway
Stop-ContainerOnPort -Port 8088  # APK Scanner
Stop-ContainerOnPort -Port 8089  # Secret Hunter (Failed last time)
Stop-ContainerOnPort -Port 8087  # Network Inspector
Stop-ContainerOnPort -Port 8080  # Crypto Check
Stop-ContainerOnPort -Port 8081  # Report Service
Stop-ContainerOnPort -Port 8085  # Fix Suggest


# Double check strictly for "mobilesec-cd" stack just in case
Write-Host "Ensuring standard docker-compose down logic..."
docker-compose down --volumes --remove-orphans 2>$null

Write-Host "--- CI Cleanup Complete ---"
exit 0
