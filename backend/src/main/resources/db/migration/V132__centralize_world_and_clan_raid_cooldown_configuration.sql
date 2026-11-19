BEGIN;

-- World Boss e Raid de Clã são controlados exclusivamente pelas flags
-- dro.gameplay.boss-cooldown-enabled e pelas flags individuais do YAML.
-- Mantemos TRUE no campo legado para compatibilidade com ferramentas antigas.
UPDATE boss_definitions
SET cooldown_enabled = TRUE
WHERE boss_type IN ('WORLD', 'CLAN');

COMMIT;
