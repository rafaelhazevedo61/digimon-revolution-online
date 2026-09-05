BEGIN;

-- A seed consolidada da Arena usa a família LOOT_TABLE_CHEST_ARENA_*.
-- Os baús oficiais passam a apontar para essa família; a configuração antiga
-- da V109 permanece preservada, mas desativada.
--
-- A V109/V205 criava e atualizava somente LOOT_TABLE_ARENA_*. Em ambientes
-- aplicados até a V205, as tabelas consolidadas ainda não existem. Criamos a
-- nova família e copiamos a configuração vigente antes de ativá-la.
INSERT INTO loot_tables (
    code, name, description, active, min_items, max_items, created_by, updated_by
)
SELECT
    REPLACE(source.code, 'LOOT_TABLE_ARENA_', 'LOOT_TABLE_CHEST_ARENA_'),
    source.name,
    source.description,
    TRUE,
    source.min_items,
    source.max_items,
    'SYSTEM',
    'SYSTEM'
FROM loot_tables source
WHERE source.code IN (
    'LOOT_TABLE_ARENA_BRONZE',
    'LOOT_TABLE_ARENA_PRATA',
    'LOOT_TABLE_ARENA_OURO',
    'LOOT_TABLE_ARENA_PLATINA',
    'LOOT_TABLE_ARENA_DIAMANTE'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    min_items = EXCLUDED.min_items,
    max_items = EXCLUDED.max_items,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT consolidated.id, source_weight.rarity, source_weight.weight
FROM loot_table_rarity_weights source_weight
JOIN loot_tables source_table
  ON source_table.id = source_weight.loot_table_id
JOIN loot_tables consolidated
  ON consolidated.code = REPLACE(source_table.code, 'LOOT_TABLE_ARENA_', 'LOOT_TABLE_CHEST_ARENA_')
WHERE source_table.code IN (
    'LOOT_TABLE_ARENA_BRONZE',
    'LOOT_TABLE_ARENA_PRATA',
    'LOOT_TABLE_ARENA_OURO',
    'LOOT_TABLE_ARENA_PLATINA',
    'LOOT_TABLE_ARENA_DIAMANTE'
)
ON CONFLICT (loot_table_id, rarity) DO UPDATE SET
    weight = EXCLUDED.weight;

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    weight, min_quantity, max_quantity, active
)
SELECT
    consolidated.id,
    source_entry.rarity,
    source_entry.item_type,
    source_entry.material_code,
    source_entry.weight,
    source_entry.min_quantity,
    source_entry.max_quantity,
    source_entry.active
FROM loot_table_entries source_entry
JOIN loot_tables source_table
  ON source_table.id = source_entry.loot_table_id
JOIN loot_tables consolidated
  ON consolidated.code = REPLACE(source_table.code, 'LOOT_TABLE_ARENA_', 'LOOT_TABLE_CHEST_ARENA_')
WHERE source_table.code IN (
    'LOOT_TABLE_ARENA_BRONZE',
    'LOOT_TABLE_ARENA_PRATA',
    'LOOT_TABLE_ARENA_OURO',
    'LOOT_TABLE_ARENA_PLATINA',
    'LOOT_TABLE_ARENA_DIAMANTE'
)
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries existing
      WHERE existing.loot_table_id = consolidated.id
        AND existing.rarity = source_entry.rarity
        AND existing.item_type = source_entry.item_type
        AND existing.material_code IS NOT DISTINCT FROM source_entry.material_code
        AND existing.weight = source_entry.weight
        AND existing.min_quantity = source_entry.min_quantity
        AND existing.max_quantity = source_entry.max_quantity
  );

UPDATE loot_tables
SET active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code IN (
    'LOOT_TABLE_CHEST_ARENA_BRONZE',
    'LOOT_TABLE_CHEST_ARENA_PRATA',
    'LOOT_TABLE_CHEST_ARENA_OURO',
    'LOOT_TABLE_CHEST_ARENA_PLATINA',
    'LOOT_TABLE_CHEST_ARENA_DIAMANTE'
);

-- Redireciona os baús que eram vinculados às tabelas da V109.
UPDATE chest_definitions chest
SET loot_table_id = consolidated.id,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
FROM loot_tables consolidated
WHERE consolidated.code = CASE chest.code
    WHEN 'CHEST_ARENA_BRONZE' THEN 'LOOT_TABLE_CHEST_ARENA_BRONZE'
    WHEN 'CHEST_ARENA_PRATA' THEN 'LOOT_TABLE_CHEST_ARENA_PRATA'
    WHEN 'CHEST_ARENA_OURO' THEN 'LOOT_TABLE_CHEST_ARENA_OURO'
    WHEN 'CHEST_ARENA_PLATINA' THEN 'LOOT_TABLE_CHEST_ARENA_PLATINA'
    WHEN 'CHEST_ARENA_DIAMANTE' THEN 'LOOT_TABLE_CHEST_ARENA_DIAMANTE'
END
AND chest.code IN (
    'CHEST_ARENA_BRONZE',
    'CHEST_ARENA_PRATA',
    'CHEST_ARENA_OURO',
    'CHEST_ARENA_PLATINA',
    'CHEST_ARENA_DIAMANTE'
);

-- Desativa somente as cinco Loot Tables antigas da V109.
UPDATE loot_tables
SET active = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code IN (
    'LOOT_TABLE_ARENA_BRONZE',
    'LOOT_TABLE_ARENA_PRATA',
    'LOOT_TABLE_ARENA_OURO',
    'LOOT_TABLE_ARENA_PLATINA',
    'LOOT_TABLE_ARENA_DIAMANTE'
);

-- Falha com rollback se a seed consolidada não estiver presente ou se algum
-- baú oficial não tiver sido redirecionado.
DO $$
DECLARE
    missing_tables INT;
    invalid_links INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_tables
    FROM (VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM loot_tables lt WHERE lt.code = required.code
    );

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Arena: a seed consolidada está incompleta; faltam % Loot Table(s).', missing_tables;
    END IF;

    SELECT COUNT(*)
    INTO invalid_links
    FROM (VALUES
        ('CHEST_ARENA_BRONZE', 'LOOT_TABLE_CHEST_ARENA_BRONZE'),
        ('CHEST_ARENA_PRATA', 'LOOT_TABLE_CHEST_ARENA_PRATA'),
        ('CHEST_ARENA_OURO', 'LOOT_TABLE_CHEST_ARENA_OURO'),
        ('CHEST_ARENA_PLATINA', 'LOOT_TABLE_CHEST_ARENA_PLATINA'),
        ('CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_CHEST_ARENA_DIAMANTE')
    ) AS expected(chest_code, table_code)
    WHERE NOT EXISTS (
        SELECT 1
        FROM chest_definitions chest
        JOIN loot_tables lt ON lt.id = chest.loot_table_id
        WHERE chest.code = expected.chest_code
          AND lt.code = expected.table_code
    );

    IF invalid_links > 0 THEN
        RAISE EXCEPTION 'Arena: existem % vínculo(s) de baú apontando para a Loot Table errada.', invalid_links;
    END IF;
END $$;

COMMIT;
