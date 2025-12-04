# RDP Check Script (Windows)
# Usage: .\rdp_check.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Checking RDP on $Target..." -ForegroundColor Green

try {
    $rdpTest = Test-NetConnection -ComputerName $Target -Port 3389 -WarningAction SilentlyContinue
    
    if ($rdpTest.TcpTestSucceeded) {
        Write-Host "RDP Port 3389 - OPEN" -ForegroundColor Green
        Write-Host "`nRDP is accessible!" -ForegroundColor Yellow
        Write-Host "Warning: Ensure RDP is properly secured!" -ForegroundColor Red
        
        # Try to get RDP version info
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient($Target, 3389)
            $stream = $tcpClient.GetStream()
            $buffer = New-Object byte[] 1024
            $stream.Read($buffer, 0, $buffer.Length) | Out-Null
            Write-Host "`nRDP connection established successfully" -ForegroundColor Green
            $tcpClient.Close()
        } catch {
            Write-Host "Could not establish RDP connection" -ForegroundColor Yellow
        }
        
        exit 0
    } else {
        Write-Host "RDP Port 3389 - CLOSED" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

