-- Add exchange_rate column to expenses table
ALTER TABLE expenses
ADD COLUMN exchange_rate DECIMAL(18,8) NOT NULL;

-- expenses table
COMMENT ON COLUMN expenses.id IS 'Primary key UUID of the expense';
COMMENT ON COLUMN expenses.employee_id IS 'Reference to the employee who submitted the expense (employees.id)';
COMMENT ON COLUMN expenses.date IS 'Date and time of the expense (with timezone)';
COMMENT ON COLUMN expenses.category IS 'Expense category (expense_category enum)';
COMMENT ON COLUMN expenses.amount IS 'Expense amount in the specified currency (precision 12,2)';
COMMENT ON COLUMN expenses.currency IS 'Currency code of the expense (currency_code enum)';
COMMENT ON COLUMN expenses.description IS 'Optional description of the expense (up to 510 characters)';
COMMENT ON COLUMN expenses.manager_id IS 'Reference to the manager responsible for approval (employees.id)';
COMMENT ON COLUMN expenses.manager_decision IS 'Manager decision: APPROVED or REJECTED';
COMMENT ON COLUMN expenses.manager_decision_date IS 'Date and time of the manager decision';
COMMENT ON COLUMN expenses.finance_id IS 'Reference to the finance user who reviewed the expense (employees.id)';
COMMENT ON COLUMN expenses.finance_decision IS 'Finance decision: APPROVED or REJECTED';
COMMENT ON COLUMN expenses.finance_decision_date IS 'Date and time of the finance decision';
COMMENT ON COLUMN expenses.revision IS 'Flag indicating whether the expense is under revision';
COMMENT ON COLUMN expenses.receipt_url IS 'URL of the receipt or invoice associated with the expense';
COMMENT ON COLUMN expenses.receipt_metadata IS 'Receipt metadata stored as JSONB';
COMMENT ON COLUMN expenses.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN expenses.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN expenses.department IS 'Department associated with the expense (departments.name)';
COMMENT ON COLUMN expenses.exchange_rate IS 'Exchange rate from the expense currency to the department currency at submission time (precision 18,8)';

-- departments table
COMMENT ON COLUMN departments.name IS 'Primary key name of the department';
COMMENT ON COLUMN departments.currency IS 'Default currency of the department (currency_code enum)';
COMMENT ON COLUMN departments.monthly_budget IS 'Department monthly budget in department currency (precision 15,2)';
COMMENT ON COLUMN departments.created_at IS 'Department record creation timestamp';
COMMENT ON COLUMN departments.updated_at IS 'Department record last update timestamp';

-- employees table
COMMENT ON COLUMN employees.id IS 'Primary key UUID of the employee';
COMMENT ON COLUMN employees.email IS 'Employee email (case-insensitive)';
COMMENT ON COLUMN employees.name IS 'Full name of the employee';
COMMENT ON COLUMN employees.manager_id IS 'Reference to the employee''s manager (employees.id)';
COMMENT ON COLUMN employees.password IS 'Hashed password for authentication';
COMMENT ON COLUMN employees.role IS 'Employee role (employee_role enum)';
COMMENT ON COLUMN employees.department IS 'Department name the employee belongs to (departments.name)';
COMMENT ON COLUMN employees.position IS 'Job position or title of the employee';
COMMENT ON COLUMN employees.active IS 'Flag indicating if the employee account is active';
COMMENT ON COLUMN employees.first_time IS 'Flag indicating if the employee is logging in for the first time';
COMMENT ON COLUMN employees.created_at IS 'Employee record creation timestamp';
COMMENT ON COLUMN employees.updated_at IS 'Employee record last update timestamp';

-- alerts table
COMMENT ON COLUMN alerts.id IS 'Primary key UUID of the alert';
COMMENT ON COLUMN alerts.expense_id IS 'Reference to the related expense (expenses.id)';
COMMENT ON COLUMN alerts.type IS 'Alert type (alert_type enum)';
COMMENT ON COLUMN alerts.message IS 'Alert descriptive message';
COMMENT ON COLUMN alerts.status IS 'Alert status: NEW or RESOLVED';
COMMENT ON COLUMN alerts.created_at IS 'Alert creation timestamp';
COMMENT ON COLUMN alerts.updated_at IS 'Alert last update timestamp';

-- spending_settings table
COMMENT ON COLUMN spending_settings.department IS 'Department name for the spending setting (departments.name)';
COMMENT ON COLUMN spending_settings.category IS 'Expense category for the spending setting (expense_category enum)';
COMMENT ON COLUMN spending_settings.type IS 'Spending type: DAILY or MONTHLY';
COMMENT ON COLUMN spending_settings.budget IS 'Budget amount for the spending setting (precision 15,2)';
COMMENT ON COLUMN spending_settings.created_at IS 'Spending setting creation timestamp';
COMMENT ON COLUMN spending_settings.updated_at IS 'Spending setting last update timestamp';

-- Table descriptions
COMMENT ON TABLE expenses IS 'Stores employee expenses and approval workflow, including amounts, currency, department and approval decisions';
COMMENT ON TABLE departments IS 'Contains department metadata: name, default currency and monthly budget';
COMMENT ON TABLE employees IS 'Employee directory with authentication, role and department association';
COMMENT ON TABLE alerts IS 'Generated alerts related to expenses (category/day/month or department/month)';
COMMENT ON TABLE spending_settings IS 'Spending thresholds and budgets per department, category and type';