#!/bin/bash

# Bash script to troubleshoot AWS AMI issues
# This script helps find available Ubuntu AMIs in your region

REGION=${1:-us-east-1}
OWNER="099720109477"

echo "🔍 Troubleshooting AWS AMI Issues"
echo "Region: $REGION"
echo "Owner: $OWNER (Canonical)"
echo ""

# Check AWS CLI configuration
echo "1. Checking AWS CLI configuration..."
if command -v aws &> /dev/null; then
    AWS_VERSION=$(aws --version)
    echo "✅ AWS CLI: $AWS_VERSION"
else
    echo "❌ AWS CLI not found or not configured"
    echo "Please install and configure AWS CLI first"
    exit 1
fi

# Check AWS credentials
echo "2. Checking AWS credentials..."
if aws sts get-caller-identity --region $REGION &> /dev/null; then
    echo "✅ AWS credentials configured"
    IDENTITY=$(aws sts get-caller-identity --region $REGION)
    ACCOUNT=$(echo $IDENTITY | jq -r '.Account')
    ARN=$(echo $IDENTITY | jq -r '.Arn')
    echo "   Account: $ACCOUNT"
    echo "   User: $ARN"
else
    echo "❌ AWS credentials not configured or invalid"
    echo "Please run 'aws configure' to set up your credentials"
    exit 1
fi

echo ""

# Search for Ubuntu AMIs
echo "3. Searching for Ubuntu AMIs..."

# Try different AMI name patterns
AMI_PATTERNS=(
    "ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"
    "ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"
    "ubuntu/images/hvm-ssd/ubuntu-*"
    "ubuntu/images/hvm-ssd/*"
)

for pattern in "${AMI_PATTERNS[@]}"; do
    echo "   Trying pattern: $pattern"
    
    # Create temporary query file
    cat > temp-ami-query.json << EOF
{
    "Owners": ["$OWNER"],
    "Filters": [
        {
            "Name": "name",
            "Values": ["$pattern"]
        },
        {
            "Name": "virtualization-type",
            "Values": ["hvm"]
        },
        {
            "Name": "architecture",
            "Values": ["x86_64"]
        }
    ]
}
EOF
    
    # Query AWS for AMIs
    if RESULT=$(aws ec2 describe-images --cli-input-json file://temp-ami-query.json --region $REGION 2>/dev/null); then
        IMAGE_COUNT=$(echo $RESULT | jq '.Images | length')
        
        if [ "$IMAGE_COUNT" -gt 0 ]; then
            echo "✅ Found $IMAGE_COUNT AMI(s) with pattern: $pattern"
            
            # Show the most recent 3 AMIs
            echo "   Most recent AMIs:"
            echo $RESULT | jq -r '.Images | sort_by(.CreationDate) | reverse | .[0:3][] | "   - ID: \(.ImageId)\n     Name: \(.Name)\n     Created: \(.CreationDate)\n"'
            
            # Get the latest AMI details
            LATEST_AMI=$(echo $RESULT | jq -r '.Images | sort_by(.CreationDate) | reverse | .[0]')
            AMI_ID=$(echo $LATEST_AMI | jq -r '.ImageId')
            AMI_NAME=$(echo $LATEST_AMI | jq -r '.Name')
            
            echo "4. Recommended Terraform configuration:"
            echo ""
            echo "data \"aws_ami\" \"ubuntu\" {"
            echo "  most_recent = true"
            echo "  owners      = [\"$OWNER\"]"
            echo ""
            echo "  filter {"
            echo "    name   = \"name\""
            echo "    values = [\"$AMI_NAME\"]"
            echo "  }"
            echo ""
            echo "  filter {"
            echo "    name   = \"virtualization-type\""
            echo "    values = [\"hvm\"]"
            echo "  }"
            echo ""
            echo "  filter {"
            echo "    name   = \"architecture\""
            echo "    values = [\"x86_64\"]"
            echo "  }"
            echo "}"
            echo ""
            
            # Alternative: Use specific AMI ID
            echo "5. Alternative: Use specific AMI ID:"
            echo ""
            echo "resource \"aws_instance\" \"app\" {"
            echo "  ami           = \"$AMI_ID\""
            echo "  instance_type = var.instance_type"
            echo "  # ... other configuration"
            echo "}"
            echo ""
            
            # Clean up
            rm -f temp-ami-query.json
            exit 0
        fi
    else
        echo "   ❌ Error with pattern: $pattern"
    fi
    
    # Clean up
    rm -f temp-ami-query.json
done

echo "❌ No Ubuntu AMIs found with any pattern"
echo ""
echo "Troubleshooting suggestions:"
echo "1. Check if you're in the correct AWS region"
echo "2. Verify your AWS credentials have EC2 permissions"
echo "3. Try a different region (e.g., us-west-2, eu-west-1)"
echo "4. Check if the Canonical owner ID is correct for your region"
echo ""
echo "To try a different region, run:"
echo "./scripts/troubleshoot-ami.sh us-west-2"
