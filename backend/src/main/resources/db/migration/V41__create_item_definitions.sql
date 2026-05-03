CREATE TABLE item_definitions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    category VARCHAR(40) NOT NULL,
    stackable BOOLEAN NOT NULL DEFAULT true
);
