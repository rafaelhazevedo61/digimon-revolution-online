-- Exclusão lógica de clãs: dissolver um clã não remove mais a linha da tabela,
-- apenas marca active = false. Isso evita violação de FK em tabelas que
-- referenciam clan_id (clan_upgrade_purchases, player_clan_missions,
-- clan_raid_instances, clan_invitations) e preserva o histórico.
ALTER TABLE clans
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE clans
    ADD COLUMN dissolved_at TIMESTAMP;

-- Nome e sigla só precisam ser únicos entre clãs ATIVOS: um clã dissolvido
-- não deve impedir a criação de um novo clã com o mesmo nome/sigla.
ALTER TABLE clans DROP CONSTRAINT IF EXISTS clans_name_key;
ALTER TABLE clans DROP CONSTRAINT IF EXISTS clans_tag_key;

CREATE UNIQUE INDEX uk_clans_name_active ON clans (name) WHERE active = TRUE;
CREATE UNIQUE INDEX uk_clans_tag_active ON clans (tag) WHERE active = TRUE;
