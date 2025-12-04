# Ping Sweep Script (Windows)
# Usage: .\ping_sweep.ps1 NETWORK

param(
    [Parameter(Mandatory=$true)]
    [string]$Network
)

Write-Host "Starting Ping Sweep on $Network..." -ForegroundColor Green

# Extract network base (e.g., 192.168.1)
$networkBase = $Network -replace '\.\d+$', ''

Write-Host "Scanning network: $networkBase.0/24" -ForegroundColor Cyan

$results = @()
1..254 | ForEach-Object {
    $ip = "$networkBase.$_"
    $ping = Test-Connection -ComputerName $ip -Count 1 -Quiet -ErrorAction SilentlyContinue
    if ($ping) {
        Write-Host "$ip - ONLINE" -ForegroundColor Green
        $results += $ip
    }
}

Write-Host "`nFound $($results.Count) active hosts" -ForegroundColor Yellow
$results

exit 0

