# AWS Setup Script for VueDB2SpringBoot Deployment (PowerShell)
param(
    [switch]$Force
)

Write-Host "Setting up AWS infrastructure for VueDB2SpringBoot..." -ForegroundColor Green

# Function to check if a command exists
function Test-Command {
    param($Command)
    try {
        if (Get-Command $Command -ErrorAction Stop) {
            return $true
        }
    }
    catch {
        return $false
    }
}

# Check if required tools are installed
Write-Host "Checking required tools..." -ForegroundColor Yellow

$requiredTools = @("terraform", "aws", "ssh-keygen")
$missingTools = @()

foreach ($tool in $requiredTools) {
    if (-not (Test-Command $tool)) {
        $missingTools += $tool
    }
}

if ($missingTools.Count -gt 0) {
    Write-Host "Missing required tools: $($missingTools -join ', ')" -ForegroundColor Red
    Write-Host "Please install the following:" -ForegroundColor Yellow
    foreach ($tool in $missingTools) {
        switch ($tool) {
            "terraform" { Write-Host "  - Terraform: https://www.terraform.io/downloads.html" -ForegroundColor Cyan }
            "aws" { Write-Host "  - AWS CLI: https://aws.amazon.com/cli/" -ForegroundColor Cyan }
            "ssh-keygen" { Write-Host "  - OpenSSH (usually included with Windows 10/11)" -ForegroundColor Cyan }
        }
    }
    exit 1
}

Write-Host "All required tools are installed" -ForegroundColor Green

# Check if SSH key exists
$sshKeyPath = "$env:USERPROFILE\.ssh\id_rsa.pub"
if (-not (Test-Path $sshKeyPath)) {
    Write-Host "Generating SSH key pair..." -ForegroundColor Yellow
    
    # Create .ssh directory if it doesn't exist
    $sshDir = "$env:USERPROFILE\.ssh"
    if (-not (Test-Path $sshDir)) {
        New-Item -ItemType Directory -Path $sshDir -Force | Out-Null
    }
    
    # Generate SSH key
    ssh-keygen -t rsa -b 4096 -f "$env:USERPROFILE\.ssh\id_rsa" -N '""'
    Write-Host "SSH key pair generated at $sshKeyPath" -ForegroundColor Green
} else {
    Write-Host "SSH key already exists at $sshKeyPath" -ForegroundColor Green
}

# Check AWS credentials
Write-Host "Checking AWS credentials..." -ForegroundColor Yellow
try {
    $awsIdentity = aws sts get-caller-identity 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "AWS credentials configured" -ForegroundColor Green
    } else {
        throw "AWS credentials not configured"
    }
} catch {
    Write-Host "AWS credentials not configured. Please run 'aws configure' first." -ForegroundColor Red
    exit 1
}

# Create terraform.tfvars if it doesn't exist
$terraformVarsPath = "terraform\terraform.tfvars"
if (-not (Test-Path $terraformVarsPath)) {
    Write-Host "Creating terraform.tfvars from example..." -ForegroundColor Yellow
    Copy-Item "terraform\terraform.tfvars.example" $terraformVarsPath
    Write-Host "Please edit $terraformVarsPath with your actual values before running terraform apply" -ForegroundColor Yellow
} else {
    Write-Host "terraform.tfvars already exists" -ForegroundColor Green
}

Write-Host "Setup complete! Next steps:" -ForegroundColor Green
Write-Host "1. Edit terraform\terraform.tfvars with your domain and Cloudflare credentials" -ForegroundColor Cyan
Write-Host "2. Run 'terraform init' in the terraform directory" -ForegroundColor Cyan
Write-Host "3. Run 'terraform plan' to review the infrastructure" -ForegroundColor Cyan
Write-Host "4. Run 'terraform apply' to create the infrastructure" -ForegroundColor Cyan
Write-Host "5. Configure GitHub secrets for CI/CD" -ForegroundColor Cyan