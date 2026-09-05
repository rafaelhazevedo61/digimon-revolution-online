BEGIN;

-- V218: padroniza as recompensas da incursão de clã no mesmo modelo do
-- Boss Mundial: tentativa, maior dano acumulado e golpe final.
ALTER TABLE boss_definitions
    ADD COLUMN IF NOT EXISTS clan_raid_attempt_chest_definition_id BIGINT REFERENCES chest_definitions(id),
    ADD COLUMN IF NOT EXISTS clan_raid_top_damage_chest_definition_id BIGINT REFERENCES chest_definitions(id),
    ADD COLUMN IF NOT EXISTS clan_raid_final_blow_chest_definition_id BIGINT REFERENCES chest_definitions(id);

CREATE INDEX IF NOT EXISTS idx_boss_definitions_clan_raid_attempt_chest_id
    ON boss_definitions (clan_raid_attempt_chest_definition_id);
CREATE INDEX IF NOT EXISTS idx_boss_definitions_clan_raid_top_damage_chest_id
    ON boss_definitions (clan_raid_top_damage_chest_definition_id);
CREATE INDEX IF NOT EXISTS idx_boss_definitions_clan_raid_final_blow_chest_id
    ON boss_definitions (clan_raid_final_blow_chest_definition_id);

CREATE TABLE IF NOT EXISTS clan_raid_rewards (
    id                    UUID PRIMARY KEY,
    clan_raid_id          UUID         NOT NULL REFERENCES clan_raid_instances(id),
    source_attack_id      UUID         NOT NULL REFERENCES clan_raid_attacks(id),
    recipient_player_id   UUID         NOT NULL REFERENCES players(id),
    recipient_digimon_id  UUID         NOT NULL REFERENCES digimons(id),
    chest_definition_id   BIGINT       NOT NULL REFERENCES chest_definitions(id),
    reward_type           VARCHAR(30)  NOT NULL,
    event_key             VARCHAR(180) NOT NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_clan_raid_rewards_event_key UNIQUE (event_key),
    CONSTRAINT ck_clan_raid_reward_type_known CHECK (reward_type IN ('ATTEMPT', 'TOP_DAMAGE', 'FINAL_BLOW'))
);

CREATE INDEX IF NOT EXISTS idx_clan_raid_rewards_raid_id
    ON clan_raid_rewards (clan_raid_id);
CREATE INDEX IF NOT EXISTS idx_clan_raid_rewards_recipient
    ON clan_raid_rewards (recipient_player_id, clan_raid_id);
CREATE INDEX IF NOT EXISTS idx_clan_raid_rewards_source_attack
    ON clan_raid_rewards (source_attack_id);

INSERT INTO loot_tables (code, name, description, active, min_items, max_items)
VALUES
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',
     'Loot da Incursão de Clã — Tentativa',
     'Baú concedido por cada ataque válido contra Omegamon da incursão de clã.',
     TRUE, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
     'Loot da Incursão de Clã — Maior Dano',
     'Baú concedido ao jogador com maior dano acumulado na derrota da incursão.',
     TRUE, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW',
     'Loot da Incursão de Clã — Golpe Final',
     'Baú concedido ao jogador que desferir o golpe final da incursão.',
     TRUE, 1, 1)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    min_items = EXCLUDED.min_items,
    max_items = EXCLUDED.max_items,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

DELETE FROM loot_table_entries
WHERE loot_table_id IN (
    SELECT id
    FROM loot_tables
    WHERE code IN (
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
    )
);
DELETE FROM loot_table_rarity_weights
WHERE loot_table_id IN (
    SELECT id
    FROM loot_tables
    WHERE code IN (
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
        'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
    )
);

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT table_row.id, seed.rarity, seed.weight
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',    'COMMON',    65),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',    'RARE',      25),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',    'EPIC',       8),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',    'LEGENDARY',  2),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'COMMON',    35),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'RARE',      30),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'EPIC',      25),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'LEGENDARY', 10),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'COMMON',    45),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'RARE',      30),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'EPIC',      20),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'LEGENDARY',  5)
) AS seed(table_code, rarity, weight) ON seed.table_code = table_row.code;

-- Cada pool de raridade soma 100. As diferenças de quantidade preservam o
-- valor esperado sem transformar material comum em recompensa rara.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, seed.rarity, seed.item_type, NULL,
       seed.weight, seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'COMMON',    'REFINEMENT_STONE',         50, 2, 3),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'COMMON',    'DATA_CORE',                30, 3, 5),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'COMMON',    'XP_DISC_1',                20, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'RARE',      'XP_DISC_3',                50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'RARE',      'REFINEMENT_STONE',          50, 4, 5),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'EPIC',      'REFINEMENT_SUCCESS_BOOST', 100, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'LEGENDARY', 'REFINEMENT_PROTECTION',    100, 1, 1),

    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'COMMON',    'REFINEMENT_STONE',          100, 8, 12),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'RARE',      'REFINEMENT_SUCCESS_BOOST',   50, 2, 3),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'RARE',      'XP_DISC_5',                 50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'EPIC',      'REFINEMENT_PROTECTION',     50, 1, 2),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'EPIC',      'ASCENSION_CORE',             50, 2, 3),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'LEGENDARY', 'XP_DISC_10',                100, 1, 1),

    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'COMMON',    'REFINEMENT_STONE',          100, 5, 7),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'RARE',      'XP_DISC_5',                 50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'RARE',      'REFINEMENT_SUCCESS_BOOST',   50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'EPIC',      'REFINEMENT_PROTECTION',     50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'EPIC',      'ASCENSION_CORE',             50, 1, 1)
) AS seed(table_code, rarity, item_type, weight, min_quantity, max_quantity)
    ON seed.table_code = table_row.code;

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    ('CHEST_CLAN_RAID_OMEGAMON_ATTEMPT',
     'Baú da Incursão de Clã — Tentativa',
     'Baú recebido por uma tentativa válida contra Omegamon.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON',
     'chest_clan_raid_omegamon_attempt'),
    ('CHEST_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
     'Baú da Incursão de Clã — Maior Dano',
     'Baú recebido pelo maior dano acumulado na derrota de Omegamon.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'EPIC',
     'chest_clan_raid_omegamon_top_damage'),
    ('CHEST_CLAN_RAID_OMEGAMON_FINAL_BLOW',
     'Baú da Incursão de Clã — Golpe Final',
     'Baú recebido pelo jogador que desferiu o golpe final em Omegamon.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'RARE',
     'chest_clan_raid_omegamon_final_blow')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

INSERT INTO chest_definitions (
    code, name, description, icon, loot_table_id, item_definition_id,
    tradable, active, created_by, updated_by
)
SELECT seed.chest_code, seed.name, seed.description, seed.icon,
       table_row.id, item.id, TRUE, TRUE, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    ('CHEST_CLAN_RAID_OMEGAMON_ATTEMPT',
     'Baú da Incursão de Clã — Tentativa',
     'Baú recebido por uma tentativa válida contra Omegamon.',
     'chest_clan_raid_omegamon_attempt',
     'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT'),
    ('CHEST_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
     'Baú da Incursão de Clã — Maior Dano',
     'Baú recebido pelo maior dano acumulado na derrota de Omegamon.',
     'chest_clan_raid_omegamon_top_damage',
     'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE'),
    ('CHEST_CLAN_RAID_OMEGAMON_FINAL_BLOW',
     'Baú da Incursão de Clã — Golpe Final',
     'Baú recebido pelo jogador que desferiu o golpe final em Omegamon.',
     'chest_clan_raid_omegamon_final_blow',
     'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW')
) AS seed(chest_code, name, description, icon, loot_table_code)
JOIN loot_tables table_row ON table_row.code = seed.loot_table_code
JOIN item_definitions item ON item.code = seed.chest_code
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

UPDATE boss_definitions boss
SET clan_raid_attempt_chest_definition_id = attempt.id,
    clan_raid_top_damage_chest_definition_id = top_damage.id,
    clan_raid_final_blow_chest_definition_id = final_blow.id
FROM chest_definitions attempt
JOIN chest_definitions top_damage
  ON top_damage.code = 'CHEST_CLAN_RAID_OMEGAMON_TOP_DAMAGE'
JOIN chest_definitions final_blow
  ON final_blow.code = 'CHEST_CLAN_RAID_OMEGAMON_FINAL_BLOW'
WHERE boss.code = 'CLAN_RAID_OMEGAMON'
  AND attempt.code = 'CHEST_CLAN_RAID_OMEGAMON_ATTEMPT';

DO $$
DECLARE
    invalid_rarity_pools INT;
    invalid_item_pools INT;
    missing_catalog INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_rarity_pools
    FROM (
        SELECT table_row.code
        FROM loot_table_rarity_weights weights
        JOIN loot_tables table_row ON table_row.id = weights.loot_table_id
        WHERE table_row.code LIKE 'LOOT_TABLE_CLAN_RAID_OMEGAMON_%'
        GROUP BY table_row.code
        HAVING SUM(weights.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO invalid_item_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE table_row.code LIKE 'LOOT_TABLE_CLAN_RAID_OMEGAMON_%'
          AND entry.active = TRUE
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO missing_catalog
    FROM boss_definitions
    WHERE code = 'CLAN_RAID_OMEGAMON'
      AND (
          clan_raid_attempt_chest_definition_id IS NULL
          OR clan_raid_top_damage_chest_definition_id IS NULL
          OR clan_raid_final_blow_chest_definition_id IS NULL
      );

    IF invalid_rarity_pools > 0 OR invalid_item_pools > 0 OR missing_catalog > 0 THEN
        RAISE EXCEPTION
            'Clan Raid: % pool(s) de raridade inválida(s), % pool(s) internas inválida(s), % boss(es) sem baús configurados.',
            invalid_rarity_pools, invalid_item_pools, missing_catalog;
    END IF;
END $$;

COMMIT;
