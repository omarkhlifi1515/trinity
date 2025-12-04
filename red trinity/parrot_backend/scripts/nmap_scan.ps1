# Nmap Network Scan Script (Windows)
# Usage: .\nmap_scan.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting Nmap Scan on $Target..." -ForegroundColor Green

# Check if nmap is installed
$nmapPath = Get-Command nmap -ErrorAction SilentlyContinue
if (-not $nmapPath) {
    Write-Host "Error: Nmap is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Install from: https://nmap.org/download.html" -ForegroundColor Yellow
    exit 1
}

# Run nmap scan
nmap -F $Target

if ($LASTEXITCODE -eq 0) {
    Write-Host "Scan completed successfully!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "Scan failed with exit code: $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

