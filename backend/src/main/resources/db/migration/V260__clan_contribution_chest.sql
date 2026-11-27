-- Um único prêmio por abertura. Chances finais: 35/30/15/10/6/3/1%.
INSERT INTO loot_tables (code, name, description, active, min_items, max_items)
VALUES ('LOOT_TABLE_CLAN_CONTRIBUTION', 'Baú de Contribuição',
        'Recompensa pessoal por resgatar uma missão de clã.', TRUE, 1, 1);

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT lt.id, data.rarity, data.weight
FROM loot_tables lt CROSS JOIN (VALUES
    ('COMMON', 35), ('RARE', 33), ('EPIC', 31), ('LEGENDARY', 1)
) AS data(rarity, weight)
WHERE lt.code = 'LOOT_TABLE_CLAN_CONTRIBUTION';

INSERT INTO loot_table_entries
    (loot_table_id, rarity, item_type, weight, min_quantity, max_quantity, active)
SELECT lt.id, data.rarity, data.item_type, data.weight, data.min_quantity, data.max_quantity, TRUE
FROM loot_tables lt CROSS JOIN (VALUES
    ('COMMON', 'ENERGY_CELL', 35, 2, 4),
    ('RARE', 'XP_DISC_3', 30, 1, 2),
    ('EPIC', 'RARITY_PRESERVATION', 15, 1, 1),
    ('EPIC', 'RARITY_REROLL', 10, 1, 1),
    ('EPIC', 'ASCENSION_CORE', 6, 1, 1),
    ('RARE', 'VIP_ENERGY_CHARGE', 3, 1, 1),
    ('LEGENDARY', 'VIP_DAY_PASS', 1, 1, 1)
) AS data(rarity, item_type, weight, min_quantity, max_quantity)
WHERE lt.code = 'LOOT_TABLE_CLAN_CONTRIBUTION';

INSERT INTO item_definitions
    (code, name, description, category, stackable, tradable, sellable, usable, max_stack, rarity, icon)
VALUES ('CHEST_CLAN_CONTRIBUTION', 'Baú de Contribuição',
        'Recompensa pessoal de missões de clã. Contém um prêmio aleatório de energia, experiência ou recursos especiais.',
        'CHEST', TRUE, FALSE, FALSE, TRUE, 999, 'RARE', 'chest');

INSERT INTO chest_definitions
    (code, name, description, icon, loot_table_id, item_definition_id, tradable, active)
SELECT item.code, item.name, item.description, item.icon, lt.id, item.id, FALSE, TRUE
FROM item_definitions item CROSS JOIN loot_tables lt
WHERE item.code = 'CHEST_CLAN_CONTRIBUTION' AND lt.code = 'LOOT_TABLE_CLAN_CONTRIBUTION';
