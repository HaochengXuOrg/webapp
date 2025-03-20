# webapp (demo)

## Prerequisites for Building and Deploying Locally

Before building and deploying the application locally, ensure the following dependencies are installed.

### **1️⃣ System Requirements**
- **OS:** Ubuntu 24.04 LTS (or Linux/macOS/Windows)
- **Java:** OpenJDK 17+ (`java -version`)
- **Maven:** Apache Maven 3.8+ (`mvn -version`)
- **MySQL:** Installed and running (`mysql --version`)

### **2️⃣ Install Required Dependencies**
#### **🔹 Install Java 17**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```
#### **🔹 Install Maven**
```bash
sudo apt install maven
mvn -version
```
#### **🔹 Install MySQL**
```bash
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```
### **3️⃣ Database Setup**
#### **🔹 Create MySQL Database and User**
- **Log into MySQL:**
  ```bash
  mysql -u root -p
  ```
- **Set up MySQL:**
  ```bash
  CREATE DATABASE health_check_db;
  CREATE USER 'your_name'@'localhost' IDENTIFIED BY 'your_password';
  GRANT ALL PRIVILEGES ON health_check_db.* TO 'hc_user'@'localhost';
  FLUSH PRIVILEGES;
  EXIT;
  ```
- **Start MySQL:**
  ```bash
  sudo systemctl start mysql
  ```
- **Stop MySQL:**
  ```bash
  sudo systemctl stop mysql
  ```
- **Restart MySQL:**
  ```bash
  sudo systemctl restart mysql
  ```
## Build and Deploy Instructions
#### **🔹 Build the Project**
```bash
./mvnw clean package
```
#### **🔹 Run the Application**
```bash
./mvnw spring-boot:run
```
 
  
