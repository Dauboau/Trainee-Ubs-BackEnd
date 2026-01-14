-- Enable citext extension for case-insensitive text
CREATE EXTENSION IF NOT EXISTS citext;

-- Create role enum type
CREATE TYPE employee_role AS ENUM ('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN');

-- Create employees table
CREATE TABLE employees (
                           id UUID PRIMARY KEY,
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

INSERT INTO employees (id,email,name,manager_id,password,role,department,position,active,first_time)
VALUES ('11111111-1111-1111-1111-111111111111','admin@empresa.com','Sergio ADM','11111111-1111-1111-1111-111111111111', 'pass-adm', 'ADMIN', 'SYSTEM', 'Administrador do Sistema', TRUE, FALSE);

INSERT INTO employees (id,email,name,manager_id,password,role,department,position,active,first_time)
VALUES ('22222222-2222-2222-2222-222222222222','manager@empresa.com','Silvio Mann','22222222-2222-2222-2222-222222222222','pass-man','MANAGER','SYSTEM','Gerente de RH',TRUE,TRUE);