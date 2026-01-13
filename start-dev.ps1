# PowerShell script to always rebuild and start dev environment
# Usage: .\start-dev.ps1

Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down

Write-Host "Rebuilding Docker image (no cache)..." -ForegroundColor Green
docker-compose build --no-cache app-dev

Write-Host "Starting containers..." -ForegroundColor Green
docker-compose up app-dev
