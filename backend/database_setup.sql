-- Database setup for ecommerce app
CREATE DATABASE IF NOT EXISTS `full-stack-ecommerce`;
CREATE USER IF NOT EXISTS 'ecommerceapp'@'localhost' IDENTIFIED BY 'ecommerceapp';
GRANT ALL PRIVILEGES ON `full-stack-ecommerce`.* TO 'ecommerceapp'@'localhost';
FLUSH PRIVILEGES;

