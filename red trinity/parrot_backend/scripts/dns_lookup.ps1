# DNS Lookup Script (Windows)
# Usage: .\dns_lookup.ps1 DOMAIN

param(
    [Parameter(Mandatory=$true)]
    [string]$Domain
)

Write-Host "Starting DNS Lookup for $Domain..." -ForegroundColor Green

try {
    # A Record
    Write-Host "`nA Records:" -ForegroundColor Cyan
    Resolve-DnsName -Name $Domain -Type A | Format-Table
    
    # MX Record
    Write-Host "`nMX Records:" -ForegroundColor Cyan
    Resolve-DnsName -Name $Domain -Type MX | Format-Table
    
    # NS Record
    Write-Host "`nNS Records:" -ForegroundColor Cyan
    Resolve-DnsName -Name $Domain -Type NS | Format-Table
    
    # TXT Record
    Write-Host "`nTXT Records:" -ForegroundColor Cyan
    Resolve-DnsName -Name $Domain -Type TXT | Format-Table
    
    exit 0
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

