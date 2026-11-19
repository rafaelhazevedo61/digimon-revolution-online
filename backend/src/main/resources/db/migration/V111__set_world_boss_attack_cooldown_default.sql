BEGIN;

UPDATE boss_definitions
SET cooldown_minutes = 5
WHERE code = 'WORLD_BOSS_APOCALYMON'
  AND cooldown_minutes <= 0;

COMMIT;
