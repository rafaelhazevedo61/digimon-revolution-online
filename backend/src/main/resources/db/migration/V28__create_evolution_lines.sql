CREATE TABLE evolution_lines (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    content_id BIGINT NOT NULL REFERENCES available_contents(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE evolution_line_steps (
    id BIGSERIAL PRIMARY KEY,
    evolution_line_id BIGINT NOT NULL REFERENCES evolution_lines(id),
    digimon_info_id BIGINT NOT NULL REFERENCES digimon_infos(id),
    stage VARCHAR(40) NOT NULL,
    step_order INT NOT NULL,

    UNIQUE (evolution_line_id, step_order),
    UNIQUE (evolution_line_id, digimon_info_id)
);