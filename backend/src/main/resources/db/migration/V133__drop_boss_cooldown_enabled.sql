BEGIN;

ALTER TABLE boss_definitions
    DROP COLUMN IF EXISTS cooldown_enabled;

COMMIT;
