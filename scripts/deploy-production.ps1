# Production Deployment Script for VueDB2SpringBoot
# This script deploys the application to AWS EC2 with proper configuration
param(
    [Parameter(Mandatory=$true)]
    [string]$ServerIP,
    
    [Parameter(Mandatory=$false)]
    [string]$DomainName
)

$Username = "ubuntu"
$ProjectName = "vuedb2springboot"

Write-Host "[INFO] Starting production deployment..." -ForegroundColor Cyan
Write-Host "Server IP: $ServerIP" -ForegroundColor Yellow
Write-Host "Domain: $DomainName" -ForegroundColor Yellow
Write-Host ""

# Check if SSH key exists
$sshKeyPath = "$env:USERPROFILE\.ssh\id_rsa"
if (-not (Test-Path $sshKeyPath)) {
    Write-Host "[ERROR] SSH private key not found at $sshKeyPath" -ForegroundColor Red
    Write-Host "[INFO] Please generate SSH keys first using: ssh-keygen -t rsa -b 4096" -ForegroundColor Yellow
    exit 1
}

# Test SSH connection
Write-Host "[INFO] Testing SSH connection..." -ForegroundColor Green
try {
    $sshTestCommand = "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no ${Username}@${ServerIP} 'echo SSH connection successful'"
    $sshTest = Invoke-Expression $sshTestCommand
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] SSH connection successful" -ForegroundColor Green
    } else {
        throw "SSH connection failed"
    }
} catch {
    Write-Host "[ERROR] SSH connection failed: $_" -ForegroundColor Red
    Write-Host "[INFO] Make sure the server is running and SSH key is properly configured" -ForegroundColor Yellow
    exit 1
}

# Create deployment archive
Write-Host "[INFO] Creating deployment archive..." -ForegroundColor Green
$tempArchive = "deployment-$(Get-Date -Format 'yyyyMMdd-HHmmss').zip"
$excludeItems = @("node_modules", ".git", "terraform", "*.log", "*.tmp", "backups", "scripts")

try {
    # Get items to archive, excluding specified items
    $itemsToArchive = Get-ChildItem -Path . -Exclude $excludeItems
    Compress-Archive -Path $itemsToArchive -DestinationPath $tempArchive -Force
    Write-Host "[OK] Archive created: $tempArchive" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to create archive: $_" -ForegroundColor Red
    exit 1
}

# Copy files to server
Write-Host "[INFO] Copying files to server..." -ForegroundColor Green
try {
    # Copy archive to server
    $scpCommand = "scp -o StrictHostKeyChecking=no $tempArchive ${Username}@${ServerIP}:/tmp/"
    Invoke-Expression $scpCommand
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Files copied to server" -ForegroundColor Green
    } else {
        throw "Failed to copy files to server"
    }
} catch {
    Write-Host "[ERROR] Failed to copy files: $_" -ForegroundColor Red
    Remove-Item $tempArchive -Force -ErrorAction SilentlyContinue
    exit 1
}

# Create deployment script with proper Unix line endings
Write-Host "[INFO] Creating deployment script..." -ForegroundColor Green
$deployScript = @"
#!/bin/bash
set -e

echo "Starting deployment..."

# Create project directory if it doesn't exist
sudo mkdir -p /opt/$ProjectName
cd /opt/$ProjectName

# Clean existing files (except .env and docker-compose.prod.yml)
sudo find . -maxdepth 1 -type f ! -name '.env' ! -name 'docker-compose.prod.yml' -delete
sudo find . -maxdepth 1 -type d ! -name '.' ! -name '..' -exec rm -rf {} + 2>/dev/null || true

# Extract new files
sudo unzip -o /tmp/$tempArchive -d .

# Set proper ownership
sudo chown -R ubuntu:ubuntu /opt/$ProjectName

# Clean up archive
rm /tmp/$tempArchive

# Stop existing containers
echo "Stopping existing containers..."
sudo docker compose -f docker-compose.prod.yml down || true

# Ensure domain_name is set in .env
if ! grep -q "domain_name=" .env; then
    echo "domain_name=$DomainName" >> .env
fi

# Build and start new containers
echo "Building and starting containers..."
sudo docker compose -f docker-compose.prod.yml up -d --build

echo "Deployment completed successfully!"
"@

try {
    # Save deploy script to temporary file with Unix line endings
    $deployScriptFile = "deploy-script-$(Get-Date -Format 'yyyyMMdd-HHmmss').sh"
    $deployScript | Out-File -FilePath $deployScriptFile -Encoding UTF8 -NoNewline
    
    # Convert to Unix line endings
    $content = Get-Content $deployScriptFile -Raw
    $content = $content -replace "`r`n", "`n"
    $content | Out-File -FilePath $deployScriptFile -Encoding UTF8 -NoNewline
    
    # Copy and execute deploy script
    $scpCommand2 = "scp -o StrictHostKeyChecking=no $deployScriptFile ${Username}@${ServerIP}:/tmp/"
    Invoke-Expression $scpCommand2
    
    $sshCommand = "ssh -o StrictHostKeyChecking=no ${Username}@${ServerIP} 'chmod +x /tmp/$deployScriptFile && /tmp/$deployScriptFile'"
    Invoke-Expression $sshCommand
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Application deployed successfully on server" -ForegroundColor Green
    } else {
        throw "Deployment script failed on server"
    }
    
    # Clean up local files
    Remove-Item $deployScriptFile -Force -ErrorAction SilentlyContinue
    
} catch {
    Write-Host "[ERROR] Failed to deploy on server: $_" -ForegroundColor Red
    Remove-Item $tempArchive -Force -ErrorAction SilentlyContinue
    exit 1
}

# Clean up local archive
Remove-Item $tempArchive -Force -ErrorAction SilentlyContinue

# Wait for application to start
Write-Host "[INFO] Waiting for application to start..." -ForegroundColor Green
Start-Sleep -Seconds 60

# Check if containers are running
Write-Host "[INFO] Checking container status..." -ForegroundColor Green
try {
    $containerCheck = "ssh -o StrictHostKeyChecking=no ${Username}@${ServerIP} 'cd /opt/$ProjectName && sudo docker compose -f docker-compose.prod.yml ps'"
    Invoke-Expression $containerCheck
} catch {
    Write-Host "[WARNING] Could not check container status" -ForegroundColor Yellow
}

# Perform health check
Write-Host "[INFO] Performing health check..." -ForegroundColor Green
$healthCheckPassed = $false

for ($i = 1; $i -le 10; $i++) {
    try {
        if ($DomainName) {
            $healthUrl = "https://$DomainName/health"
        } else {
            $healthUrl = "http://$ServerIP:8080/health"
        }
        
        $response = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 10
        
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] Health check passed!" -ForegroundColor Green
            $healthCheckPassed = $true
            break
        }
    } catch {
        Write-Host "Health check attempt $i failed, retrying in 30 seconds..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
    }
}

if (-not $healthCheckPassed) {
    Write-Host "[WARNING] Health check failed, but deployment completed" -ForegroundColor Yellow
    Write-Host "[INFO] You can check the logs with: ssh $Username@$ServerIP 'cd /opt/$ProjectName && sudo docker compose -f docker-compose.prod.yml logs'" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "[SUCCESS] Production deployment completed successfully!" -ForegroundColor Green
Write-Host ""

if ($DomainName) {
    Write-Host "[INFO] Application is available at: https://$DomainName" -ForegroundColor Cyan
} else {
    Write-Host "[INFO] Application is available at: http://$ServerIP:8080" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "You can SSH into the server with:" -ForegroundColor Gray
Write-Host "ssh $Username@$ServerIP" -ForegroundColor Gray
Write-Host ""
Write-Host "To check logs:" -ForegroundColor Gray
Write-Host "ssh $Username@$ServerIP 'cd /opt/$ProjectName && sudo docker compose -f docker-compose.prod.yml logs'" -ForegroundColor Gray
