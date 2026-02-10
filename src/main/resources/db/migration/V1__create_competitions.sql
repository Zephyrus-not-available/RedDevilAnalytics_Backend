-- Simple example - adjust column names/types to match your Competition entity
CREATE TABLE IF NOT EXISTS competitions (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255),
  start_date TIMESTAMP,
  end_date TIMESTAMP
);

