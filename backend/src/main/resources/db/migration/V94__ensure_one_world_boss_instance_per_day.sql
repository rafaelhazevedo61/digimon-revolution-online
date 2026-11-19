ALTER TABLE world_boss_instances
    ADD COLUMN boss_date DATE;

UPDATE world_boss_instances
SET boss_date = created_at::date
WHERE boss_date IS NULL;

ALTER TABLE world_boss_instances
    ALTER COLUMN boss_date SET NOT NULL;

CREATE UNIQUE INDEX ux_world_boss_instances_boss_date
    ON world_boss_instances (boss_date);
