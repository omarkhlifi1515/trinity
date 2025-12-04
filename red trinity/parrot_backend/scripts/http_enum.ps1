# HTTP Enumeration Script (Windows)
# Usage: .\http_enum.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting HTTP Enumeration on $Target..." -ForegroundColor Green

# Handle different target formats
$Url = $Target

# If target contains port (e.g., 192.168.0.188:5000), extract it
if ($Target -match '^(.+):(\d+)$') {
    $hostPart = $matches[1]
    $portPart = $matches[2]
    
    # If no protocol, add http://
    if (-not ($hostPart -match '^https?://')) {
        $Url = "http://${hostPart}:${portPart}"
    } else {
        $Url = "${hostPart}:${portPart}"
    }
} elseif (-not ($Url -match '^https?://')) {
    # Add http:// if no protocol specified
    $Url = "http://$Url"
}

try {
    # Get HTTP headers
    Write-Host "`nHTTP Headers:" -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri $Url -Method Head -ErrorAction Stop
    $response.Headers | Format-List
    
    # Get server info
    Write-Host "`nServer Information:" -ForegroundColor Cyan
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Server: $($response.Headers.Server)"
    Write-Host "Content-Type: $($response.Headers.'Content-Type')"
    
    # Check common paths
    Write-Host "`nChecking common paths..." -ForegroundColor Cyan
    $commonPaths = @("/robots.txt", "/sitemap.xml", "/.git", "/admin", "/login")
    
    foreach ($path in $commonPaths) {
        try {
            $testUrl = $Url.TrimEnd('/') + $path
            $testResponse = Invoke-WebRequest -Uri $testUrl -Method Head -TimeoutSec 2 -ErrorAction Stop
            Write-Host "$path - EXISTS (Status: $($testResponse.StatusCode))" -ForegroundColor Green
        } catch {
            Write-Host "$path - NOT FOUND" -ForegroundColor Red
        }
    }
    
    exit 0
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

