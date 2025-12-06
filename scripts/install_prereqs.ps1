<#
Install system prerequisites for running the project on Windows using Chocolatey.

Usage: Run PowerShell as Administrator and execute:
  pwsh -ExecutionPolicy Bypass -File .\scripts\install_prereqs.ps1

This script will:
- Install Chocolatey (if missing)
- Install Python 3.10, Node LTS, MariaDB, Redis, Yarn, wkhtmltopdf
- Upgrade pip

Important: This modifies system packages. Run as Administrator and review the script before running.
#>

function Write-Header($m) { Write-Host "`n== $m ==" -ForegroundColor Cyan }

if (-not ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)) {
    Write-Error "This script must be run as Administrator. Open PowerShell as Administrator and re-run."
    exit 1
}

Write-Header "Installing Chocolatey (if missing)"
if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Chocolatey..."
    Set-ExecutionPolicy Bypass -Scope Process -Force
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    refreshenv
} else {
    Write-Host "Chocolatey already installed"
}

Write-Header "Enabling global confirmation for choco"
choco feature enable -n allowGlobalConfirmation | Out-Null

Write-Header "Installing prerequisites"
# Versions can be tuned if needed
choco install -y python --version=3.10.15
choco install -y nodejs-lts
choco install -y mariadb
choco install -y redis-64
choco install -y yarn
choco install -y wkhtmltopdf

Write-Header "Upgrading pip and installing Python tooling"
python -m pip install --upgrade pip setuptools wheel

Write-Header "Completed prerequisites installation"
Write-Host "Next steps: initialize DB, create bench/site or use Docker. See README.md for commands." -ForegroundColor Green
