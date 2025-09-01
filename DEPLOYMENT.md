# VueDB2SpringBoot Deployment Guide

This guide covers deploying the VueDB2SpringBoot application to AWS EC2 using Terraform and Cloudflare DNS, with automated CI/CD via GitHub Actions.

## 📋 Prerequisites

### Required Tools
- [Terraform](https://www.terraform.io/downloads.html) (>= 1.0)
- [AWS CLI](https://aws.amazon.com/cli/) (configured with credentials)
- [Docker](https://www.docker.com/get-started) and Docker Compose
- [Git](https://git-scm.com/downloads)
- SSH key pair

### Required Accounts
- AWS Account with appropriate permissions
- Cloudflare Account with API access
- GitHub Account with repository access

## 🚀 Quick Start

### Linux/macOS (Bash)

#### 1. Initial Setup

```bash
# Clone the repository
git clone <your-repo-url>
cd VueDB2SpringBoot

# Make setup script executable and run it
chmod +x scripts/setup-aws.sh
./scripts/setup-aws.sh
```

### Windows (PowerShell)

#### 1. Initial Setup

```powershell
# Clone the repository
git clone <your-repo-url>
cd VueDB2SpringBoot

# Setup PowerShell execution policy (first time only)
.\scripts\make-executable.ps1 -SetExecutionPolicy

# Run AWS setup script
.\scripts\setup-aws.ps1
```

### 2. Configure Terraform Variables

#### Linux/macOS:
```bash
cp terraform/terraform.tfvars.example terraform/terraform.tfvars
```

#### Windows:
```powershell
Copy-Item terraform\terraform.tfvars.example terraform\terraform.tfvars
```

Edit `terraform/terraform.tfvars` with your actual values:

```hcl
# AWS Configuration
aws_region     = "us-east-1"
project_name   = "vuedb2springboot"
instance_type  = "t3.medium"

# Domain Configuration
domain_name = "app.yourdomain.com"  # Replace with your actual domain

# SSH Configuration
ssh_public_key_path = "~/.ssh/id_rsa.pub"

# Cloudflare Configuration
cloudflare_api_token = "your_cloudflare_api_token_here"
cloudflare_zone_id   = "your_cloudflare_zone_id_here"
```

### 3. Get Cloudflare Credentials

1. **API Token**: Go to Cloudflare Dashboard → My Profile → API Tokens → Create Token
   - Use "Custom token" template
   - Permissions: Zone:Zone:Read, Zone:DNS:Edit
   - Zone Resources: Include specific zone (your domain)

2. **Zone ID**: Go to Cloudflare Dashboard → Select your domain → Right sidebar shows Zone ID

### 4. Deploy Infrastructure

#### Linux/macOS:
```bash
cd terraform

# Initialize Terraform
terraform init

# Review the plan
terraform plan

# Apply the infrastructure
terraform apply
```

#### Windows:
```powershell
# Use the PowerShell deployment script
.\scripts\terraform-deploy.ps1 -Action plan

# Apply the infrastructure
.\scripts\terraform-deploy.ps1 -Action apply

# Or manually:
cd terraform
terraform init
terraform plan
terraform apply
```

### 5. Generate SSH Keys

You need to generate an SSH key pair for secure access to your EC2 instance.

#### Linux/macOS (Bash)

1. **Check if you already have SSH keys:**
   ```bash
   ls -la ~/.ssh/id_rsa
   ```

2. **If you don't have keys, generate them:**
   ```bash
   ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
   ```

3. **View your public key (to add to Terraform):**
   ```bash
   cat ~/.ssh/id_rsa.pub
   ```

4. **View your private key (for GitHub Secrets):**
   ```bash
   cat ~/.ssh/id_rsa
   ```

#### Windows (PowerShell)

1. **Check if you already have SSH keys:**
   ```powershell
   Test-Path "$env:USERPROFILE\.ssh\id_rsa"
   ```

2. **If you don't have keys, generate them:**
   ```powershell
   ssh-keygen -t rsa -b 4096 -f "$env:USERPROFILE\.ssh\id_rsa" -N '""'
   ```

3. **View your public key (to add to Terraform):**
   ```powershell
   Get-Content "$env:USERPROFILE\.ssh\id_rsa.pub"
   ```

4. **View your private key (for GitHub Secrets):**
   ```powershell
   Get-Content "$env:USERPROFILE\.ssh\id_rsa"
   ```

#### Quick Setup with Scripts

Our setup scripts can generate SSH keys automatically:

**Linux/macOS:**
```bash
./scripts/setup-aws.sh
```

**Windows:**
```powershell
.\scripts\setup-aws.ps1
```

### 6. Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions → **Repository secrets**, and add:

- `AWS_ACCESS_KEY_ID`: Your AWS access key
- `AWS_SECRET_ACCESS_KEY`: Your AWS secret key
- `CLOUDFLARE_API_TOKEN`: Your Cloudflare API token
- `CLOUDFLARE_ZONE_ID`: Your Cloudflare Zone ID
- `DOMAIN_NAME`: Your domain name (e.g., app.yourdomain.com)
- `SSH_PRIVATE_KEY`: Your SSH private key content (entire private key including `-----BEGIN OPENSSH PRIVATE KEY-----` and `-----END OPENSSH PRIVATE KEY-----` lines)

#### Adding SSH_PRIVATE_KEY to GitHub Secrets

1. **Copy your private key content** (from step 5 above)
2. **Go to GitHub repository → Settings → Secrets and variables → Actions**
3. **Click on "Repository secrets" tab** (not "Environment secrets")
4. **Click "New repository secret"**
5. **Name**: `SSH_PRIVATE_KEY`
6. **Value**: Paste the entire private key content
7. **Click "Add secret"**

#### Important Security Notes

- **Never share your private key** - it's like a password
- **Only add the private key to GitHub Secrets** - never commit it to code
- **The public key** (`.pub` file) is safe to share and goes on the EC2 instance
- **Keep your private key secure** - it gives access to your EC2 instance

#### Verifying SSH Setup

After deployment, you can test SSH access:

```bash
# Test SSH connection (replace with your actual EC2 IP)
ssh ubuntu@<your-ec2-ip>
```

If it works without asking for a password, your SSH key setup is correct!

**To get your EC2 IP:**
```bash
# Linux/macOS
cd terraform && terraform output

# Windows
.\scripts\terraform-deploy.ps1 -Action output
```

## 🔄 CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/deploy.yml`) automatically:

1. **On Pull Request**: Runs tests for both backend and frontend
2. **On Push to Main**: 
   - Runs tests
   - Deploys infrastructure with Terraform
   - Deploys application to EC2
   - Sets up SSL certificate
   - Performs health checks

### Manual Deployment

#### Linux/macOS:
```bash
# Deploy locally for testing
chmod +x scripts/deploy-local.sh
./scripts/deploy-local.sh

# Deploy to production (after infrastructure is set up)
ssh ubuntu@<your-ec2-ip>
cd /opt/vuedb2springboot
./deploy.sh
```

#### Windows:
```powershell
# Deploy locally for testing
.\scripts\deploy-local.ps1

# Deploy to production (after infrastructure is set up)
.\scripts\deploy-production.ps1 -ServerIP <your-ec2-ip> -DomainName <your-domain>

# Or SSH manually:
ssh ubuntu@<your-ec2-ip>
cd /opt/vuedb2springboot
./deploy.sh
```

## 🏗️ Infrastructure Overview

### AWS Resources Created
- **VPC**: Custom VPC with public subnet
- **EC2 Instance**: Ubuntu 22.04 LTS (t3.medium by default)
- **Security Group**: Configured for HTTP, HTTPS, SSH, and application ports
- **Elastic IP**: Static IP address for the instance
- **Internet Gateway**: For internet access

### Cloudflare Integration
- **DNS Record**: A record pointing your domain to the EC2 instance
- **Proxy**: Enabled for SSL termination and performance optimization

### Application Stack
- **Frontend**: Vue.js application served by Nginx
- **Backend**: Spring Boot application
- **Database**: IBM DB2 Community Edition
- **Reverse Proxy**: Nginx with SSL termination
- **SSL**: Let's Encrypt certificates via Certbot

## 📊 Monitoring and Maintenance

### Health Monitoring

#### Linux/macOS:
```bash
# Run the monitoring script
chmod +x scripts/monitor.sh
./scripts/monitor.sh
```

#### Windows:
```powershell
# Run the monitoring script
.\scripts\monitor.ps1

# For continuous monitoring
.\scripts\monitor.ps1 -Continuous
```

### Database Backup

#### Linux/macOS:
```bash
# Create database backup
chmod +x scripts/backup-database.sh
./scripts/backup-database.sh
```

#### Windows:
```powershell
# Create database backup
.\scripts\backup-database.ps1

# With S3 upload
.\scripts\backup-database.ps1 -UploadToS3 -S3Bucket your-bucket-name
```

### Log Access

```bash
# View application logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db2
```

## 🔧 Configuration

### Environment Variables

The application uses the following environment variables:

**Backend:**
- `SPRING_PROFILES_ACTIVE=db2`
- `DB_URL=jdbc:db2://db2:50000/PHOTODB`
- `DB_USER=db2inst1`
- `DB_PASS=passw0rd`
- `JWT_SECRET`: JWT signing secret
- `JWT_EXPIRATION`: JWT expiration time in milliseconds

**Frontend:**
- `VITE_API_URL`: Backend API URL

### SSL Configuration

SSL certificates are automatically managed by Certbot and renewed automatically. The Nginx configuration includes:

- HTTP to HTTPS redirect
- SSL termination
- Security headers
- Reverse proxy to application services

## 🚨 Troubleshooting

### Common Issues

1. **Terraform Apply Fails**
   - Check AWS credentials: `aws sts get-caller-identity`
   - Verify Cloudflare credentials
   - Ensure domain is properly configured in Cloudflare

2. **Application Won't Start**
   - Check Docker logs: `docker-compose logs`
   - Verify environment variables
   - Check database connectivity

3. **SSL Certificate Issues**
   - Ensure domain points to EC2 instance
   - Check Cloudflare proxy settings
   - Verify DNS propagation

4. **Database Connection Issues**
   - Check DB2 container logs
   - Verify database credentials
   - Ensure database is fully initialized

### Useful Commands

```bash
# Check infrastructure status
terraform show

# View EC2 instance details
aws ec2 describe-instances --filters "Name=tag:Name,Values=vuedb2springboot-web-server"

# SSH into EC2 instance
ssh ubuntu@<your-ec2-ip>

# Restart services
docker-compose restart

# Update application
git pull && ./deploy.sh
```

## 🔒 Security Considerations

1. **SSH Access**: Only allow SSH from trusted IPs in production
2. **Database**: Use strong passwords and consider RDS for production
3. **SSL**: Certificates are automatically renewed
4. **Firewall**: Security groups restrict access to necessary ports only
5. **Secrets**: Store sensitive data in GitHub Secrets, not in code

## 📈 Scaling

For production scaling, consider:

1. **Load Balancer**: Use AWS Application Load Balancer
2. **Database**: Migrate to AWS RDS for DB2
3. **Container Orchestration**: Use ECS or EKS
4. **CDN**: Leverage Cloudflare's CDN features
5. **Monitoring**: Add CloudWatch or third-party monitoring

## 🪟 Windows-Specific Instructions

### PowerShell Prerequisites

1. **PowerShell Version**: Ensure you have PowerShell 5.1+ or PowerShell Core 6+
2. **Execution Policy**: Set to `RemoteSigned` or `Unrestricted`
3. **Docker Desktop**: Install and start Docker Desktop for Windows
4. **OpenSSH**: Usually included with Windows 10/11

### Windows Setup Commands

```powershell
# Check PowerShell version
$PSVersionTable.PSVersion

# Set execution policy (run as Administrator if needed)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Check if Docker is running
docker info

# Verify AWS CLI
aws --version
aws sts get-caller-identity
```

### Windows Deployment Workflow

```powershell
# 1. Setup environment
.\scripts\make-executable.ps1 -SetExecutionPolicy

# 2. Setup AWS infrastructure
.\scripts\setup-aws.ps1

# 3. Deploy infrastructure
.\scripts\terraform-deploy.ps1 -Action apply

# 4. Get server IP from Terraform output
.\scripts\terraform-deploy.ps1 -Action output

# 5. Deploy application
.\scripts\deploy-production.ps1 -ServerIP <ip> -DomainName <domain>

# 6. Monitor deployment
.\scripts\monitor.ps1 -Continuous
```

### Windows Troubleshooting

#### Common Issues:

1. **Execution Policy Error**
   ```powershell
   # Solution: Set execution policy
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

2. **Docker Not Running**
   ```powershell
   # Solution: Start Docker Desktop and wait for it to be ready
   docker info
   ```

3. **SSH Connection Failed**
   ```powershell
   # Solution: Check SSH key exists
   Test-Path "$env:USERPROFILE\.ssh\id_rsa"
   
   # Generate SSH key if needed
   ssh-keygen -t rsa -b 4096 -f "$env:USERPROFILE\.ssh\id_rsa"
   ```

4. **Terraform Not Found**
   ```powershell
   # Solution: Install Terraform and add to PATH
   terraform version
   ```

5. **AWS AMI Not Found Error**
   ```powershell
   # Solution: Run the AMI troubleshooting script
   .\scripts\troubleshoot-ami.ps1
   
   # Or try a different region
   .\scripts\troubleshoot-ami.ps1 -Region us-west-2
   ```

### Windows File Paths

- **SSH Keys**: `%USERPROFILE%\.ssh\`
- **AWS Credentials**: `%USERPROFILE%\.aws\`
- **Terraform Files**: `terraform\` (use backslashes)
- **Backup Directory**: `C:\backups\` (default)

### Windows Scripts Reference

| Script | Purpose | Usage |
|--------|---------|-------|
| `setup-aws.ps1` | AWS setup | `.\scripts\setup-aws.ps1` |
| `deploy-local.ps1` | Local deployment | `.\scripts\deploy-local.ps1` |
| `deploy-production.ps1` | Production deployment | `.\scripts\deploy-production.ps1 -ServerIP <ip>` |
| `terraform-deploy.ps1` | Terraform management | `.\scripts\terraform-deploy.ps1 -Action apply` |
| `backup-database.ps1` | Database backup | `.\scripts\backup-database.ps1` |
| `monitor.ps1` | System monitoring | `.\scripts\monitor.ps1 -Continuous` |
| `troubleshoot-ami.ps1` | AMI troubleshooting | `.\scripts\troubleshoot-ami.ps1` |

## 🆘 Support

If you encounter issues:

1. Check the logs using the monitoring script
2. Review the GitHub Actions workflow logs
3. Verify all environment variables and secrets
4. Ensure all prerequisites are met

For additional help, refer to:
- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Cloudflare Terraform Provider](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
