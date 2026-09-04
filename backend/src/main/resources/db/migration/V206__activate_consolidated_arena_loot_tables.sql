BEGIN;

-- A seed consolidada da Arena usa a família LOOT_TABLE_CHEST_ARENA_*.
-- Os baús oficiais passam a apontar para essa família; a configuração antiga
-- da V109 permanece preservada, mas desativada.
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
