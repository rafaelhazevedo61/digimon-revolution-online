ALTER TABLE world_boss_instances
    ADD COLUMN cycle_number INTEGER NOT NULL DEFAULT 1;

DROP INDEX IF EXISTS ux_world_boss_instances_boss_date;

CREATE UNIQUE INDEX ux_world_boss_instances_boss_date_cycle
    ON world_boss_instances (boss_date, cycle_number);
