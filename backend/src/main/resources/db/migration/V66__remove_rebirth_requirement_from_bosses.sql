-- Remove rebirth requirements from all bosses
UPDATE boss_definitions SET required_rebirths = 0 WHERE required_rebirths > 0;
