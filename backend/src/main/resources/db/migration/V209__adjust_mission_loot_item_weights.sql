BEGIN;

-- V209: substitui integralmente as pools legadas das missões.
-- A V208 já foi executada e permanece imutável; esta migration remove as
-- entradas ativas existentes e recria a composição canônica com os pesos
-- internos corrigidos. A quantidade legada de entradas não é usada como
-- premissa, pois a pool é reconstruída do zero.
--
-- Regra-base da referência:
--   - Digitama/incubadora: mesma categoria -> 50/50 após normalização;
--   - recompensas de progressão específicas: mesma categoria -> 50/50;
--   - XP Disc de entrada (60) versus núcleo de dados (30): 67/33 após normalização.

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
    ('LOOT_TABLE_MISSION_MISSION_1', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_1', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_4', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_5', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'RARE', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'EPIC', 'STORAGE_SLOT_1', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'EPIC', 'CODE_INFINITE', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'LEGENDARY', 'XP_DISC_1', NULL, 67, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_6', 'LEGENDARY', 'DATA_CORE', NULL, 33, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_BABY_II', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_BABY_II', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ROOKIE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_CHAMPION', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_ULTIMATE', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'COMMON', 'LOOT_CHEST', 'CHEST_FRAGMENT_MEGA', 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'RARE', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'EPIC', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'EPIC', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'LEGENDARY', 'XP_DISC_1', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'RARE', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'EPIC', 'STORAGE_SLOT_1', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'EPIC', 'CODE_INFINITE', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'LEGENDARY', 'XP_DISC_1', NULL, 67, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'LEGENDARY', 'DATA_CORE', NULL, 33, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'COMMON', 'REFINEMENT_STONE', NULL, 100, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'RARE', 'LOOT_CHEST', 'CHEST_DIGITAMA_RANDOM', 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'RARE', 'INCUBATOR_COMMON', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'EPIC', 'STORAGE_SLOT_1', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'EPIC', 'CODE_INFINITE', NULL, 50, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'LEGENDARY', 'XP_DISC_1', NULL, 67, 1, 1),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'LEGENDARY', 'DATA_CORE', NULL, 33, 1, 1)
;

DO $$
DECLARE
    missing_tables INT;
    invalid_entries INT;
    invalid_pools INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_tables
    FROM (
        SELECT DISTINCT expected.table_code
        FROM mission_expected_entries expected
        LEFT JOIN loot_tables table_row ON table_row.code = expected.table_code
        WHERE table_row.id IS NULL
    ) missing;

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Missões: % Loot Table(s) canônica(s) não encontrada(s).', missing_tables;
    END IF;

    -- A composição anterior inteira deixa de ser elegível.
    DELETE FROM loot_table_entries entry
    USING loot_tables table_row
    WHERE entry.loot_table_id = table_row.id
      AND table_row.code LIKE 'LOOT_TABLE_MISSION_%';

    -- Recria exclusivamente as entradas canônicas da V208, com os pesos
    -- internos já normalizados conforme a referência.
    INSERT INTO loot_table_entries (
        loot_table_id, rarity, item_type, material_code,
        weight, min_quantity, max_quantity, active
    )
    SELECT
        table_row.id,
        expected.rarity,
        expected.item_type,
        expected.material_code,
        expected.weight,
        expected.min_quantity,
        expected.max_quantity,
        TRUE
    FROM mission_expected_entries expected
    JOIN loot_tables table_row ON table_row.code = expected.table_code;

    SELECT COUNT(*)
    INTO invalid_entries
    FROM (
        SELECT expected.table_code, expected.rarity, expected.item_type, expected.material_code
        FROM mission_expected_entries expected
        JOIN loot_tables table_row ON table_row.code = expected.table_code
        LEFT JOIN loot_table_entries actual
          ON actual.loot_table_id = table_row.id
         AND actual.rarity = expected.rarity
         AND actual.item_type = expected.item_type
         AND actual.material_code IS NOT DISTINCT FROM expected.material_code
         AND actual.active = TRUE
         AND actual.weight = expected.weight
         AND actual.min_quantity = expected.min_quantity
         AND actual.max_quantity = expected.max_quantity
        WHERE actual.id IS NULL
    ) invalid;

    IF invalid_entries > 0 THEN
        RAISE EXCEPTION 'Missões: % entrada(s) canônica(s) não foram recriadas corretamente.', invalid_entries;
    END IF;

    SELECT COUNT(*)
    INTO invalid_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE table_row.code LIKE 'LOOT_TABLE_MISSION_%'
          AND entry.active = TRUE
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION 'Missões: soma de pesos internos diferente de 100 em % pool(s) após a V209.', invalid_pools;
    END IF;
END $$;

COMMIT;
