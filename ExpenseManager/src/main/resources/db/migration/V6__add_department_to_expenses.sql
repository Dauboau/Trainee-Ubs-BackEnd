-- Add department column to expenses table
ALTER TABLE expenses
ADD COLUMN department VARCHAR(100) NOT NULL;

-- Add foreign key constraint to departments table
ALTER TABLE expenses
ADD CONSTRAINT fk_expense_department 
FOREIGN KEY (department) 
REFERENCES departments(name) 
ON UPDATE CASCADE 
ON DELETE RESTRICT;

-- Create index on department column for optimized queries
CREATE INDEX idx_expenses_department ON expenses(department);
