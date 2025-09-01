# Monitoring Script for VueDB2SpringBoot (PowerShell)
param(
    [switch]$Continuous,
    [int]$IntervalSeconds = 30
)

function Show-Header {
    Write-Host "📊 VueDB2SpringBoot System Monitor" -ForegroundColor Green
    Write-Host "==================================" -ForegroundColor Green
    Write-Host ""
}

function Get-DockerContainers {
    Write-Host "🐳 Docker Containers Status:" -ForegroundColor Yellow
    try {
        docker-compose ps
    } catch {
        Write-Host "❌ Failed to get Docker container status" -ForegroundColor Red
    }
    Write-Host ""
}

function Get-SystemResources {
    Write-Host "💻 System Resources:" -ForegroundColor Yellow
    
    # CPU Usage
    Write-Host "CPU Usage:" -ForegroundColor Cyan
    try {
        $cpu = Get-WmiObject -Class Win32_Processor | Measure-Object -Property LoadPercentage -Average
        Write-Host "  Average CPU: $([math]::Round($cpu.Average, 2))%" -ForegroundColor White
    } catch {
        Write-Host "  Unable to get CPU usage" -ForegroundColor Red
    }
    
    # Memory Usage
    Write-Host "Memory Usage:" -ForegroundColor Cyan
    try {
        $memory = Get-WmiObject -Class Win32_OperatingSystem
        $totalMemory = [math]::Round($memory.TotalVisibleMemorySize / 1MB, 2)
        $freeMemory = [math]::Round($memory.FreePhysicalMemory / 1MB, 2)
        $usedMemory = $totalMemory - $freeMemory
        $memoryPercent = [math]::Round(($usedMemory / $totalMemory) * 100, 2)
        
        Write-Host "  Total: ${totalMemory} GB" -ForegroundColor White
        Write-Host "  Used: ${usedMemory} GB ($memoryPercent%)" -ForegroundColor White
        Write-Host "  Free: ${freeMemory} GB" -ForegroundColor White
    } catch {
        Write-Host "  Unable to get memory usage" -ForegroundColor Red
    }
    
    # Disk Usage
    Write-Host "Disk Usage:" -ForegroundColor Cyan
    try {
        $drives = Get-WmiObject -Class Win32_LogicalDisk | Where-Object { $_.DriveType -eq 3 }
        foreach ($drive in $drives) {
            $totalGB = [math]::Round($drive.Size / 1GB, 2)
            $freeGB = [math]::Round($drive.FreeSpace / 1GB, 2)
            $usedGB = $totalGB - $freeGB
            $percentUsed = [math]::Round(($usedGB / $totalGB) * 100, 2)
            
            Write-Host "  $($drive.DeviceID) - Used: ${usedGB} GB / ${totalGB} GB ($percentUsed%)" -ForegroundColor White
        }
    } catch {
        Write-Host "  Unable to get disk usage" -ForegroundColor Red
    }
    
    Write-Host ""
}

function Test-ApplicationHealth {
    Write-Host "🏥 Application Health Checks:" -ForegroundColor Yellow
    
    # Backend health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Backend: Healthy" -ForegroundColor Green
        } else {
            Write-Host "❌ Backend: Unhealthy (Status: $($response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Backend: Unhealthy (Connection failed)" -ForegroundColor Red
    }
    
    # Frontend health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Frontend: Healthy" -ForegroundColor Green
        } else {
            Write-Host "❌ Frontend: Unhealthy (Status: $($response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Frontend: Unhealthy (Connection failed)" -ForegroundColor Red
    }
    
    # Database health
    try {
        $dbTest = docker exec db2 db2 connect to PHOTODB user db2inst1 using passw0rd 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Database: Healthy" -ForegroundColor Green
            docker exec db2 db2 terminate 2>$null | Out-Null
        } else {
            Write-Host "❌ Database: Unhealthy" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Database: Unhealthy (Connection failed)" -ForegroundColor Red
    }
    
    Write-Host ""
}

function Get-ErrorLogs {
    Write-Host "📋 Recent Error Logs:" -ForegroundColor Yellow
    
    # Backend errors
    Write-Host "Backend errors:" -ForegroundColor Cyan
    try {
        $backendLogs = docker-compose logs --tail=10 backend 2>$null | Select-String -Pattern "error|Error|ERROR" -CaseSensitive:$false
        if ($backendLogs) {
            $backendLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        } else {
            Write-Host "  No recent backend errors" -ForegroundColor Green
        }
    } catch {
        Write-Host "  Unable to get backend logs" -ForegroundColor Red
    }
    
    # Frontend errors
    Write-Host "Frontend errors:" -ForegroundColor Cyan
    try {
        $frontendLogs = docker-compose logs --tail=10 frontend 2>$null | Select-String -Pattern "error|Error|ERROR" -CaseSensitive:$false
        if ($frontendLogs) {
            $frontendLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        } else {
            Write-Host "  No recent frontend errors" -ForegroundColor Green
        }
    } catch {
        Write-Host "  Unable to get frontend logs" -ForegroundColor Red
    }
    
    # Database errors
    Write-Host "Database errors:" -ForegroundColor Cyan
    try {
        $dbLogs = docker-compose logs --tail=10 db2 2>$null | Select-String -Pattern "error|Error|ERROR" -CaseSensitive:$false
        if ($dbLogs) {
            $dbLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        } else {
            Write-Host "  No recent database errors" -ForegroundColor Green
        }
    } catch {
        Write-Host "  Unable to get database logs" -ForegroundColor Red
    }
    
    Write-Host ""
}

function Show-MonitoringComplete {
    Write-Host "📈 Monitoring complete!" -ForegroundColor Green
    Write-Host ""
}

# Main monitoring function
function Start-Monitoring {
    Show-Header
    Get-DockerContainers
    Get-SystemResources
    Test-ApplicationHealth
    Get-ErrorLogs
    Show-MonitoringComplete
}

# Check if Docker is available
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker is not available. Please install Docker Desktop." -ForegroundColor Red
    exit 1
}

# Check if docker-compose is available
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker Compose is not available. Please install Docker Compose." -ForegroundColor Red
    exit 1
}

if ($Continuous) {
    Write-Host "🔄 Starting continuous monitoring (Press Ctrl+C to stop)..." -ForegroundColor Green
    Write-Host "📊 Monitoring interval: $IntervalSeconds seconds" -ForegroundColor Yellow
    Write-Host ""
    
    try {
        while ($true) {
            Clear-Host
            Start-Monitoring
            Start-Sleep -Seconds $IntervalSeconds
        }
    } catch {
        Write-Host "`n🛑 Monitoring stopped by user" -ForegroundColor Yellow
    }
} else {
    Start-Monitoring
}
