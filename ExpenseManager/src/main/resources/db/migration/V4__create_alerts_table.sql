-- Create alerts table
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_alert_department ON alerts(department_id);
CREATE INDEX idx_alert_is_read ON alerts(is_read);
CREATE INDEX idx_alert_created ON alerts(created_at);
