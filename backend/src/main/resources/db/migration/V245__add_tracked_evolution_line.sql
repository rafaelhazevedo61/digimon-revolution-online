ALTER TABLE players ADD COLUMN tracked_evolution_line_id BIGINT
    REFERENCES evolution_lines(id) ON DELETE SET NULL;
