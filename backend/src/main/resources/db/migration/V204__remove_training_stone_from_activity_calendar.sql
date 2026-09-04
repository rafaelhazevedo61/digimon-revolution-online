BEGIN;

-- Remove somente Pedra de Treino das Loot Tables do Calendário.
-- Os pesos de raridade e todas as demais entradas permanecem inalterados.
DELETE FROM loot_table_entries
WHERE loot_table_id IN (
    SELECT id
    FROM loot_tables
    WHERE code IN (
        'LOOT_TABLE_ACTIVITY_CALENDAR',
        'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
    )
)
AND item_type = 'TRAINING_STONE'
AND material_code IS NULL;

COMMIT;
