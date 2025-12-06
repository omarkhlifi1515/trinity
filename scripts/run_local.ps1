<#
PowerShell helper to start the project locally using Docker Compose.

Usage (run in repo root with elevated privileges if needed):
    pwsh -ExecutionPolicy Bypass -File .\scripts\run_local.ps1

What it does:
- Detects whether `docker compose` or `docker-compose` is available
- Brings up services from `docker/docker-compose.yml`
- Waits for the frappe container to appear and runs `/workspace/init.sh` inside it
- Prints next commands to start the bench and frontend if you want to run them interactively

Notes:
- Requires Docker Desktop (Windows) and access to run Docker commands.
- This script does not run `bench start` interactively (that blocks the terminal). It runs init and then shows commands.
#>

function Write-Header($msg) { Write-Host "`n==== $msg ====" -ForegroundColor Cyan }

try {
    Write-Header "Checking Docker availability"
    $dockerVersion = & docker --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker is not available. Install Docker Desktop and ensure 'docker' is in PATH."
        exit 1
    }
    Write-Host "$dockerVersion"

    # choose compose command
    $composeCmd = "docker compose"
    & docker compose version > $null 2>&1
    if ($LASTEXITCODE -ne 0) {
        $composeCmd = "docker-compose"
        & docker-compose version > $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Neither 'docker compose' nor 'docker-compose' is available. Install Docker Compose."
            exit 1
        }
    }

    Write-Header "Starting services with $composeCmd"
    & $composeCmd -f .\docker\docker-compose.yml up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to start docker-compose services"
        exit 1
    }

    Write-Header "Waiting for frappe container"
    $containerName = $null
    for ($i=0; $i -lt 60; $i++) {
        # try to find a container that uses the frappe bench image or has 'frappe' in its name
        $names = & docker ps --format '{{.Names}}' | Where-Object { $_ -match 'frappe' }
        if ($names) { $containerName = $names[0]; break }
        Start-Sleep -Seconds 2
        Write-Host -NoNewline '.'
    }

    if (-not $containerName) {
        Write-Error "Could not find the frappe container after waiting. Check 'docker ps' for running containers."
        exit 1
    }

    Write-Header "Running initialization inside container: $containerName"
    # run init script - non-interactive
    & docker exec $containerName bash -c "if [ -f /workspace/init.sh ]; then bash /workspace/init.sh; else echo 'init.sh not found'; fi"

    Write-Header "Initialization completed (if no errors shown above)"
    Write-Host "To start the bench interactively run (inside the container or using docker exec):"
    Write-Host "  docker exec -it $containerName bash -c 'bench start'" -ForegroundColor Yellow

    Write-Host "Or use docker compose exec to open a shell in the container:"
    Write-Host "  $composeCmd exec frappe bash" -ForegroundColor Yellow

    Write-Host "Frontend dev (on your host):" -ForegroundColor Green
    Write-Host "  cd frontend" -ForegroundColor Yellow
    Write-Host "  yarn install" -ForegroundColor Yellow
    Write-Host "  yarn dev" -ForegroundColor Yellow

    Write-Header "Logs (tail)"
    Write-Host "To stream logs for troubleshooting, run:" -ForegroundColor Green
    Write-Host "  $composeCmd -f .\docker\docker-compose.yml logs -f" -ForegroundColor Yellow

} catch {
    Write-Error "An unexpected error occurred: $_"
    exit 1
}
