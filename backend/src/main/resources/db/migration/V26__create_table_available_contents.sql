CREATE TABLE available_contents (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    release_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
