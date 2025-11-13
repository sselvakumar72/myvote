-- sql
-- Replace passwords with strong values before use

-- 1) Create an admin role that can create databases/roles
CREATE ROLE myvote_admin WITH LOGIN PASSWORD 'myvote_admin' CREATEDB CREATEROLE;

-- 2) Create the application database owned by the admin
CREATE DATABASE myvote OWNER myvote_admin;

-- 3) Create the application user with no extra privileges
CREATE ROLE myvote_app WITH LOGIN PASSWORD 'myvote_app' NOSUPERUSER NOCREATEDB NOCREATEROLE;

-- 4) Allow the app user to connect to the database
GRANT CONNECT ON DATABASE myvote TO myvote_app;

-- 5) Switch to the new database (works in psql)
connect myvote

-- 6) Create an application schema owned by the app user and grant rights
CREATE SCHEMA IF NOT EXISTS myvote_app AUTHORIZATION myvote_app;
GRANT USAGE ON SCHEMA myvote_app TO myvote;
GRANT CREATE ON SCHEMA myvote_app TO myvote;
GRANT ALL PRIVILEGES ON SCHEMA myvote_app TO myvote_app;

-- 7) Grant privileges on existing objects (if any) and set defaults for future objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA myvote_app TO myvote_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA myvote_app TO myvote_app;

--ALTER DEFAULT PRIVILEGES FOR ROLE myvote_admin IN SCHEMA myvote_app GRANT ALL ON TABLES TO myvote;
--ALTER DEFAULT PRIVILEGES FOR ROLE myvote_admin IN SCHEMA myvote_app GRANT ALL ON SEQUENCES TO myvote;
