#!/bin/bash

# Make all scripts executable
echo "Making all scripts executable..."

chmod +x scripts/setup-aws.sh
chmod +x scripts/deploy-local.sh
chmod +x scripts/backup-database.sh
chmod +x scripts/monitor.sh
chmod +x scripts/make-executable.sh

echo "All scripts are now executable!"
