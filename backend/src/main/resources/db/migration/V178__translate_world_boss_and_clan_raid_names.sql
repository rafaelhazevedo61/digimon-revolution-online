-- Tradução dos nomes exibidos para as atividades compartilhadas.
UPDATE boss_definitions
SET name = 'Apocalymon (Chefe Mundial)'
WHERE code = 'WORLD_BOSS_APOCALYMON'
  AND name = 'Apocalymon (Boss Mundial)';

UPDATE boss_definitions
SET name = 'Omegamon (Incursão de Clã)'
WHERE code = 'CLAN_RAID_OMEGAMON'
  AND name = 'Omegamon (Raid de Clã)';

-- A condição pelo nome torna a migration idempotente em ambientes já corrigidos.

-- Atualiza somente a apresentação; os códigos das definições permanecem inalterados.

-- Fim.
