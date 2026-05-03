CREATE TABLE evolution_step_materials (
    id BIGSERIAL PRIMARY KEY,
    evolution_line_step_id BIGINT NOT NULL REFERENCES evolution_line_steps(id),
    material_code VARCHAR(80) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    description VARCHAR(255)
);

CREATE INDEX idx_evolution_step_materials_step_id
    ON evolution_step_materials(evolution_line_step_id);
