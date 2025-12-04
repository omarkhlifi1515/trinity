# Allow Red Trinity through Windows Firewall
# Run as Administrator

Write-Host "Configuring Windows Firewall for Red Trinity..." -ForegroundColor Green

# Add firewall rule for port 8888
netsh advfirewall firewall add rule name="Red Trinity Server" dir=in action=allow protocol=TCP localport=8888

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Firewall rule added successfully!" -ForegroundColor Green
    Write-Host "Red Trinity is now accessible from other devices on your network" -ForegroundColor Cyan
} else {
    Write-Host "✗ Failed to add firewall rule. Try running as Administrator." -ForegroundColor Red
}

# Show current firewall rules for port 8888
Write-Host "`nCurrent firewall rules for port 8888:" -ForegroundColor Yellow
netsh advfirewall firewall show rule name="Red Trinity Server"

