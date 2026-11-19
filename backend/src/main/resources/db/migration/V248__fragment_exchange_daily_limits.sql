CREATE TABLE fragment_exchange_daily (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    target_stage VARCHAR(20) NOT NULL,
    exchange_date DATE NOT NULL,
    received INTEGER NOT NULL CHECK (received >= 0),
    PRIMARY KEY (player_id, target_stage, exchange_date)
);
