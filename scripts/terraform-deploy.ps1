# Terraform Deployment Script for VueDB2SpringBoot (PowerShell)
param(
    [string]$Action = "plan",
    [string]$TerraformDir = "terraform",
    [switch]$AutoApprove,
    [switch]$Destroy
)

Write-Host "🏗️  Terraform Deployment Script for VueDB2SpringBoot" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# Function to check if Terraform is installed
function Test-Terraform {
    try {
        $version = terraform version
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform is installed: $($version.Split("`n")[0])" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "❌ Terraform is not installed or not in PATH" -ForegroundColor Red
        return $false
    }
}

# Function to check if terraform.tfvars exists
function Test-TerraformVars {
    $tfvarsPath = "$TerraformDir\terraform.tfvars"
    if (Test-Path $tfvarsPath) {
        Write-Host "✅ terraform.tfvars found" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ terraform.tfvars not found at $tfvarsPath" -ForegroundColor Red
        Write-Host "💡 Copy terraform.tfvars.example to terraform.tfvars and configure it" -ForegroundColor Yellow
        return $false
    }
}

# Function to initialize Terraform
function Initialize-Terraform {
    Write-Host "🔧 Initializing Terraform..." -ForegroundColor Yellow
    
    try {
        Push-Location $TerraformDir
        terraform init
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform initialized successfully" -ForegroundColor Green
            return $true
        } else {
            throw "Terraform init failed"
        }
    } catch {
        Write-Host "❌ Failed to initialize Terraform: $_" -ForegroundColor Red
        return $false
    } finally {
        Pop-Location
    }
}

# Function to validate Terraform configuration
function Test-TerraformConfig {
    Write-Host "🔍 Validating Terraform configuration..." -ForegroundColor Yellow
    
    try {
        Push-Location $TerraformDir
        terraform validate
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform configuration is valid" -ForegroundColor Green
            return $true
        } else {
            throw "Terraform validation failed"
        }
    } catch {
        Write-Host "❌ Terraform configuration validation failed: $_" -ForegroundColor Red
        return $false
    } finally {
        Pop-Location
    }
}

# Function to run Terraform plan
function Invoke-TerraformPlan {
    Write-Host "📋 Running Terraform plan..." -ForegroundColor Yellow
    
    try {
        Push-Location $TerraformDir
        terraform plan
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform plan completed successfully" -ForegroundColor Green
            return $true
        } else {
            throw "Terraform plan failed"
        }
    } catch {
        Write-Host "❌ Terraform plan failed: $_" -ForegroundColor Red
        return $false
    } finally {
        Pop-Location
    }
}

# Function to run Terraform apply
function Invoke-TerraformApply {
    Write-Host "🚀 Applying Terraform configuration..." -ForegroundColor Yellow
    
    try {
        Push-Location $TerraformDir
        
        if ($AutoApprove) {
            terraform apply -auto-approve
        } else {
            terraform apply
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform apply completed successfully" -ForegroundColor Green
            return $true
        } else {
            throw "Terraform apply failed"
        }
    } catch {
        Write-Host "❌ Terraform apply failed: $_" -ForegroundColor Red
        return $false
    } finally {
        Pop-Location
    }
}

# Function to run Terraform destroy
function Invoke-TerraformDestroy {
    Write-Host "💥 Destroying Terraform infrastructure..." -ForegroundColor Yellow
    
    if (-not $AutoApprove) {
        $confirmation = Read-Host "Are you sure you want to destroy all infrastructure? (yes/no)"
        if ($confirmation -ne "yes") {
            Write-Host "❌ Destruction cancelled by user" -ForegroundColor Yellow
            return $false
        }
    }
    
    try {
        Push-Location $TerraformDir
        
        if ($AutoApprove) {
            terraform destroy -auto-approve
        } else {
            terraform destroy
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform destroy completed successfully" -ForegroundColor Green
            return $true
        } else {
            throw "Terraform destroy failed"
        }
    } catch {
        Write-Host "❌ Terraform destroy failed: $_" -ForegroundColor Red
        return $false
    } finally {
        Pop-Location
    }
}

# Function to show Terraform outputs
function Show-TerraformOutputs {
    Write-Host "📊 Terraform Outputs:" -ForegroundColor Yellow
    
    try {
        Push-Location $TerraformDir
        terraform output
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Terraform outputs displayed successfully" -ForegroundColor Green
        } else {
            Write-Host "⚠️  No outputs available or failed to get outputs" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Failed to get Terraform outputs: $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
}

# Main execution
try {
    # Check prerequisites
    if (-not (Test-Terraform)) {
        exit 1
    }
    
    if (-not (Test-TerraformVars)) {
        exit 1
    }
    
    # Initialize Terraform
    if (-not (Initialize-Terraform)) {
        exit 1
    }
    
    # Validate configuration
    if (-not (Test-TerraformConfig)) {
        exit 1
    }
    
    # Execute the requested action
    switch ($Action.ToLower()) {
        "plan" {
            if (-not (Invoke-TerraformPlan)) {
                exit 1
            }
        }
        "apply" {
            if (-not (Invoke-TerraformApply)) {
                exit 1
            }
            Show-TerraformOutputs
        }
        "destroy" {
            if (-not (Invoke-TerraformDestroy)) {
                exit 1
            }
        }
        "output" {
            Show-TerraformOutputs
        }
        default {
            Write-Host "❌ Invalid action: $Action" -ForegroundColor Red
            Write-Host "💡 Valid actions: plan, apply, destroy, output" -ForegroundColor Yellow
            exit 1
        }
    }
    
    Write-Host "🎉 Terraform operation completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Terraform operation failed: $_" -ForegroundColor Red
    exit 1
}
