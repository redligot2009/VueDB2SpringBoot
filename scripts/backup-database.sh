#!/bin/bash

# Database Backup Script for VueDB2SpringBoot
set -e

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="photodb_backup_${DATE}.sql"

echo "Starting database backup..."

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Create database backup
echo "Creating database backup..."
docker exec db2 db2 connect to PHOTODB user db2inst1 using passw0rd
docker exec db2 db2 backup database PHOTODB to /database/backup
docker exec db2 db2 terminate

# Copy backup file from container
echo "Copying backup file..."
docker cp db2:/database/backup/PHOTODB.0.db2inst1.NODE0000.CATN0000.${DATE}*.001 $BACKUP_DIR/$BACKUP_FILE

# Compress backup
echo "Compressing backup..."
gzip $BACKUP_DIR/$BACKUP_FILE

# Remove old backups (keep last 7 days)
echo "Cleaning up old backups..."
find $BACKUP_DIR -name "photodb_backup_*.sql.gz" -mtime +7 -delete

echo "Database backup completed: $BACKUP_DIR/$BACKUP_FILE.gz"

# Optional: Upload to S3 (uncomment and configure if needed)
# echo "Uploading backup to S3..."
# aws s3 cp $BACKUP_DIR/$BACKUP_FILE.gz s3://your-backup-bucket/database-backups/
