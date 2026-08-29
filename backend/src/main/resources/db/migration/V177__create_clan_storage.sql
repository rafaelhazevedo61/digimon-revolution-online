-- Armazém compartilhado do clã.
-- Cada linha representa uma pilha de um item catalogado; linhas adicionais
-- permitem respeitar o max_stack sem transformar a capacidade em quantidade.
CREATE TABLE clan_storage_items (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL REFERENCES clans(id),
    item_definition_id BIGINT NOT NULL REFERENCES item_definitions(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clan_storage_items_clan_id
    ON clan_storage_items(clan_id);

CREATE INDEX idx_clan_storage_items_clan_item
    ON clan_storage_items(clan_id, item_definition_id);

-- Histórico permanente das movimentações do armazém.
CREATE TABLE clan_storage_history (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL REFERENCES clans(id),
    actor_player_id UUID NOT NULL REFERENCES players(id),
    actor_username VARCHAR(80) NOT NULL,
    action VARCHAR(20) NOT NULL,
    item_definition_id BIGINT NOT NULL REFERENCES item_definitions(id),
    item_code VARCHAR(80) NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clan_storage_history_clan_created_at
    ON clan_storage_history(clan_id, created_at DESC);

-- Capacidade inicial: 20 slots. Cada nível do upgrade adiciona 10 slots.
-- O custo segue a progressão genérica dos upgrades de clã.
-- V82 criou effect_per_level como DECIMAL(5,4), cujo limite inteiro é 9.
-- O upgrade de armazenamento precisa registrar 10,0000 por nível.
ALTER TABLE clan_upgrade_types
    ALTER COLUMN effect_per_level TYPE DECIMAL(6,4);
INSERT INTO clan_upgrade_types (
    code, name, description, unlocked_at_clan_level, max_level,
    base_honor_marks_cost, cost_multiplier, effect_per_level, effect_type, stat
)
VALUES (
    'CLAN_STORAGE_CAPACITY',
    'Armazém do Clã',
    'Aumenta a capacidade do armazém compartilhado em 10 slots por nível.',
    1,
    10,
    100,
    2.0,
    10,
    'CAPACITY',
    'storage_slots'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    unlocked_at_clan_level = EXCLUDED.unlocked_at_clan_level,
    max_level = EXCLUDED.max_level,
    base_honor_marks_cost = EXCLUDED.base_honor_marks_cost,
    cost_multiplier = EXCLUDED.cost_multiplier,
    effect_per_level = EXCLUDED.effect_per_level,
    effect_type = EXCLUDED.effect_type,
    stat = EXCLUDED.stat;
