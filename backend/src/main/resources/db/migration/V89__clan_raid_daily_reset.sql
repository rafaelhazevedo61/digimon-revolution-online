-- Adiciona controle de reset diário de tentativas na raid de clã
-- sem apagar o histórico/dano dos ataques.
ALTER TABLE clan_raid_instances ADD COLUMN IF NOT EXISTS daily_reset_at TIMESTAMPTZ;

UPDATE clan_raid_instances
SET daily_reset_at = created_at
WHERE daily_reset_at IS NULL;
