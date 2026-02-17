@echo off
REM Batch script to always rebuild and start dev environment
REM Usage: start-dev.bat

echo Stopping existing containers...
docker-compose down

echo Rebuilding Docker image (no cache)...
docker-compose build --no-cache app-dev

echo Starting containers...
docker-compose up app-dev
