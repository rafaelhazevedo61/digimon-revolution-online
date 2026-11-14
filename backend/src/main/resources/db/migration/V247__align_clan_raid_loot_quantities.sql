-- Referência: aba Incursão do Mapeamento Consolidado de Drops.
UPDATE loot_table_rarity_weights AS rarity_weight
SET weight = expected.weight
FROM loot_tables AS loot
CROSS JOIN (VALUES ('COMMON', 50), ('RARE', 30), ('EPIC', 15), ('LEGENDARY', 5))
    AS expected(rarity, weight)
WHERE loot.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
  AND rarity_weight.loot_table_id = loot.id
  AND rarity_weight.rarity = expected.rarity;

UPDATE loot_table_entries AS entry
SET min_quantity = expected.min_quantity,
    max_quantity = expected.max_quantity
FROM loot_tables AS loot
JOIN (VALUES
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'COMMON', 'REFINEMENT_STONE', 1, 2),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'COMMON', 'DATA_CORE', 2, 4),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT', 'RARE', 'REFINEMENT_STONE', 3, 4),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'COMMON', 'REFINEMENT_STONE', 5, 8),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'RARE', 'REFINEMENT_SUCCESS_BOOST', 1, 2),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'EPIC', 'REFINEMENT_PROTECTION', 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE', 'EPIC', 'ASCENSION_CORE', 1, 1),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW', 'COMMON', 'REFINEMENT_STONE', 3, 5)
) AS expected(code, rarity, item_type, min_quantity, max_quantity)
    ON expected.code = loot.code
WHERE entry.loot_table_id = loot.id
  AND entry.active
  AND entry.rarity = expected.rarity
  AND entry.item_type = expected.item_type;

UPDATE loot_table_entries AS entry
SET item_type = 'XP_DISC_3'
FROM loot_tables AS loot
WHERE entry.loot_table_id = loot.id
  AND loot.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
  AND entry.active
  AND entry.rarity = 'RARE'
  AND entry.item_type = 'XP_DISC_5';
