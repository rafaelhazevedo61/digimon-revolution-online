-- V203 created Arena entries under LOOT_TABLE_CHEST_ARENA_* while the
-- existing chest_definitions consume LOOT_TABLE_ARENA_*. Move the configured
-- weights and entries to the tables actually referenced by the application.
BEGIN;

WITH mapping(source_code, target_code) AS (
    VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO', 'LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_ARENA_DIAMANTE')
)
DELETE FROM loot_table_entries entry
USING mapping, loot_tables target
WHERE target.code = mapping.target_code
  AND entry.loot_table_id = target.id;

WITH mapping(source_code, target_code) AS (
    VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO', 'LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_ARENA_DIAMANTE')
)
DELETE FROM loot_table_rarity_weights rarity_weight
USING mapping, loot_tables target
WHERE target.code = mapping.target_code
  AND rarity_weight.loot_table_id = target.id;

WITH mapping(source_code, target_code) AS (
    VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO', 'LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_ARENA_DIAMANTE')
)
INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT target.id, source_weight.rarity, source_weight.weight
FROM mapping
JOIN loot_tables source ON source.code = mapping.source_code
JOIN loot_tables target ON target.code = mapping.target_code
JOIN loot_table_rarity_weights source_weight ON source_weight.loot_table_id = source.id;

WITH mapping(source_code, target_code) AS (
    VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO', 'LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_ARENA_DIAMANTE')
)
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    weight, min_quantity, max_quantity, active
)
SELECT target.id, source_entry.rarity, source_entry.item_type,
       source_entry.material_code, source_entry.weight,
       source_entry.min_quantity, source_entry.max_quantity,
       source_entry.active
FROM mapping
JOIN loot_tables source ON source.code = mapping.source_code
JOIN loot_tables target ON target.code = mapping.target_code
JOIN loot_table_entries source_entry ON source_entry.loot_table_id = source.id;

WITH mapping(source_code, target_code) AS (
    VALUES
        ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_CHEST_ARENA_OURO', 'LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LOOT_TABLE_ARENA_DIAMANTE')
)
UPDATE loot_tables source
SET active = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
FROM mapping
WHERE source.code = mapping.source_code;

COMMIT;
