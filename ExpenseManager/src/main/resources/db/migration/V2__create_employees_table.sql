-- Enable citext extension for case-insensitive text
CREATE EXTENSION IF NOT EXISTS citext;

-- Enable UUID v7 extension
CREATE EXTENSION IF NOT EXISTS pg_uuidv7;

-- Create role enum type
CREATE TYPE employee_role AS ENUM ('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN');

-- Create employees table
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    email CITEXT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    manager_id UUID NOT NULL,
    password VARCHAR(255) NOT NULL,
    role employee_role NOT NULL,
    department VARCHAR(100) NOT NULL,
    position VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    first_time BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_employee_department FOREIGN KEY (department) REFERENCES departments(name) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_employee_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON UPDATE CASCADE ON DELETE RESTRICT
);