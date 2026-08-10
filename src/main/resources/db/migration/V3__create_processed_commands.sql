CREATE TABLE processed_commands (
    command_id VARCHAR(36) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);