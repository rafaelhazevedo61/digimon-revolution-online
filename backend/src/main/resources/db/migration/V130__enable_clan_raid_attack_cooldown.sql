BEGIN;

UPDATE boss_definitions
SET cooldown_minutes = 5,
    cooldown_enabled = TRUE
WHERE boss_type = 'CLAN';

COMMIT;

