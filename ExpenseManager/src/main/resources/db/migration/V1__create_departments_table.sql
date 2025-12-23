-- Create departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    monthly_budget DECIMAL(15,2) NOT NULL,
    CONSTRAINT uk_department_name UNIQUE (name)
);
