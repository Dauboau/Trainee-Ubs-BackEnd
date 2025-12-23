-- Create expenses table
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    receipt_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_expense_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_expense_employee ON expenses(employee_id);
CREATE INDEX idx_expense_status ON expenses(status);
CREATE INDEX idx_expense_date ON expenses(expense_date);
