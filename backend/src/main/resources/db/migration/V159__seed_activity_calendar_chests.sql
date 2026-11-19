-- O conteúdo das duas Loot Tables permanece administrável pelo painel Admin.
INSERT INTO loot_tables (code, name, description, active, min_items, max_items)
VALUES
 ('LOOT_TABLE_ACTIVITY_CALENDAR', 'Loot Table Calendário de Atividades', 'Recompensa diária do calendário; configurar pelo Admin.', TRUE, 1, 2),
 ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'Loot Table Conclusão Mensal', 'Recompensa exclusiva por completar todos os dias; configurar pelo Admin.', TRUE, 1, 3)
ON CONFLICT (code) DO NOTHING;

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT lt.id, seed.rarity, seed.weight
FROM loot_tables lt
CROSS JOIN (VALUES ('COMMON', 70), ('RARE', 20), ('EPIC', 8), ('LEGENDARY', 2)) AS seed(rarity, weight)
WHERE lt.code IN ('LOOT_TABLE_ACTIVITY_CALENDAR', 'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY')
ON CONFLICT (loot_table_id, rarity) DO NOTHING;

INSERT INTO item_definitions (code, name, description, category, stackable, buy_price, sell_price, tradable, sellable, usable, max_stack, rarity, icon)
VALUES
 ('CHEST_ACTIVITY_CALENDAR', 'Baú do Calendário de Atividades', 'Recompensa diária do calendário de atividades.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_activity_calendar'),
 ('CHEST_ACTIVITY_CALENDAR_MONTHLY', 'Baú de Conclusão Mensal', 'Recompensa exclusiva por completar todos os dias do calendário.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'EPIC', 'chest_activity_calendar_monthly')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, stackable = EXCLUDED.stackable, tradable = EXCLUDED.tradable, sellable = EXCLUDED.sellable, usable = EXCLUDED.usable, max_stack = EXCLUDED.max_stack, rarity = EXCLUDED.rarity, icon = EXCLUDED.icon;

INSERT INTO chest_definitions (code, name, description, icon, loot_table_id, item_definition_id, tradable, active, created_by, updated_by)
SELECT seed.code, seed.name, seed.description, seed.icon, lt.id, item.id, TRUE, TRUE, 'SYSTEM', 'SYSTEM'
FROM (VALUES
 ('CHEST_ACTIVITY_CALENDAR', 'Baú do Calendário de Atividades', 'Recompensa diária do calendário de atividades.', 'chest_activity_calendar', 'LOOT_TABLE_ACTIVITY_CALENDAR'),
 ('CHEST_ACTIVITY_CALENDAR_MONTHLY', 'Baú de Conclusão Mensal', 'Recompensa exclusiva por completar todos os dias do calendário.', 'chest_activity_calendar_monthly', 'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY')
) AS seed(code, name, description, icon, table_code)
JOIN loot_tables lt ON lt.code = seed.table_code
JOIN item_definitions item ON item.code = seed.code
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, icon = EXCLUDED.icon, loot_table_id = EXCLUDED.loot_table_id, item_definition_id = EXCLUDED.item_definition_id, active = TRUE, updated_at = CURRENT_TIMESTAMP, updated_by = 'SYSTEM';
