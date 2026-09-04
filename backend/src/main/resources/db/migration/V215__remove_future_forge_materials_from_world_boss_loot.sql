BEGIN;

-- V215: ajusta as pools do World Boss sem alterar V110 já executada.
-- Equipamentos e materiais de forja/forja avançada ficam fora deste escopo.
-- Fragmentos de evolução, consumíveis, dados, Digitamas e incubadoras
-- permanecem como recompensas suportadas.

DELETE FROM loot_table_entries entry
USING loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code IN (
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
  );

-- Mantém os pesos de raridade definidos em V110: tentativa 65/25/8/2,
-- maior dano 40/30/20/10 e golpe final 50/30/15/5.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT lt.id, seed.rarity, seed.item_type, seed.material_code,
       seed.weight, seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    -- Tentativa: poção, treino, dados, Digitamas e incubadoras.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'POTION_SMALL',       NULL,                40, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'TRAINING_STONE',     NULL,                35, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'DATA_CORE',          NULL,                25, 1, 2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_FIRE',      NULL,                35, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_WATER',     NULL,                30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'DIGITAMA_NATURE',    NULL,                35, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'EPIC',      'INCUBATOR_COMMON',   NULL,               100, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'LEGENDARY', 'INCUBATOR_RARE',     NULL,               100, 1, 1),

    -- Maior dano: remove a Pedra de Refinamento e normaliza o pool comum.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'TRAINING_STONE',     NULL,                50, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'DATA_CORE',          NULL,                50, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON',   35, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_GABUMON',  35, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'INCUBATOR_RARE',      NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'INCUBATOR_RARE',      NULL,               50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_GREYMON', 50, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 'INCUBATOR_EPIC',      NULL,               60, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_WARGREYMON',40, 1, 3),

    -- Golpe final: mantém consumíveis, dados, Digitamas e fragmentos.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'POTION_SMALL',       NULL,               35, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'DATA_CORE',          NULL,               35, 1, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    'TRAINING_STONE',     NULL,               30, 1, 4),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_FIRE',      NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_WATER',     NULL,               30, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'DIGITAMA_NATURE',    NULL,               20, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON',  20, 1, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',      'INCUBATOR_RARE',      NULL,              100, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'LEGENDARY', 'INCUBATOR_EPIC',      NULL,              100, 1, 1)
) AS seed(table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code;

DO $$
DECLARE
    invalid_pools INT;
    forge_entries INT;
    equipment_entries INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_pools
    FROM (
        SELECT lt.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables lt ON lt.id = entry.loot_table_id
        WHERE lt.code IN (
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
        )
          AND entry.active = TRUE
        GROUP BY lt.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO forge_entries
    FROM loot_table_entries entry
    JOIN loot_tables lt ON lt.id = entry.loot_table_id
    WHERE lt.code IN (
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
    )
      AND entry.active = TRUE
      AND entry.item_type IN (
          'REFINEMENT_STONE', 'REFINEMENT_SUCCESS_BOOST',
          'REFINEMENT_PROTECTION', 'ASCENSION_CORE',
          'RARITY_PRESERVATION', 'RARITY_REROLL'
      );

    SELECT COUNT(*)
    INTO equipment_entries
    FROM loot_table_entries entry
    JOIN loot_tables lt ON lt.id = entry.loot_table_id
    WHERE lt.code IN (
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
        'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
    )
      AND entry.active = TRUE
      AND entry.item_type IN ('EQUIPMENT', 'EQUIPMENT_POOL');

    IF invalid_pools > 0 OR forge_entries > 0 OR equipment_entries > 0 THEN
        RAISE EXCEPTION
            'World Boss: % pool(s) inválida(s), % material(is) de forja e % entrada(s) de equipamento.',
            invalid_pools, forge_entries, equipment_entries;
    END IF;
END $$;

COMMIT;
