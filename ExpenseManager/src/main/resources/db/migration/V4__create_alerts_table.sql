-- Create alert type enum
CREATE TYPE alert_type AS ENUM ('CATEGORY_DAILY', 'CATEGORY_MONTHLY', 'DEPARTMENT_MONTHLY');

-- Create alert status enum
CREATE TYPE alert_status AS ENUM ('NEW', 'RESOLVED');

-- Create alerts table
CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL,
    type alert_type NOT NULL,
    message VARCHAR(255),
    status alert_status NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_alert_expense_type UNIQUE (expense_id, type),

    CONSTRAINT fk_alert_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON UPDATE CASCADE ON DELETE CASCADE,
);