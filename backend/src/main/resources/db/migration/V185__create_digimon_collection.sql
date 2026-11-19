ALTER TABLE digimons DROP CONSTRAINT IF EXISTS ck_digimon_status;

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'COLLECTION_DIGIVICE', 'Digivice de Registro',
    'Registra uma espécie e raridade na Coleção consumindo o Digimon selecionado.',
    'CONSUMABLE', TRUE, NULL, NULL, FALSE, FALSE, FALSE, 999, 'EPIC', 'collection_digivice'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category,
    stackable = EXCLUDED.stackable, tradable = EXCLUDED.tradable, sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable, max_stack = EXCLUDED.max_stack, rarity = EXCLUDED.rarity, icon = EXCLUDED.icon;

CREATE TABLE digimon_collection_entries (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    digimon_info_id BIGINT NOT NULL REFERENCES digimon_infos(id),
    rarity VARCHAR(20) NOT NULL,
    source_digimon_id UUID NOT NULL REFERENCES digimons(id),
    source_event VARCHAR(40) NOT NULL,
    discovered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_collection_player_info_rarity UNIQUE (player_id, digimon_info_id, rarity),
    CONSTRAINT ck_collection_entry_rarity CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'))
);
CREATE INDEX idx_collection_entries_player ON digimon_collection_entries(player_id);
CREATE INDEX idx_collection_entries_species ON digimon_collection_entries(player_id, digimon_info_id);
