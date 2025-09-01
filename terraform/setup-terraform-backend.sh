#!/bin/bash

# Bash script to set up S3 backend for Terraform state
# This creates the S3 bucket and configures it for Terraform state storage

set -e

# Default values
REGION="us-east-1"

# Function to show usage
usage() {
    echo "Usage: $0 <bucket-name> [region]"
    echo "  bucket-name: Name of the S3 bucket to create"
    echo "  region: AWS region (default: us-east-1)"
    echo ""
    echo "Example: $0 my-terraform-state-bucket us-east-1"
    exit 1
}

# Check if bucket name is provided
if [ $# -lt 1 ]; then
    echo "Error: Bucket name is required"
    usage
fi

BUCKET_NAME="$1"
if [ $# -ge 2 ]; then
    REGION="$2"
fi

echo "🔧 Setting up Terraform S3 backend..."
echo "📦 Bucket Name: $BUCKET_NAME"
echo "🌍 Region: $REGION"
echo ""

# Check if bucket already exists
echo "🔍 Checking if bucket exists..."
if aws s3api head-bucket --bucket "$BUCKET_NAME" 2>/dev/null; then
    echo "✅ Bucket '$BUCKET_NAME' already exists"
else
    echo "📦 Creating S3 bucket: $BUCKET_NAME"
    
    # Create the bucket
    if [ "$REGION" = "us-east-1" ]; then
        # us-east-1 doesn't need LocationConstraint
        aws s3api create-bucket --bucket "$BUCKET_NAME" --region "$REGION"
    else
        # Other regions need LocationConstraint
        aws s3api create-bucket \
            --bucket "$BUCKET_NAME" \
            --region "$REGION" \
            --create-bucket-configuration LocationConstraint="$REGION"
    fi
    
    echo "✅ Bucket created successfully!"
fi

# Enable versioning
echo "🔄 Enabling versioning..."
aws s3api put-bucket-versioning \
    --bucket "$BUCKET_NAME" \
    --versioning-configuration Status=Enabled
echo "✅ Versioning enabled successfully!"

# Enable server-side encryption
echo "🔒 Enabling server-side encryption..."
aws s3api put-bucket-encryption \
    --bucket "$BUCKET_NAME" \
    --server-side-encryption-configuration '{
        "Rules": [
            {
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }
        ]
    }'
echo "✅ Encryption enabled successfully!"

# Block public access
echo "🚫 Blocking public access..."
aws s3api put-public-access-block \
    --bucket "$BUCKET_NAME" \
    --public-access-block-configuration '{
        "BlockPublicAcls": true,
        "IgnorePublicAcls": true,
        "BlockPublicPolicy": true,
        "RestrictPublicBuckets": true
    }'
echo "✅ Public access blocked successfully!"

echo ""
echo "🎉 S3 backend setup completed!"
echo ""
echo "📋 Next steps:"
echo "1. Add the following GitHub Secret:"
echo "   Name: TERRAFORM_STATE_BUCKET"
echo "   Value: $BUCKET_NAME"
echo ""
echo "2. The bucket is configured with:"
echo "   ✅ Versioning enabled"
echo "   ✅ Server-side encryption (AES256)"
echo "   ✅ Public access blocked"
echo ""
echo "3. Your Terraform state will be stored at:"
echo "   s3://$BUCKET_NAME/vuedb2springboot/terraform.tfstate"
echo ""
echo "🔐 Security Note: Make sure your AWS credentials have the following permissions:"
echo "   - s3:CreateBucket"
echo "   - s3:GetBucketVersioning"
echo "   - s3:PutBucketVersioning"
echo "   - s3:GetBucketEncryption"
echo "   - s3:PutBucketEncryption"
echo "   - s3:GetPublicAccessBlock"
echo "   - s3:PutPublicAccessBlock"
echo "   - s3:GetObject"
echo "   - s3:PutObject"
echo "   - s3:DeleteObject"
