-- Torna o raid de clã acessível a qualquer Digimon, independente de stage/level/rebirth.
UPDATE boss_definitions
SET required_stage = 'BABY',
    required_level = 1,
    required_rebirths = 0
WHERE code = 'CLAN_RAID_OMEGAMON';
