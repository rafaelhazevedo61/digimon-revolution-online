BEGIN;

ALTER TABLE world_boss_attacks
    ADD COLUMN request_id VARCHAR(120),
    ADD COLUMN remaining_hp_after INT NOT NULL DEFAULT 0,
    ADD COLUMN win_chance INT NOT NULL DEFAULT 0,
    ADD COLUMN defeated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN defeated_reward_xp INT NOT NULL DEFAULT 0,
    ADD COLUMN defeated_reward_bits INT NOT NULL DEFAULT 0,
    ADD COLUMN daily_attacks_remaining INT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uk_world_boss_attacks_request
    ON world_boss_attacks (world_boss_id, player_id, request_id)
    WHERE request_id IS NOT NULL;

CREATE TABLE world_boss_rewards (
    id UUID PRIMARY KEY,
    world_boss_id UUID NOT NULL REFERENCES world_boss_instances(id),
    source_attack_id UUID NOT NULL REFERENCES world_boss_attacks(id),
    recipient_player_id UUID NOT NULL REFERENCES players(id),
    recipient_digimon_id UUID NOT NULL REFERENCES digimons(id),
    chest_definition_id BIGINT NOT NULL REFERENCES chest_definitions(id),
    reward_type VARCHAR(30) NOT NULL,
    event_key VARCHAR(180) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_world_boss_rewards_event_key UNIQUE (event_key)
);

CREATE INDEX idx_world_boss_rewards_boss_id
    ON world_boss_rewards (world_boss_id);

CREATE INDEX idx_world_boss_rewards_recipient
    ON world_boss_rewards (recipient_player_id, world_boss_id);

CREATE INDEX idx_world_boss_rewards_source_attack
    ON world_boss_rewards (source_attack_id);

INSERT INTO loot_tables (
    code,
    name,
    description,
    active,
    min_items,
    max_items
)
VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'Loot Table Baú Boss Mundial Apocalymon — Tentativa', 'Pool inicial do Baú concedido por tentativa no Boss Mundial Apocalymon.', TRUE, 1, 2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'Loot Table Baú Boss Mundial Apocalymon — Maior Dano', 'Pool inicial do Baú concedido ao maior dano acumulado.', TRUE, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'Loot Table Baú Boss Mundial Apocalymon — Golpe Final', 'Pool inicial do Baú concedido ao jogador do golpe final.', TRUE, 1, 2)
ON CONFLICT (code) DO NOTHING;

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT lt.id, seed.rarity, seed.weight
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'COMMON',    65),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'RARE',      25),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'EPIC',        8),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'LEGENDARY',   2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    40),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      30),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      20),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 10),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    50),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      30),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',      15),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'LEGENDARY',  5)
) AS seed(table_code, rarity, weight) ON seed.table_code = lt.code
ON CONFLICT (loot_table_id, rarity) DO NOTHING;

INSERT INTO loot_table_entries (
    loot_table_id,
    rarity,
    item_type,
    material_code,
    weight,
    min_quantity,
    max_quantity,
    active
)
SELECT lt.id, seed.rarity, seed.item_type, seed.material_code,
       seed.weight, seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'POTION_SMALL',       NULL,               40, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'TRAINING_STONE',     NULL,               35, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'DATA_CORE',          NULL,               25, 1, 2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_FIRE',      NULL,               35, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_WATER',     NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_NATURE',    NULL,               35, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'EPIC',      'INCUBATOR_COMMON',   NULL,              100, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'LEGENDARY', 'INCUBATOR_RARE',     NULL,              100, 1, 1),

    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'TRAINING_STONE',     NULL,               35, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'DATA_CORE',          NULL,               35, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'REFINEMENT_STONE',   NULL,               30, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON',  35, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_GABUMON', 35, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'INCUBATOR_RARE',      NULL,              30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'INCUBATOR_RARE',      NULL,              50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_GREYMON', 50, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 'INCUBATOR_EPIC',      NULL,              60, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_WARGREYMON',40, 1, 3),

    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'POTION_SMALL',       NULL,               35, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'DATA_CORE',          NULL,               35, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'TRAINING_STONE',     NULL,               30, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_FIRE',      NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_WATER',     NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_NATURE',    NULL,               20, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON', 20, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',      'INCUBATOR_RARE',      NULL,              100, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'LEGENDARY', 'INCUBATOR_EPIC',      NULL,              100, 1, 1)
) AS seed(table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code;

INSERT INTO item_definitions (
    code,
    name,
    description,
    category,
    stackable,
    buy_price,
    sell_price,
    tradable,
    sellable,
    usable,
    max_stack,
    rarity,
    icon
)
VALUES
    ('CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT',    'Baú Boss Mundial Apocalymon — Tentativa',    'Baú recebido por uma tentativa válida contra Apocalymon.',    'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_boss_world_apocalymon_attempt'),
    ('CHEST_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'Baú Boss Mundial Apocalymon — Maior Dano',  'Baú recebido pelo maior dano acumulado na derrota.',           'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'RARE',   'chest_boss_world_apocalymon_top_damage'),
    ('CHEST_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'Baú Boss Mundial Apocalymon — Golpe Final',  'Baú recebido pelo jogador que desferiu o golpe final.',      'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'RARE',   'chest_boss_world_apocalymon_final_blow')
ON CONFLICT (code) DO NOTHING;

INSERT INTO chest_definitions (
    code,
    name,
    description,
    icon,
    loot_table_id,
    item_definition_id,
    tradable,
    active
)
SELECT seed.chest_code,
       seed.name,
       seed.description,
       seed.icon,
       lt.id,
       item.id,
       TRUE,
       TRUE
FROM (VALUES
    ('CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT',    'Baú Boss Mundial Apocalymon — Tentativa',   'Baú recebido por uma tentativa válida contra Apocalymon.',   'chest_boss_world_apocalymon_attempt',    'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT'),
    ('CHEST_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'Baú Boss Mundial Apocalymon — Maior Dano', 'Baú recebido pelo maior dano acumulado na derrota.',          'chest_boss_world_apocalymon_top_damage', 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE'),
    ('CHEST_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'Baú Boss Mundial Apocalymon — Golpe Final', 'Baú recebido pelo jogador que desferiu o golpe final.',     'chest_boss_world_apocalymon_final_blow', 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW')
) AS seed(chest_code, name, description, icon, loot_table_code)
JOIN loot_tables lt ON lt.code = seed.loot_table_code
JOIN item_definitions item ON item.code = seed.chest_code
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active;

COMMIT;
