# Local Deployment Script for VueDB2SpringBoot (PowerShell)
param(
    [switch]$SkipTests,
    [switch]$Force
)

Write-Host "Starting local deployment..." -ForegroundColor Green

# Function to check if Docker is running
function Test-DockerRunning {
    try {
        docker info 2>$null | Out-Null
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    }
}

# Function to check if Docker Compose is available
function Test-DockerCompose {
    try {
        docker-compose --version 2>$null | Out-Null
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    }
}

# Check if Docker is running
if (-not (Test-DockerRunning)) {
    Write-Host "Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Check if Docker Compose is available
if (-not (Test-DockerCompose)) {
    Write-Host "Docker Compose is not installed. Please install it first." -ForegroundColor Red
    exit 1
}

Write-Host "Docker and Docker Compose are available" -ForegroundColor Green

# Build and start services
Write-Host "Building and starting services..." -ForegroundColor Yellow
try {
    docker-compose down
    docker-compose build --no-cache
    docker-compose up -d
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start services"
    }
} catch {
    Write-Host "Failed to start services: $_" -ForegroundColor Red
    exit 1
}

# Wait for services to be ready
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check service health
Write-Host "Checking service health..." -ForegroundColor Yellow
try {
    docker-compose ps
} catch {
    Write-Host "Failed to check service status" -ForegroundColor Red
}

# Test backend health
Write-Host "Testing backend health..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "Backend is healthy" -ForegroundColor Green
    } else {
        throw "Backend returned status code: $($response.StatusCode)"
    }
} catch {
    Write-Host "Backend health check failed: $_" -ForegroundColor Red
    Write-Host "Backend logs:" -ForegroundColor Yellow
    docker-compose logs backend
    exit 1
}

# Test frontend
Write-Host "Testing frontend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "Frontend is accessible" -ForegroundColor Green
    } else {
        throw "Frontend returned status code: $($response.StatusCode)"
    }
} catch {
    Write-Host "Frontend is not accessible: $_" -ForegroundColor Red
    Write-Host "Frontend logs:" -ForegroundColor Yellow
    docker-compose logs frontend
    exit 1
}

Write-Host "Local deployment successful!" -ForegroundColor Green
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Backend API: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "Health Check: http://localhost:8080/api/health" -ForegroundColor Cyan
Write-Host "API Docs: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan