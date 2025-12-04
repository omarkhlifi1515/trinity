# Port Scanner Script (Windows PowerShell)
# Usage: .\port_scan.ps1 TARGET [PORTS]

param(
    [Parameter(Mandatory=$true)]
    [string]$Target,
    
    [Parameter(Mandatory=$false)]
    [string]$Ports = "80,443,22,21,25,53,3389,8080"
)

Write-Host "Starting Port Scan on $Target..." -ForegroundColor Green
Write-Host "Scanning ports: $Ports" -ForegroundColor Cyan

$portArray = $Ports -split ','
$openPorts = @()

foreach ($port in $portArray) {
    $port = $port.Trim()
    try {
        $connection = Test-NetConnection -ComputerName $Target -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "Port $port - OPEN" -ForegroundColor Green
            $openPorts += $port
        } else {
            Write-Host "Port $port - CLOSED" -ForegroundColor Red
        }
    } catch {
        Write-Host "Port $port - ERROR" -ForegroundColor Yellow
    }
}

Write-Host "`nFound $($openPorts.Count) open ports: $($openPorts -join ', ')" -ForegroundColor Yellow

exit 0

