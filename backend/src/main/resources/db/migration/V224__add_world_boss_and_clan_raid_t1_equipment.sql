BEGIN;

-- V224: define explicitamente as pools LEGENDARY das recompensas especiais.
-- As demais raridades e entradas das Loot Tables permanecem inalteradas.
-- A raridade efetiva do equipamento é sorteada na abertura porque
-- equipment_rarity permanece NULL.

CREATE TEMP TABLE special_boss_legendary_entries (
    table_code VARCHAR(100) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    material_code VARCHAR(80),
    equipment_template_name VARCHAR(120),
    equipment_rarity VARCHAR(20),
    weight INT NOT NULL,
    min_quantity INT NOT NULL,
    max_quantity INT NOT NULL
) ON COMMIT DROP;

INSERT INTO special_boss_legendary_entries (
    table_code,
    item_type,
    material_code,
    equipment_template_name,
    equipment_rarity,
    weight,
    min_quantity,
    max_quantity
)
VALUES
    -- Incursão: tentativa — Cristal de Proteção / Martelo Kernel T1.
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',
     'REFINEMENT_PROTECTION', NULL, NULL, NULL, 50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',
     'EQUIPMENT', NULL, 'Martelo Kernel T1', NULL, 50, 1, 1),

    -- Incursão: maior dano — Núcleo de Ascensão 2–3 / Kernel Shell T1.
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
     'ASCENSION_CORE', NULL, NULL, NULL, 50, 2, 3),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',
     'EQUIPMENT', NULL, 'Kernel Shell T1', NULL, 50, 1, 1),

    -- Incursão: golpe final — Núcleo de Ascensão 1 / Kernel Core T1.
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW',
     'ASCENSION_CORE', NULL, NULL, NULL, 50, 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW',
     'EQUIPMENT', NULL, 'Kernel Core T1', NULL, 50, 1, 1),

    -- Chefe Mundial: tentativa — Cristal de Proteção / Lâmina do Overlord T1.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
     'REFINEMENT_PROTECTION', NULL, NULL, NULL, 50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
     'EQUIPMENT', NULL, 'Lâmina do Overlord T1', NULL, 50, 1, 1),

    -- Chefe Mundial: maior dano — Disco de Experiência 10% / Couraça do Overlord T1.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
     'XP_DISC_10', NULL, NULL, NULL, 50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
     'EQUIPMENT', NULL, 'Couraça do Overlord T1', NULL, 50, 1, 1),

    -- Chefe Mundial: golpe final — somente Coroa do Overlord T1.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW',
     'EQUIPMENT', NULL, 'Coroa do Overlord T1', NULL, 100, 1, 1);

DO $$
DECLARE
    missing_tables INT;
    missing_templates INT;
BEGIN
    SELECT COUNT(*) INTO missing_tables
    FROM special_boss_legendary_entries expected
    LEFT JOIN loot_tables table_row ON table_row.code = expected.table_code
    WHERE table_row.id IS NULL;

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Recompensas especiais: % Loot Table(s) não encontrada(s).', missing_tables;
    END IF;

    SELECT COUNT(*) INTO missing_templates
    FROM special_boss_legendary_entries expected
    LEFT JOIN equipment_templates template
      ON template.name = expected.equipment_template_name
     AND template.active = TRUE
    WHERE expected.item_type = 'EQUIPMENT'
      AND template.name IS NULL;

    IF missing_templates > 0 THEN
        RAISE EXCEPTION 'Recompensas especiais: % template(s) de equipamento não encontrado(s) ou inativo(s).', missing_templates;
    END IF;
END $$;

-- Limpa somente a pool LEGENDARY das seis Loot Tables especiais.
DELETE FROM loot_table_entries entry
USING loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND entry.rarity = 'LEGENDARY'
  AND EXISTS (
      SELECT 1
      FROM special_boss_legendary_entries expected
      WHERE expected.table_code = table_row.code
  );

-- Recria a composição definida acima, com pesos explícitos.
INSERT INTO loot_table_entries (
    loot_table_id,
    rarity,
    item_type,
    material_code,
    equipment_template_name,
    equipment_rarity,
    weight,
    min_quantity,
    max_quantity,
    active
)
SELECT
    table_row.id,
    'LEGENDARY',
    expected.item_type::VARCHAR,
    expected.material_code,
    expected.equipment_template_name,
    expected.equipment_rarity::VARCHAR,
    expected.weight,
    expected.min_quantity,
    expected.max_quantity,
    TRUE
FROM special_boss_legendary_entries expected
JOIN loot_tables table_row ON table_row.code = expected.table_code;

DO $$
DECLARE
    invalid_pools INT;
    invalid_entries INT;
BEGIN
    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT table_row.code
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND entry.rarity = 'LEGENDARY'
          AND EXISTS (
              SELECT 1
              FROM special_boss_legendary_entries expected
              WHERE expected.table_code = table_row.code
          )
        GROUP BY table_row.code
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    SELECT COUNT(*) INTO invalid_entries
    FROM (
        SELECT table_row.code
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND entry.rarity = 'LEGENDARY'
          AND EXISTS (
              SELECT 1
              FROM special_boss_legendary_entries expected
              WHERE expected.table_code = table_row.code
          )
        GROUP BY table_row.code
        HAVING COUNT(*) <> (
            SELECT COUNT(*)
            FROM special_boss_legendary_entries expected
            WHERE expected.table_code = table_row.code
        )
    ) invalid;

    IF invalid_pools > 0 OR invalid_entries > 0 THEN
        RAISE EXCEPTION
            'Recompensas especiais: % pool(s) não somam 100 e % pool(s) têm quantidade de entradas diferente da configuração.',
            invalid_pools, invalid_entries;
    END IF;
END $$;

COMMIT;
