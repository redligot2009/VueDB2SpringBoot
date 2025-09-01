# PowerShell script to troubleshoot AWS AMI issues
# This script helps find available Ubuntu AMIs in your region

param(
    [string]$Region = "us-east-1",
    [string]$Owner = "099720109477"
)

Write-Host "[INFO] Troubleshooting AWS AMI Issues" -ForegroundColor Cyan
Write-Host "Region: $Region" -ForegroundColor Yellow
Write-Host "Owner: $Owner (Canonical)" -ForegroundColor Yellow
Write-Host ""

# Check AWS CLI configuration
Write-Host "1. Checking AWS CLI configuration..." -ForegroundColor Green
try {
    $awsVersion = aws --version
    Write-Host "[OK] AWS CLI: $awsVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] AWS CLI not found or not configured" -ForegroundColor Red
    Write-Host "Please install and configure AWS CLI first" -ForegroundColor Yellow
    exit 1
}

# Check AWS credentials
Write-Host "2. Checking AWS credentials..." -ForegroundColor Green
try {
    $callerIdentity = aws sts get-caller-identity --region $Region
    Write-Host "[OK] AWS credentials configured" -ForegroundColor Green
    $identity = $callerIdentity | ConvertFrom-Json
    Write-Host "   Account: $($identity.Account)" -ForegroundColor Gray
    Write-Host "   User: $($identity.Arn)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] AWS credentials not configured or invalid" -ForegroundColor Red
    Write-Host "Please run 'aws configure' to set up your credentials" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Search for Ubuntu AMIs
Write-Host "3. Searching for Ubuntu AMIs..." -ForegroundColor Green

# Try different AMI name patterns
$amiPatterns = @(
    "ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*",
    "ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*",
    "ubuntu/images/hvm-ssd/ubuntu-*",
    "ubuntu/images/hvm-ssd/*"
)

foreach ($pattern in $amiPatterns) {
    Write-Host "   Trying pattern: $pattern" -ForegroundColor Gray
    
    try {
        $amiQuery = @{
            "owners" = @($Owner)
            "filters" = @(
                @{
                    "Name" = "name"
                    "Values" = @($pattern)
                },
                @{
                    "Name" = "virtualization-type"
                    "Values" = @("hvm")
                },
                @{
                    "Name" = "architecture"
                    "Values" = @("x86_64")
                }
            )
        }
        
        $amiQueryJson = $amiQuery | ConvertTo-Json -Depth 10
        $amiQueryJson | Out-File -FilePath "temp-ami-query.json" -Encoding UTF8
        
        $result = aws ec2 describe-images --cli-input-json file://temp-ami-query.json --region $Region
        Remove-Item "temp-ami-query.json" -ErrorAction SilentlyContinue
        
        if ($result) {
            $images = $result | ConvertFrom-Json
            if ($images.Images.Count -gt 0) {
                Write-Host "[OK] Found $($images.Images.Count) AMI(s) with pattern: $pattern" -ForegroundColor Green
                
                # Show the most recent 3 AMIs
                $recentImages = $images.Images | Sort-Object CreationDate -Descending | Select-Object -First 3
                
                Write-Host "   Most recent AMIs:" -ForegroundColor Yellow
                foreach ($image in $recentImages) {
                    Write-Host "   - ID: $($image.ImageId)" -ForegroundColor Cyan
                    Write-Host "     Name: $($image.Name)" -ForegroundColor White
                    Write-Host "     Created: $($image.CreationDate)" -ForegroundColor Gray
                    Write-Host ""
                }
                
                # Generate Terraform configuration
                $latestAmi = $recentImages[0]
                Write-Host "4. Recommended Terraform configuration:" -ForegroundColor Green
                Write-Host ""
                
                $terraformConfig = @'
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["'@ + $latestAmi.Name + @'"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
}
'@
                Write-Host $terraformConfig -ForegroundColor Cyan
                Write-Host ""
                
                # Alternative: Use specific AMI ID
                Write-Host "5. Alternative: Use specific AMI ID:" -ForegroundColor Green
                Write-Host ""
                
                $instanceConfig = @'
resource "aws_instance" "app" {
  ami           = "'@ + $latestAmi.ImageId + @'"
  instance_type = var.instance_type
  # ... other configuration
}
'@
                Write-Host $instanceConfig -ForegroundColor Cyan
                Write-Host ""
                
                exit 0
            }
        }
    } catch {
        Write-Host "   [ERROR] Error with pattern: $pattern" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "[ERROR] No Ubuntu AMIs found with any pattern" -ForegroundColor Red
Write-Host ""
Write-Host "Troubleshooting suggestions:" -ForegroundColor Yellow
Write-Host "1. Check if you are in the correct AWS region" -ForegroundColor White
Write-Host "2. Verify your AWS credentials have EC2 permissions" -ForegroundColor White
Write-Host "3. Try a different region (e.g., us-west-2, eu-west-1)" -ForegroundColor White
Write-Host "4. Check if the Canonical owner ID is correct for your region" -ForegroundColor White
Write-Host ""
Write-Host "To try a different region, run:" -ForegroundColor Cyan
Write-Host ".\scripts\troubleshoot-ami.ps1 -Region us-west-2" -ForegroundColor Cyan
