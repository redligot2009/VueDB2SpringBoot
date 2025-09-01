# Terraform backend configuration for state storage
# This ensures state is persisted between GitHub Actions runs

terraform {
  backend "s3" {
    # These values will be provided via backend config during terraform init
    # bucket  = "your-terraform-state-bucket"
    # key     = "vuedb2springboot/terraform.tfstate"
    # region  = "us-east-1"
    # encrypt = true
  }
}
