# Database Backup Script for VueDB2SpringBoot (PowerShell)
param(
    [string]$BackupDir = "C:\backups",
    [switch]$UploadToS3,
    [string]$S3Bucket = ""
)

$Date = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = "photodb_backup_$Date.sql"

Write-Host "🗄️  Starting database backup..." -ForegroundColor Green

# Create backup directory if it doesn't exist
if (-not (Test-Path $BackupDir)) {
    Write-Host "📁 Creating backup directory: $BackupDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
}

# Check if DB2 container is running
try {
    $db2Status = docker ps --filter "name=db2" --format "table {{.Names}}\t{{.Status}}"
    if ($db2Status -notmatch "db2") {
        throw "DB2 container is not running"
    }
} catch {
    Write-Host "❌ DB2 container is not running. Please start the application first." -ForegroundColor Red
    exit 1
}

# Create database backup
Write-Host "📦 Creating database backup..." -ForegroundColor Yellow
try {
    # Connect to database and create backup
    docker exec db2 db2 connect to PHOTODB user db2inst1 using passw0rd
    docker exec db2 db2 backup database PHOTODB to /database/backup
    docker exec db2 db2 terminate
    
    if ($LASTEXITCODE -ne 0) {
        throw "Database backup failed"
    }
} catch {
    Write-Host "❌ Failed to create database backup: $_" -ForegroundColor Red
    exit 1
}

# Find the backup file in the container
Write-Host "📋 Copying backup file..." -ForegroundColor Yellow
try {
    # Get the backup file name from the container
    $backupFileName = docker exec db2 ls /database/backup/ | Where-Object { $_ -match "PHOTODB.*\.001$" } | Select-Object -First 1
    
    if (-not $backupFileName) {
        throw "Backup file not found in container"
    }
    
    # Copy backup file from container
    docker cp "db2:/database/backup/$backupFileName" "$BackupDir\$BackupFile"
    
    if (-not (Test-Path "$BackupDir\$BackupFile")) {
        throw "Failed to copy backup file"
    }
} catch {
    Write-Host "❌ Failed to copy backup file: $_" -ForegroundColor Red
    exit 1
}

# Compress backup
Write-Host "🗜️  Compressing backup..." -ForegroundColor Yellow
try {
    # Use PowerShell's built-in compression
    Compress-Archive -Path "$BackupDir\$BackupFile" -DestinationPath "$BackupDir\$BackupFile.zip" -Force
    Remove-Item "$BackupDir\$BackupFile" -Force
    
    $compressedFile = "$BackupDir\$BackupFile.zip"
    Write-Host "✅ Backup compressed: $compressedFile" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to compress backup: $_" -ForegroundColor Red
    exit 1
}

# Remove old backups (keep last 7 days)
Write-Host "🧹 Cleaning up old backups..." -ForegroundColor Yellow
try {
    $cutoffDate = (Get-Date).AddDays(-7)
    $oldBackups = Get-ChildItem -Path $BackupDir -Filter "photodb_backup_*.zip" | Where-Object { $_.LastWriteTime -lt $cutoffDate }
    
    foreach ($oldBackup in $oldBackups) {
        Remove-Item $oldBackup.FullName -Force
        Write-Host "🗑️  Removed old backup: $($oldBackup.Name)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Warning: Failed to clean up old backups: $_" -ForegroundColor Yellow
}

Write-Host "✅ Database backup completed: $compressedFile" -ForegroundColor Green

# Optional: Upload to S3
if ($UploadToS3 -and $S3Bucket) {
    Write-Host "☁️  Uploading backup to S3..." -ForegroundColor Yellow
    try {
        # Check if AWS CLI is available
        if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
            throw "AWS CLI is not installed"
        }
        
        $s3Key = "database-backups/$BackupFile.zip"
        aws s3 cp $compressedFile "s3://$S3Bucket/$s3Key"
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Backup uploaded to S3: s3://$S3Bucket/$s3Key" -ForegroundColor Green
        } else {
            throw "S3 upload failed"
        }
    } catch {
        Write-Host "❌ Failed to upload to S3: $_" -ForegroundColor Red
        Write-Host "💡 Make sure AWS CLI is configured and S3 bucket exists" -ForegroundColor Yellow
    }
} elseif ($UploadToS3 -and -not $S3Bucket) {
    Write-Host "⚠️  S3 upload requested but no bucket specified. Use -S3Bucket parameter." -ForegroundColor Yellow
}

Write-Host "🎉 Backup process completed successfully!" -ForegroundColor Green
