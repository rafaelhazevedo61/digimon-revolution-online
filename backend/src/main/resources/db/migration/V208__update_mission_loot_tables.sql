BEGIN;

-- V208: atualização das Loot Tables das 18 missões conforme a planilha consolidada.
-- Equipamentos T1 permanecem fora até existir um código de catálogo suportado pelo backend.
CREATE TEMP TABLE mission_expected_rarity_weights (
    table_code VARCHAR(80) NOT NULL,
    rarity VARCHAR(20) NOT NULL,
    weight INT NOT NULL
) ON COMMIT DROP;

INSERT INTO mission_expected_rarity_weights (table_code, rarity, weight)
VALUES
    ('LOOT_TABLE_MISSION_MISSION_1', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_1', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_1', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_1', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_2', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_2', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_2', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_3', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_3', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_3', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_3', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_4', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_4', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_4', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_4', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_5', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_5', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_5', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_5', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_6', 'COMMON', 70),
    ('LOOT_TABLE_MISSION_MISSION_6', 'RARE', 20),
    ('LOOT_TABLE_MISSION_MISSION_6', 'EPIC', 8),
    ('LOOT_TABLE_MISSION_MISSION_6', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'LEGENDARY', 5),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'LEGENDARY', 5),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'LEGENDARY', 5),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'LEGENDARY', 5),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'LEGENDARY', 5),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'COMMON', 60),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'RARE', 28),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'EPIC', 10),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'LEGENDARY', 2),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'COMMON', 50),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'RARE', 30),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'EPIC', 15),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'LEGENDARY', 5)
;

CREATE TEMP TABLE mission_expected_entries (
    table_code VARCHAR(80) NOT NULL,
    rarity VARCHAR(20) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    material_code VARCHAR(80),
    weight INT NOT NULL,
    min_quantity INT NOT NULL,
    max_quantity INT NOT NULL
) ON COMMIT DROP;

INSERT INTO mission_expected_entries (
    table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity
)
VALUES
    ('LOOT_TABLE_MISSION_MISSION_1', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_BABY_II', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'RARE', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'EPIC', 'STORAGE_SLOT_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'EPIC', 'CODE_INFINITE', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'LEGENDARY', 'XP_DISC_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'LEGENDARY', 'DATA_CORE', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_BABY_II', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_BABY_II', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'RARE', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'EPIC', 'STORAGE_SLOT_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'EPIC', 'CODE_INFINITE', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'LEGENDARY', 'XP_DISC_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'LEGENDARY', 'DATA_CORE', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'RARE', 'INCUBATOR_COMMON', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'EPIC', 'STORAGE_SLOT_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'EPIC', 'CODE_INFINITE', NULL, 40, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'LEGENDARY', 'XP_DISC_1', NULL, 60, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'LEGENDARY', 'DATA_CORE', NULL, 40, 1, 1)
;

DO $$
DECLARE
    missing_tables INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_tables
    FROM mission_expected_rarity_weights expected
    LEFT JOIN loot_tables lt ON lt.code = expected.table_code
    WHERE lt.id IS NULL;

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Missões: % referência(s) de Loot Table não encontrada(s).', missing_tables;
    END IF;
END $$;

-- Atualiza os pesos de raridade, preservando os vínculos e as quantidades das tabelas.
UPDATE loot_table_rarity_weights rarity_weight
SET weight = expected.weight
FROM loot_tables lt, mission_expected_rarity_weights expected
WHERE rarity_weight.loot_table_id = lt.id
  AND expected.table_code = lt.code
  AND expected.rarity = rarity_weight.rarity;

-- Desativa entradas antigas/legadas para que a composição aprovada seja a única elegível.
UPDATE loot_table_entries entry
SET active = FALSE
FROM loot_tables lt
WHERE entry.loot_table_id = lt.id
  AND EXISTS (
      SELECT 1
      FROM mission_expected_rarity_weights expected
      WHERE expected.table_code = lt.code
  );

-- Reativa e atualiza uma única linha existente para cada entrada esperada.
WITH ranked_entries AS (
    SELECT
        entry.id,
        expected.weight,
        expected.min_quantity,
        expected.max_quantity,
        ROW_NUMBER() OVER (
            PARTITION BY entry.loot_table_id, entry.rarity, entry.item_type, entry.material_code
            ORDER BY entry.id
        ) AS row_number
    FROM loot_table_entries entry
    JOIN loot_tables lt ON lt.id = entry.loot_table_id
    JOIN mission_expected_entries expected
      ON expected.table_code = lt.code
     AND expected.rarity = entry.rarity
     AND expected.item_type = entry.item_type
     AND expected.material_code IS NOT DISTINCT FROM entry.material_code
)
UPDATE loot_table_entries entry
SET weight = ranked_entries.weight,
    min_quantity = ranked_entries.min_quantity,
    max_quantity = ranked_entries.max_quantity,
    active = TRUE
FROM ranked_entries
WHERE entry.id = ranked_entries.id
  AND ranked_entries.row_number = 1;

-- Insere as entradas aprovadas que não existiam no legado.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight, min_quantity, max_quantity, active
)
SELECT
    lt.id,
    expected.rarity,
    expected.item_type,
    expected.material_code,
    expected.weight,
    expected.min_quantity,
    expected.max_quantity,
    TRUE
FROM mission_expected_entries expected
JOIN loot_tables lt ON lt.code = expected.table_code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = lt.id
      AND existing.rarity = expected.rarity
      AND existing.item_type = expected.item_type
      AND existing.material_code IS NOT DISTINCT FROM expected.material_code
);

DO $$
DECLARE
    invalid_rarity_pools INT;
    invalid_entry_pools INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_rarity_pools
    FROM (
        SELECT lt.code, rarity_weight.rarity
        FROM loot_table_rarity_weights rarity_weight
        JOIN loot_tables lt ON lt.id = rarity_weight.loot_table_id
        JOIN mission_expected_rarity_weights expected
          ON expected.table_code = lt.code
         AND expected.rarity = rarity_weight.rarity
        WHERE rarity_weight.weight <> expected.weight
    ) invalid_rarity;

    SELECT COUNT(*)
    INTO invalid_entry_pools
    FROM (
        SELECT lt.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables lt ON lt.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND EXISTS (
              SELECT 1
              FROM mission_expected_rarity_weights expected
              WHERE expected.table_code = lt.code
          )
        GROUP BY lt.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid_entries;

    IF invalid_rarity_pools > 0 THEN
        RAISE EXCEPTION 'Missões: % peso(s) de raridade não corresponde(m) à configuração esperada.', invalid_rarity_pools;
    END IF;

    IF invalid_entry_pools > 0 THEN
        RAISE EXCEPTION 'Missões: soma de pesos internos diferente de 100 em % pool(s).', invalid_entry_pools;
    END IF;
END $$;

COMMIT;
