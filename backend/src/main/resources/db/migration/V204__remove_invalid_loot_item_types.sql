-- V203 accidentally seeded item-definition codes as ItemType values.
-- The Java enum does not contain these values, causing /admin/loot-tables to fail
-- while hydrating LootTableEntryEntity. Remove only those invalid entries.
BEGIN;

DELETE FROM loot_table_entries
WHERE item_type IN ('REFINEMENT_SUCCESS_BOOST', 'REFINEMENT_PROTECTION');

COMMIT;
