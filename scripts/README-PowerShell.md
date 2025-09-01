# PowerShell Scripts for VueDB2SpringBoot

This directory contains PowerShell equivalents of the bash scripts for Windows users.

## 📋 Available Scripts

### Core Scripts

| Script | Description | Usage |
|--------|-------------|-------|
| `setup-aws.ps1` | Setup AWS infrastructure and prerequisites | `.\scripts\setup-aws.ps1` |
| `deploy-local.ps1` | Deploy application locally for testing | `.\scripts\deploy-local.ps1` |
| `deploy-production.ps1` | Deploy to production server | `.\scripts\deploy-production.ps1 -ServerIP <ip>` |
| `terraform-deploy.ps1` | Manage Terraform infrastructure | `.\scripts\terraform-deploy.ps1 -Action <plan\|apply\|destroy>` |

### Utility Scripts

| Script | Description | Usage |
|--------|-------------|-------|
| `backup-database.ps1` | Backup database with compression | `.\scripts\backup-database.ps1` |
| `monitor.ps1` | Monitor system health and logs | `.\scripts\monitor.ps1` |
| `troubleshoot-ami.ps1` | Troubleshoot AWS AMI lookup issues | `.\scripts\troubleshoot-ami.ps1` |
| `make-executable.ps1` | Setup PowerShell execution policy | `.\scripts\make-executable.ps1 -SetExecutionPolicy` |

## 🚀 Quick Start

### 1. Setup PowerShell Environment

```powershell
# Run as Administrator (first time only)
.\scripts\make-executable.ps1 -SetExecutionPolicy

# Or manually set execution policy
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 2. Setup AWS Infrastructure

```powershell
# Setup AWS prerequisites
.\scripts\setup-aws.ps1

# Deploy infrastructure
.\scripts\terraform-deploy.ps1 -Action apply
```

### 3. Deploy Application

```powershell
# Deploy locally for testing
.\scripts\deploy-local.ps1

# Deploy to production
.\scripts\deploy-production.ps1 -ServerIP 1.2.3.4 -DomainName app.example.com
```

## 📖 Detailed Usage

### setup-aws.ps1

Sets up AWS infrastructure prerequisites and validates configuration.

```powershell
.\scripts\setup-aws.ps1
```

**Features:**
- Checks for required tools (Terraform, AWS CLI, SSH)
- Generates SSH key pair if needed
- Validates AWS credentials
- Creates terraform.tfvars from example

### deploy-local.ps1

Deploys the application locally using Docker Compose.

```powershell
.\scripts\deploy-local.ps1 [-SkipTests] [-Force]
```

**Parameters:**
- `-SkipTests`: Skip running tests before deployment
- `-Force`: Force deployment even if checks fail

**Features:**
- Validates Docker and Docker Compose
- Builds and starts all services
- Performs health checks
- Shows service URLs

### deploy-production.ps1

Deploys the application to a production server.

```powershell
.\scripts\deploy-production.ps1 -ServerIP <ip> [-Username ubuntu] [-ProjectName vuedb2springboot] [-DomainName <domain>] [-SkipSSL] [-Force]
```

**Parameters:**
- `-ServerIP` (Required): IP address of the production server
- `-Username`: SSH username (default: ubuntu)
- `-ProjectName`: Project name (default: vuedb2springboot)
- `-DomainName`: Domain name for SSL setup
- `-SkipSSL`: Skip SSL certificate setup
- `-Force`: Force deployment

**Features:**
- Tests SSH connectivity
- Copies application files
- Deploys on remote server
- Sets up SSL certificates
- Performs health checks

### terraform-deploy.ps1

Manages Terraform infrastructure deployment.

```powershell
.\scripts\terraform-deploy.ps1 -Action <action> [-TerraformDir terraform] [-AutoApprove] [-Destroy]
```

**Parameters:**
- `-Action`: Action to perform (plan, apply, destroy, output)
- `-TerraformDir`: Terraform directory (default: terraform)
- `-AutoApprove`: Auto-approve without prompts
- `-Destroy`: Destroy infrastructure (alternative to -Action destroy)

**Examples:**
```powershell
# Plan infrastructure changes
.\scripts\terraform-deploy.ps1 -Action plan

# Apply infrastructure
.\scripts\terraform-deploy.ps1 -Action apply

# Apply with auto-approval
.\scripts\terraform-deploy.ps1 -Action apply -AutoApprove

# Destroy infrastructure
.\scripts\terraform-deploy.ps1 -Action destroy

# Show outputs
.\scripts\terraform-deploy.ps1 -Action output
```

### backup-database.ps1

Creates compressed database backups.

```powershell
.\scripts\backup-database.ps1 [-BackupDir C:\backups] [-UploadToS3] [-S3Bucket <bucket>]
```

**Parameters:**
- `-BackupDir`: Backup directory (default: C:\backups)
- `-UploadToS3`: Upload backup to S3
- `-S3Bucket`: S3 bucket name for upload

**Features:**
- Creates DB2 database backup
- Compresses backup files
- Cleans up old backups (7+ days)
- Optional S3 upload

### monitor.ps1

Monitors system health and application status.

```powershell
.\scripts\monitor.ps1 [-Continuous] [-IntervalSeconds 30]
```

**Parameters:**
- `-Continuous`: Run continuous monitoring
- `-IntervalSeconds`: Monitoring interval in seconds (default: 30)

**Features:**
- Docker container status
- System resource usage (CPU, Memory, Disk)
- Application health checks
- Error log analysis
- Continuous monitoring mode

## 🔧 Configuration

### Prerequisites

1. **PowerShell 5.1+** or **PowerShell Core 6+**
2. **Docker Desktop** for Windows
3. **Terraform** (>= 1.0)
4. **AWS CLI** (configured)
5. **OpenSSH** (usually included with Windows 10/11)

### Environment Setup

1. **Execution Policy**: Set to RemoteSigned or Unrestricted
2. **SSH Keys**: Generate with `ssh-keygen -t rsa -b 4096`
3. **AWS Credentials**: Configure with `aws configure`
4. **Terraform Variables**: Copy and edit `terraform.tfvars`

### File Structure

```
scripts/
├── setup-aws.ps1           # AWS setup
├── deploy-local.ps1        # Local deployment
├── deploy-production.ps1   # Production deployment
├── terraform-deploy.ps1    # Terraform management
├── backup-database.ps1     # Database backup
├── monitor.ps1             # System monitoring
├── troubleshoot-ami.ps1    # AMI troubleshooting
├── make-executable.ps1     # Setup script
└── README-PowerShell.md    # This file
```

## 🚨 Troubleshooting

### Common Issues

1. **Execution Policy Error**
   ```
   Solution: Run .\scripts\make-executable.ps1 -SetExecutionPolicy
   ```

2. **Docker Not Running**
   ```
   Solution: Start Docker Desktop and wait for it to be ready
   ```

3. **SSH Connection Failed**
   ```
   Solution: Check SSH key exists and server is accessible
   ```

4. **Terraform Not Found**
   ```
   Solution: Install Terraform and add to PATH
   ```

### Debug Commands

```powershell
# Check PowerShell version
$PSVersionTable.PSVersion

# Check execution policy
Get-ExecutionPolicy

# Check Docker status
docker info

# Check AWS credentials
aws sts get-caller-identity

# Check Terraform version
terraform version
```

## 🔄 Workflow Examples

### Complete Deployment Workflow

```powershell
# 1. Setup environment
.\scripts\make-executable.ps1 -SetExecutionPolicy
.\scripts\setup-aws.ps1

# 2. Deploy infrastructure
.\scripts\terraform-deploy.ps1 -Action apply

# 3. Get server IP from Terraform output
.\scripts\terraform-deploy.ps1 -Action output

# 4. Deploy application
.\scripts\deploy-production.ps1 -ServerIP <ip> -DomainName app.example.com

# 5. Monitor deployment
.\scripts\monitor.ps1 -Continuous
```

### Local Development Workflow

```powershell
# 1. Deploy locally
.\scripts\deploy-local.ps1

# 2. Monitor locally
.\scripts\monitor.ps1

# 3. Backup database
.\scripts\backup-database.ps1
```

## 📚 Additional Resources

- [PowerShell Documentation](https://docs.microsoft.com/en-us/powershell/)
- [Docker Desktop for Windows](https://docs.docker.com/desktop/windows/)
- [Terraform Documentation](https://www.terraform.io/docs/)
- [AWS CLI Documentation](https://docs.aws.amazon.com/cli/)
