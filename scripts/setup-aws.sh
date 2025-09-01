#!/bin/bash

# AWS Setup Script for VueDB2SpringBoot Deployment
set -e

echo "Setting up AWS infrastructure for VueDB2SpringBoot..."

# Check if required tools are installed
check_tool() {
    if ! command -v $1 &> /dev/null; then
        echo "ERROR: $1 is not installed. Please install it first."
        exit 1
    fi
}

echo "Checking required tools..."
check_tool "terraform"
check_tool "aws"
check_tool "ssh-keygen"

# Check if SSH key exists
if [ ! -f ~/.ssh/id_rsa.pub ]; then
    echo "Generating SSH key pair..."
    ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
    echo "SSH key pair generated at ~/.ssh/id_rsa"
else
    echo "SSH key already exists at ~/.ssh/id_rsa.pub"
fi

# Check AWS credentials
echo "Checking AWS credentials..."
if ! aws sts get-caller-identity &> /dev/null; then
    echo "ERROR: AWS credentials not configured. Please run 'aws configure' first."
    exit 1
fi
echo "AWS credentials configured"

# Create terraform.tfvars if it doesn't exist
if [ ! -f terraform/terraform.tfvars ]; then
    echo "Creating terraform.tfvars from example..."
    cp terraform/terraform.tfvars.example terraform/terraform.tfvars
    echo "WARNING: Please edit terraform/terraform.tfvars with your actual values before running terraform apply"
fi

echo "Setup complete! Next steps:"
echo "1. Edit terraform/terraform.tfvars with your domain and Cloudflare credentials"
echo "2. Run 'terraform init' in the terraform directory"
echo "3. Run 'terraform plan' to review the infrastructure"
echo "4. Run 'terraform apply' to create the infrastructure"
echo "5. Configure GitHub secrets for CI/CD"
