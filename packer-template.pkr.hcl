# PACKER TEMPLATE: Ubuntu 24.04 LTS, AWS & GCP Custom Image

# Define Variables
# AWS
variable "aws_region" {
  type    = string
  default = "us-east-1"
}
variable "ami_name" {
  type    = string
  default = "csye6225-aws-ubuntu-image-{{timestamp}}"
}
variable "ssh_username" {
  type    = string
  default = "ubuntu"
}
variable "subnet_id" {
  type    = string
  default = "subnet-00db8b8825dd9ffb1"
}
variable "source_ami" {
  type    = string
  default = "ami-04b4f1a9cf54c11d0"
}

# GCP
variable "gcp_zone" {
  type    = string
  default = "us-east1-b"
}
variable "gcp_project_id" {
  type    = string
  default = "csye6225-dev-452002"
}
variable "service_account_email" {
  type    = string
  default = "github-actions-packer@csye6225-dev-452002.iam.gserviceaccount.com"
}
variable "gcp_machine_type" {
  type    = string
  default = "e2-medium"
}

# Required Plugins
packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
    googlecompute = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

# AWS Builder
source "amazon-ebs" "aws_image" {
  ami_name                = var.ami_name
  region                  = var.aws_region
  instance_type           = "t2.micro"
  source_ami              = var.source_ami
  ssh_username            = var.ssh_username
  subnet_id               = var.subnet_id
  ami_virtualization_type = "hvm"
  ami_users               = ["354918369551", "571600864784"]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

# GCP Builder
source "googlecompute" "gcp_image" {
  project_id   = var.gcp_project_id
  source_image = "ubuntu-2404-noble-amd64-v20250214"
  zone         = var.gcp_zone
  machine_type = var.gcp_machine_type

  image_name              = "csye6225-custom-ubuntu-24-04-{{timestamp}}"
  image_family            = "csye6225-ubuntu-24-04"
  image_description       = "Custom GCP Image for CSYE 6225"
  image_storage_locations = ["us"]

  network               = "default"
  service_account_email = var.service_account_email
  ssh_username          = var.ssh_username

  tags = ["packer-built", "csye6225"]

  metadata = {
    packer_build_date = "{{isotime}}"
  }
}

# Provisioners
build {
  name = "csye-ubuntu-image"
  sources = [
    "source.amazon-ebs.aws_image",
    "source.googlecompute.gcp_image"
  ]

  provisioner "file" {
    source = "build-artifact/health-check.jar"
    #source      = "health-check/target/health-check-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/health-check.jar"
  }

  provisioner "file" {
    source      = "scripts/setup.sh"
    destination = "/tmp/setup.sh"
  }

  provisioner "file" {
    source      = "scripts/healthcheck.service"
    destination = "/tmp/healthcheck.service"
  }

  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup.sh",
      "sed -i 's/\r$//' /tmp/setup.sh",
      "/tmp/setup.sh"
    ]
  }

}


