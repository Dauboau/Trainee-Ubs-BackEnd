-- Set default value for departments.monthly_budget to 0

ALTER TABLE departments
ALTER COLUMN monthly_budget SET DEFAULT 0;
