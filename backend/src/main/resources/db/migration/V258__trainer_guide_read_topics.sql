-- Leituras opcionais, independentes das etapas e recompensas do tutorial inicial.
CREATE TABLE trainer_guide_read_topics (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    topic VARCHAR(40) NOT NULL,
    read_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id, topic)
);
