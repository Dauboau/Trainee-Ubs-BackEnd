-- Create decision enum type
CREATE TYPE decision_type AS ENUM ('APPROVED', 'REJECTED');

-- Create expense category enum type
CREATE TYPE expense_category AS ENUM ('TRAVEL', 'MEAL', 'TRANSPORT', 'OTHER');

-- Create expenses table
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    employee_id UUID NOT NULL,
    date DATE NOT NULL,
    category expense_category NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency currency_code NOT NULL,
    description VARCHAR(510),
    manager_id UUID,
    manager_decision decision_type,
    manager_decision_date TIMESTAMP,
    finance_id UUID,
    finance_decision decision_type,
    finance_decision_date TIMESTAMP,
    revision BOOLEAN NOT NULL DEFAULT FALSE,
    receipt_url VARCHAR(255) NOT NULL,
    receipt_metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_expense_employee_date UNIQUE (employee_id, date),

    CONSTRAINT fk_expense_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_expense_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_expense_finance FOREIGN KEY (finance_id) REFERENCES employees(id) ON UPDATE CASCADE ON DELETE RESTRICT
);