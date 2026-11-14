-- Separa o HP de vida do Chefe Mundial e da Incursão dos atributos usados no cálculo da chance.
-- Com combat_* nulo, a chance continua sendo calculada com hp/atk/def (bosses normais).
ALTER TABLE boss_definitions
    ADD COLUMN combat_hp INTEGER,
    ADD COLUMN combat_atk INTEGER,
    ADD COLUMN combat_def INTEGER,
    ADD CONSTRAINT chk_boss_definitions_combat_stats CHECK (
        (combat_hp IS NULL OR combat_hp >= 0)
        AND (combat_atk IS NULL OR combat_atk >= 0)
        AND (combat_def IS NULL OR combat_def >= 0)
    );

-- Poder de combate = HP×0,3 + ATK×1,5 + DEF: 1.000 no Apocalymon e 600 no Omegamon.
UPDATE boss_definitions
SET combat_hp = 1000, combat_atk = 300, combat_def = 250
WHERE code = 'WORLD_BOSS_APOCALYMON';

UPDATE boss_definitions
SET combat_hp = 600, combat_atk = 180, combat_def = 150
WHERE code = 'CLAN_RAID_OMEGAMON';
