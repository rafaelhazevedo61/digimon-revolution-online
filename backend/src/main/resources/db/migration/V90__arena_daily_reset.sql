-- Adiciona controle de reset diário de tentativas na arena por player
-- sem apagar o histórico de partidas.
ALTER TABLE players ADD COLUMN IF NOT EXISTS arena_daily_reset_at TIMESTAMPTZ;
