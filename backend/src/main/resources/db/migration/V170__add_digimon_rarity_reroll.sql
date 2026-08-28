CREATE TABLE digimon_rarity_rerolls (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    digimon_id UUID NOT NULL REFERENCES digimons(id),
    current_rarity VARCHAR(20) NOT NULL,
    new_rarity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    CONSTRAINT ck_rarity_reroll_status CHECK (status IN ('PENDING', 'ACCEPTED', 'KEPT'))
);
CREATE INDEX idx_rarity_rerolls_player_status ON digimon_rarity_rerolls(player_id, status);

INSERT INTO item_definitions (code, name, description, category, stackable, buy_price, sell_price, tradable, sellable, usable, max_stack, rarity, icon)
VALUES ('RARITY_REROLL', 'Reroll de Raridade', 'Gera uma nova proposta de raridade para o seu Digimon ativo.', 'CONSUMABLE', TRUE, 0, 0, TRUE, FALSE, TRUE, 999, 'EPIC', '✨')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, usable = TRUE;
