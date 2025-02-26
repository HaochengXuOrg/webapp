#!/bin/bash
set -e

APP_DIR="/opt/csye6225"
ZIP_FILE="webapp.zip"
LINUX_GROUP="csye6225"
LINUX_USER="csye6225"
ARTIFACT_NAME="health-check.jar"
SYSTEMD_SERVICE_NAME="healthcheck.service"

echo " Updating and upgrading system packages..."
sudo apt update -y && sudo apt upgrade -y

echo " Installing Openjdk..."
apt install openjdk-21-jdk-headless

echo " Installing Maven..."
apt install maven

echo "Installing MySQL..."
sudo apt install -y mysql-server

echo "Configuring MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql

mysql -u root "-p2001050926" -e "CREATE DATABASE health_check_db;"
mysql -u root "-p2001050926" -e "CREATE USER 'root'@'127.0.0.1' IDENTIFIED BY '2001050926';GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;FLUSH PRIVILEGES;"

echo "Creating Linux group and user..."
sudo groupadd -f ${LINUX_GROUP}
sudo useradd -m -g ${LINUX_GROUP} -s /bin/bash ${LINUX_USER} || echo "User already exists"

echo "Creating application directory..."
sudo mkdir -p ${APP_DIR}

echo "Copying application artifact..."
if [ -f "/tmp/${ARTIFACT_NAME}" ]; then
  sudo cp "/tmp/${ARTIFACT_NAME}" "${APP_DIR}/"
else
  echo "Artifact /tmp/${ARTIFACT_NAME} not found! Exiting."
  exit 1
fi

echo "Updating permissions for ${APP_DIR}..."
sudo chown -R ${LINUX_USER}:${LINUX_GROUP} ${APP_DIR}
sudo find ${APP_DIR} -type d -exec chmod 750 {} \;
sudo find ${APP_DIR} -type f -exec chmod 640 {} \;
sudo chmod 750 ${APP_DIR}/health-check.jar


echo "Installing systemd service..."
if [ -f "/tmp/${SYSTEMD_SERVICE_NAME}" ]; then
  sudo cp "/tmp/${SYSTEMD_SERVICE_NAME}" "/etc/systemd/system/${SYSTEMD_SERVICE_NAME}"
  sudo chown root:root "/etc/systemd/system/${SYSTEMD_SERVICE_NAME}"
  sudo chmod 644 "/etc/systemd/system/${SYSTEMD_SERVICE_NAME}"
else
  echo "Systemd service file /tmp/${SYSTEMD_SERVICE_NAME} not found! Exiting."
  exit 1
fi

echo "Reloading systemd and enabling service..."
sudo systemctl daemon-reload
sudo systemctl enable "${SYSTEMD_SERVICE_NAME}"

echo "Setup complete."
