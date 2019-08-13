CREATE DATABASE account_db;
CREATE USER moneytransfer WITH PASSWORD 'money';
GRANT ALL PRIVILEGES ON DATABASE account_db to moneytransfer;