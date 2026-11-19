-- Progresso do tutorial inicial por jogador.
-- Cada linha registra um step concluido; a ausencia de linha significa step pendente.
CREATE TABLE tutorial_progress (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    step VARCHAR(40) NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_tutorial_progress_player_step UNIQUE (player_id, step)
);

CREATE INDEX idx_tutorial_progress_player ON tutorial_progress (player_id);
