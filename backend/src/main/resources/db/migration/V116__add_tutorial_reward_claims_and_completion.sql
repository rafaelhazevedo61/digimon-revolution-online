BEGIN;

ALTER TABLE tutorial_progress
    ADD COLUMN reward_claimed_at TIMESTAMP;

-- Etapas antigas já entregaram suas recompensas automaticamente no fluxo anterior.
-- Marcá-las como resgatadas evita que sejam concedidas novamente após a migração.
UPDATE tutorial_progress
SET reward_claimed_at = completed_at
WHERE reward_claimed_at IS NULL;

CREATE TABLE tutorial_completions (
    player_id UUID PRIMARY KEY REFERENCES players(id),
    finished_at TIMESTAMP NOT NULL
);

COMMIT;
