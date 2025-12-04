# Whois Lookup Script (Windows)
# Usage: .\whois_lookup.ps1 DOMAIN

param(
    [Parameter(Mandatory=$true)]
    [string]$Domain
)

Write-Host "Starting Whois Lookup for $Domain..." -ForegroundColor Green

# Check if whois is available
$whoisPath = Get-Command whois -ErrorAction SilentlyContinue

if ($whoisPath) {
    # Use whois command if available
    whois $Domain
    exit $LASTEXITCODE
} else {
    # Use PowerShell DNS cmdlets as alternative
    Write-Host "Whois command not found. Using DNS lookup instead..." -ForegroundColor Yellow
    
    try {
        $dnsInfo = Resolve-DnsName -Name $Domain -Type ANY -ErrorAction Stop
        $dnsInfo | Format-List
        
        Write-Host "`nFor full whois info, install whois from:" -ForegroundColor Yellow
        Write-Host "https://docs.microsoft.com/en-us/sysinternals/downloads/whois" -ForegroundColor Cyan
        
        exit 0
    } catch {
        Write-Host "Error: $_" -ForegroundColor Red
        exit 1
    }
}

