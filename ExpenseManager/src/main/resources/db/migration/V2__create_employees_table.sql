-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT uk_employee_email UNIQUE (email),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- Create index for department_id
CREATE INDEX idx_employee_department ON employees(department_id);
