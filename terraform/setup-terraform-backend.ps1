# PowerShell script to set up S3 backend for Terraform state
# This creates the S3 bucket and configures it for Terraform state storage

param(
    [Parameter(Mandatory=$true)]
    [string]$BucketName,
    
    [string]$Region = "us-east-1"
)

Write-Host "Setting up Terraform S3 backend..." -ForegroundColor Cyan
Write-Host "Bucket Name: $BucketName" -ForegroundColor Yellow
Write-Host "Region: $Region" -ForegroundColor Yellow

try {
    # Check if bucket already exists
    Write-Host "Checking if bucket exists..." -ForegroundColor Cyan
    $bucketExists = aws s3api head-bucket --bucket $BucketName 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Bucket '$BucketName' already exists" -ForegroundColor Green
    } else {
        Write-Host "Creating S3 bucket: $BucketName" -ForegroundColor Yellow
        
        # Create the bucket
        if ($Region -eq "us-east-1") {
            # us-east-1 doesn't need LocationConstraint
            aws s3api create-bucket --bucket $BucketName --region $Region
        } else {
            # Other regions need LocationConstraint
            $locationConstraint = @{LocationConstraint = $Region} | ConvertTo-Json
            aws s3api create-bucket --bucket $BucketName --region $Region --create-bucket-configuration $locationConstraint
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Bucket created successfully!" -ForegroundColor Green
        } else {
            Write-Host "Failed to create bucket" -ForegroundColor Red
            exit 1
        }
    }
    
    # Enable versioning
    Write-Host "Enabling versioning..." -ForegroundColor Cyan
    aws s3api put-bucket-versioning --bucket $BucketName --versioning-configuration Status=Enabled
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Versioning enabled successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to enable versioning" -ForegroundColor Red
    }
    
    # Enable server-side encryption
    Write-Host "Enabling server-side encryption..." -ForegroundColor Cyan
    $encryptionConfig = '{
        "Rules": [
            {
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }
        ]
    }'
    
    aws s3api put-bucket-encryption --bucket $BucketName --server-side-encryption-configuration $encryptionConfig
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Encryption enabled successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to enable encryption" -ForegroundColor Red
    }
    
    # Block public access
    Write-Host "Blocking public access..." -ForegroundColor Cyan
    $publicAccessConfig = '{
        "BlockPublicAcls": true,
        "IgnorePublicAcls": true,
        "BlockPublicPolicy": true,
        "RestrictPublicBuckets": true
    }'
    
    aws s3api put-public-access-block --bucket $BucketName --public-access-block-configuration $publicAccessConfig
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Public access blocked successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to block public access" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "✅ S3 backend setup completed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Add the following GitHub Secret:" -ForegroundColor White
    Write-Host "   Name: TERRAFORM_STATE_BUCKET" -ForegroundColor Yellow
    Write-Host "   Value: $BucketName" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "2. The bucket is configured with:" -ForegroundColor White
    Write-Host "   - Versioning enabled" -ForegroundColor Green
    Write-Host "   - Server-side encryption (AES256)" -ForegroundColor Green
    Write-Host "   - Public access blocked" -ForegroundColor Green
    Write-Host ""
    Write-Host "3. Your Terraform state will be stored at:" -ForegroundColor White
    Write-Host "   s3://$BucketName/vuedb2springboot/terraform.tfstate" -ForegroundColor Yellow
    
} catch {
    Write-Host "Error setting up S3 backend: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
