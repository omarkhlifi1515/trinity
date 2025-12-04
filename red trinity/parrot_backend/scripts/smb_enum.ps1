# SMB Enumeration Script (Windows)
# Usage: .\smb_enum.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting SMB Enumeration on $Target..." -ForegroundColor Green

try {
    # Check SMB connectivity
    Write-Host "`nChecking SMB connectivity..." -ForegroundColor Cyan
    $smbTest = Test-NetConnection -ComputerName $Target -Port 445 -WarningAction SilentlyContinue
    
    if ($smbTest.TcpTestSucceeded) {
        Write-Host "SMB Port 445 - OPEN" -ForegroundColor Green
        
        # Try to enumerate shares (requires credentials or null session)
        Write-Host "`nAttempting to enumerate shares..." -ForegroundColor Cyan
        try {
            $shares = Get-SmbShare -CimSession $Target -ErrorAction Stop
            $shares | Format-Table Name, Path, Description
        } catch {
            Write-Host "Cannot enumerate shares (authentication required)" -ForegroundColor Yellow
            Write-Host "Error: $_" -ForegroundColor Red
        }
        
        # Check for null session
        Write-Host "`nChecking for null session vulnerability..." -ForegroundColor Cyan
        try {
            $nullSession = New-CimSession -ComputerName $Target -Credential (New-Object System.Management.Automation.PSCredential("", (New-Object System.Security.SecureString))) -ErrorAction Stop
            Write-Host "Null session possible - VULNERABLE!" -ForegroundColor Red
            Remove-CimSession $nullSession
        } catch {
            Write-Host "Null session not possible - SECURE" -ForegroundColor Green
        }
        
    } else {
        Write-Host "SMB Port 445 - CLOSED" -ForegroundColor Red
    }
    
    exit 0
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

