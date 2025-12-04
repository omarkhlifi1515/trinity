# Service Enumeration Script (Windows)
# Usage: .\service_enum.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting Service Enumeration on $Target..." -ForegroundColor Green

try {
    # Try to get services via WMI (requires credentials)
    Write-Host "`nAttempting to enumerate services..." -ForegroundColor Cyan
    
    try {
        $services = Get-WmiObject -Class Win32_Service -ComputerName $Target -ErrorAction Stop
        Write-Host "`nFound $($services.Count) services:" -ForegroundColor Green
        
        $services | Select-Object Name, DisplayName, State, StartMode | Format-Table -AutoSize
        
        # Show running services
        $runningServices = $services | Where-Object { $_.State -eq "Running" }
        Write-Host "`nRunning Services: $($runningServices.Count)" -ForegroundColor Cyan
        $runningServices | Select-Object Name, DisplayName | Format-Table -AutoSize
        
        exit 0
    } catch {
        Write-Host "Cannot enumerate services (authentication required)" -ForegroundColor Yellow
        Write-Host "Error: $_" -ForegroundColor Red
        
        # Fallback: Check if target is reachable
        $ping = Test-Connection -ComputerName $Target -Count 1 -Quiet
        if ($ping) {
            Write-Host "`nTarget is reachable but service enumeration requires credentials" -ForegroundColor Yellow
            exit 0
        } else {
            Write-Host "Target is not reachable" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

