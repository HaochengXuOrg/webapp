#!/bin/bash
set -e

APP_DIR="/opt/csye6225"
ZIP_FILE="webapp.zip"
LINUX_GROUP="csye6225"
LINUX_USER="csye6225"
ARTIFACT_NAME="health-check.jar"
SYSTEMD_SERVICE_NAME="healthcheck.service"
MYSQL_ROOT_PASSWORD="2001050926"

echo " Updating and upgrading system packages..."
echo 'debconf debconf/frontend select Noninteractive' | sudo debconf-set-selections
sudo apt-get update -y && sudo apt-get upgrade -y

echo " Installing Openjdk..."
sudo apt-get install -y openjdk-21-jdk-headless

echo " Installing Maven..."
sudo apt-get install -y maven

echo "Preconfiguring MySQL root password..."
sudo debconf-set-selections <<< "mysql-server mysql-server/root_password password ${MYSQL_ROOT_PASSWORD}"
sudo debconf-set-selections <<< "mysql-server mysql-server/root_password_again password ${MYSQL_ROOT_PASSWORD}"

echo "Installing MySQL..."
sudo apt-get install -y mysql-server

echo "Configuring MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql

# Ensure root uses password authentication
sudo mysql -u root -p${MYSQL_ROOT_PASSWORD} <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
FLUSH PRIVILEGES;
EOF

mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS health_check_db;"
mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';"
mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;"
mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "FLUSH PRIVILEGES;"
#mysql -u root "-p2001050926" -e "CREATE DATABASE IF NOT EXISTS health_check_db;"
#mysql -u root "-p2001050926" -e "CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY '2001050926';GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;FLUSH PRIVILEGES;"

echo "Creating Linux group and user..."
sudo groupadd -f ${LINUX_GROUP} || true
if id "${LINUX_USER}" &>/dev/null; then
    echo "User ${LINUX_USER} exists. Updating shell..."
    sudo usermod -s /usr/sbin/nologin ${LINUX_USER}
else
    sudo useradd -r -m -g ${LINUX_GROUP} -s /usr/sbin/nologin ${LINUX_USER}
fi
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
sudo find ${APP_DIR} -type d -exec chmod 777 {} \;
sudo find ${APP_DIR} -type f -exec chmod 777 {} \;
sudo chmod 777 ${APP_DIR}/health-check.jar


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
