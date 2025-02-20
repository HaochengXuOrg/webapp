SELECT * FROM your_database.your_table;#!/bin/bash
set -e

DB_NAME="health_check_db"
DB_USER="user"
DB_PASSWORD="password"
MYSQL_ROOT_PASSWORD="2001050926"
LINUX_GROUP="webappgroup"
LINUX_USER="webappuser"
APP_DIR="/opt/csye6225"
ZIP_FILE="webapp.zip"

echo " Updating and upgrading system packages..."
sudo apt update -y && sudo apt upgrade -y

echo "Installing MySQL..."
sudo apt install -y mysql-server

echo "Configuring MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql

mysql -u root "-p2001050926" -e "CREATE DATABASE health_check_db;CREATE USER 'hc_user'@'localhost' IDENTIFIED BY 'password';GRANT ALL PRIVILEGES ON health_check_db.* TO 'hc_user'@'localhost';FLUSH PRIVILEGES;EXIT;"

echo "Creating Linux group and user..."
sudo groupadd -f ${LINUX_GROUP}
sudo useradd -m -g ${LINUX_GROUP} -s /bin/bash ${LINUX_USER} || echo "User already exists"

echo "Creating application directory..."
sudo mkdir -p ${APP_DIR}

echo "Extracting application files..."
if [ -f "${ZIP_FILE}" ]; then
    sudo unzip -o ${ZIP_FILE} -d ${APP_DIR}
else
    echo "Zip file ${ZIP_FILE} not found! Please ensure the file is in the script directory."
    exit 1
fi

echo "Updating permissions for ${APP_DIR}..."
sudo chown -R ${LINUX_USER}:${LINUX_GROUP} ${APP_DIR}
sudo find ${APP_DIR} -type d -exec chmod 750 {} \;
sudo find ${APP_DIR} -type f -exec chmod 640 {} \;

echo "Setup complete"
