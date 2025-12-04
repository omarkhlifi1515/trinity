# Nmap Full Port Scan Script (Windows)
# Usage: .\nmap_full_scan.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting Nmap Full Port Scan on $Target..." -ForegroundColor Green

$nmapPath = Get-Command nmap -ErrorAction SilentlyContinue
if (-not $nmapPath) {
    Write-Host "Error: Nmap is not installed" -ForegroundColor Red
    exit 1
}

nmap -p- $Target

exit $LASTEXITCODE

