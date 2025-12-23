-- Create spending_settings table
CREATE TABLE spending_settings (
    department VARCHAR(100) NOT NULL,
    category expense_category NOT NULL,
    type expense_category NOT NULL,
    budget DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    PRIMARY KEY (department, category, type),

    CONSTRAINT fk_spending_setting_department FOREIGN KEY (department) REFERENCES departments(name) ON UPDATE CASCADE ON DELETE CASCADE
);