# Make all PowerShell scripts executable and set execution policy
param(
    [switch]$SetExecutionPolicy
)

Write-Host "Setting up PowerShell scripts..." -ForegroundColor Green

# Check if we need to set execution policy
if ($SetExecutionPolicy) {
    Write-Host "Setting PowerShell execution policy..." -ForegroundColor Yellow
    
    try {
        # Check current execution policy
        $currentPolicy = Get-ExecutionPolicy
        Write-Host "Current execution policy: $currentPolicy" -ForegroundColor Cyan
        
        if ($currentPolicy -eq "Restricted") {
            Write-Host "Execution policy is Restricted. Setting to RemoteSigned..." -ForegroundColor Yellow
            Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
            Write-Host "Execution policy set to RemoteSigned" -ForegroundColor Green
        } else {
            Write-Host "Execution policy is already permissive: $currentPolicy" -ForegroundColor Green
        }
    } catch {
        Write-Host "Failed to set execution policy: $_" -ForegroundColor Red
        Write-Host "You may need to run PowerShell as Administrator" -ForegroundColor Yellow
    }
}

# List of PowerShell scripts to make executable
$scripts = @(
    "setup-aws.ps1",
    "deploy-local.ps1", 
    "backup-database.ps1",
    "monitor.ps1",
    "deploy-production.ps1",
    "terraform-deploy.ps1",
    "make-executable.ps1"
)

Write-Host "PowerShell scripts available:" -ForegroundColor Yellow
foreach ($script in $scripts) {
    $scriptPath = "scripts\$script"
    if (Test-Path $scriptPath) {
        Write-Host "  [OK] $script" -ForegroundColor Green
    } else {
        Write-Host "  [MISSING] $script" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "PowerShell setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Available PowerShell commands:" -ForegroundColor Cyan
Write-Host "  .\scripts\setup-aws.ps1                    - Setup AWS infrastructure" -ForegroundColor White
Write-Host "  .\scripts\deploy-local.ps1                 - Deploy locally" -ForegroundColor White
Write-Host "  .\scripts\deploy-production.ps1            - Deploy to production" -ForegroundColor White
Write-Host "  .\scripts\terraform-deploy.ps1             - Manage Terraform" -ForegroundColor White
Write-Host "  .\scripts\backup-database.ps1              - Backup database" -ForegroundColor White
Write-Host "  .\scripts\monitor.ps1                      - Monitor system" -ForegroundColor White
Write-Host "  .\scripts\monitor.ps1 -Continuous          - Continuous monitoring" -ForegroundColor White
Write-Host ""
Write-Host "If you get execution policy errors, run:" -ForegroundColor Yellow
Write-Host "  .\scripts\make-executable.ps1 -SetExecutionPolicy" -ForegroundColor Cyan