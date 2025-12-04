# Registry Scan Script (Windows - Local Only)
# Usage: .\registry_scan.ps1

Write-Host "Starting Registry Scan..." -ForegroundColor Green
Write-Host "Note: This scan runs on local machine only" -ForegroundColor Yellow

try {
    # Check for common security misconfigurations
    Write-Host "`nChecking Registry Security Settings..." -ForegroundColor Cyan
    
    # Check UAC
    $uac = Get-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System" -Name "EnableLUA" -ErrorAction SilentlyContinue
    if ($uac.EnableLUA -eq 1) {
        Write-Host "UAC Enabled - SECURE" -ForegroundColor Green
    } else {
        Write-Host "UAC Disabled - INSECURE" -ForegroundColor Red
    }
    
    # Check Remote Desktop
    $rdp = Get-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\Terminal Server" -Name "fDenyTSConnections" -ErrorAction SilentlyContinue
    if ($rdp.fDenyTSConnections -eq 0) {
        Write-Host "Remote Desktop Enabled" -ForegroundColor Yellow
    } else {
        Write-Host "Remote Desktop Disabled" -ForegroundColor Green
    }
    
    # Check Windows Firewall
    $firewall = Get-NetFirewallProfile | Where-Object { $_.Enabled -eq $true }
    if ($firewall) {
        Write-Host "Windows Firewall Enabled - SECURE" -ForegroundColor Green
    } else {
        Write-Host "Windows Firewall Disabled - INSECURE" -ForegroundColor Red
    }
    
    Write-Host "`nRegistry scan completed!" -ForegroundColor Green
    exit 0
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

