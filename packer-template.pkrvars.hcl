# AWS Variables
aws_region    = "us-east-1"
ami_name      = "csye6225-aws-ubuntu-image-{{timestamp}}"
ssh_username  = "ubuntu"

subnet_id     = "subnet-00db8b8825dd9ffb1"

source_ami    = "ami-04b4f1a9cf54c11d0"

# GCP Variables
gcp_zone              = "us-east1-b"
gcp_project_id        = "csye6225-dev-452002"
service_account_email = "github-actions-packer@csye6225-dev-452002.iam.gserviceaccount.com"
gcp_machine_type      = "e2-medium"
