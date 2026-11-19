-- Loja: substituir fragmentos genéricos por baús de fragmentos por estágio.
-- Cada baú entrega um fragmento específico aleatório do estágio correspondente.

ALTER TABLE shop_products
    ADD COLUMN item_definition_code VARCHAR(80);

-- Normaliza os nomes dos produtos legados usando o catálogo oficial de itens.
UPDATE shop_products product
SET name = definition.name,
    description = definition.description,
    item_definition_code = COALESCE(product.item_definition_code, definition.code)
FROM item_definitions definition
WHERE product.product_type = 'ITEM'
  AND product.item_type = definition.code;

-- Garante que a Poção Pequena exista no catálogo principal e esteja em português.
INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, equipment_template_name, price, sell_price, active
)
VALUES (
    'POTION_SMALL', 'Poção Pequena', 'Restaura uma pequena quantidade de HP.',
    'ITEM', 'POTION', 'POTION_SMALL',
    'POTION_SMALL', NULL, 50, 15, TRUE
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    product_type = EXCLUDED.product_type,
    category = EXCLUDED.category,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    active = TRUE;

-- Os fragmentos genéricos pertencem ao modelo legado e deixam de ser vendidos.
UPDATE shop_products
SET active = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE category = 'FRAGMENT';

-- Loot tables reutilizáveis dos baús vendidos na loja.
INSERT INTO loot_tables (
    code, name, description, active, min_items, max_items,
    created_by, updated_by
)
VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',
     'Loot Table Baú de Fragmentos - Rookie',
     'Pool de fragmentos específicos para evoluções Rookie.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM'),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION',
     'Loot Table Baú de Fragmentos - Champion',
     'Pool de fragmentos específicos para evoluções Champion.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM'),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE',
     'Loot Table Baú de Fragmentos - Ultimate',
     'Pool de fragmentos específicos para evoluções Ultimate.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM'),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',
     'Loot Table Baú de Fragmentos - Mega',
     'Pool de fragmentos específicos para evoluções Mega.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = TRUE,
    min_items = EXCLUDED.min_items,
    max_items = EXCLUDED.max_items,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

-- Cada tabela possui quatro pesos oficiais. Apenas a raridade do estágio é elegível.
INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT table_row.id, weights.rarity, weights.weight
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON',    100),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'RARE',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'EPIC',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'LEGENDARY',   0),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'COMMON',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE',        100),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'EPIC',          0),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'LEGENDARY',     0),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'COMMON',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'RARE',          0),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC',        100),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'LEGENDARY',     0),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'COMMON',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'RARE',          0),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'EPIC',          0),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY',   100)
) AS weights(table_code, rarity, weight)
    ON weights.table_code = table_row.code
ON CONFLICT (loot_table_id, rarity) DO UPDATE SET
    weight = EXCLUDED.weight;

-- Uma unidade do tipo de fragmento é sorteada entre seis materiais específicos.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, entries.rarity, 'EVOLUTION_MATERIAL', entries.material_code,
       1, entries.min_quantity, entries.max_quantity, TRUE
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_AGUMON',             1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_GABUMON',            1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_PATAMON',            1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_BIYOMON',            1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_TENTOMON',           1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON', 'FRAGMENT_GOMAMON',            1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_GREYMON',             1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_GARURUMON',           1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_ANGEMON',             1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_BIRDRAMON',           1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_KABUTERIMON',         1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'FRAGMENT_IKKAKUMON',           1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_METALGREYMON',        1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_WEREGARURUMON',       1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_MAGNAANGEMON',       1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_GARUDAMON',           1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_MEGAKABUTERIMON',     1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'FRAGMENT_ZUDOMON',              1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_WARGREYMON',          1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_METALGARURUMON',      1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_SERAPHIMON',          1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_PHOENIXMON',          1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_HERCULESKABUTERIMON', 1, 3),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_VIKEMON',             1, 3)
) AS entries(table_code, rarity, material_code, min_quantity, max_quantity)
    ON entries.table_code = table_row.code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = table_row.id
      AND existing.rarity = entries.rarity
      AND existing.item_type = 'EVOLUTION_MATERIAL'
      AND existing.material_code = entries.material_code
);

-- Definições de inventário dos quatro baús.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    ('CHEST_FRAGMENT_ROOKIE',
     'Baú de Fragmentos - Rookie',
     'Contém um fragmento específico aleatório para evolução Rookie.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_fragment_rookie'),
    ('CHEST_FRAGMENT_CHAMPION',
     'Baú de Fragmentos - Champion',
     'Contém um fragmento específico aleatório para evolução Champion.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'RARE', 'chest_fragment_champion'),
    ('CHEST_FRAGMENT_ULTIMATE',
     'Baú de Fragmentos - Ultimate',
     'Contém um fragmento específico aleatório para evolução Ultimate.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'EPIC', 'chest_fragment_ultimate'),
    ('CHEST_FRAGMENT_MEGA',
     'Baú de Fragmentos - Mega',
     'Contém um fragmento específico aleatório para evolução Mega.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'LEGENDARY', 'chest_fragment_mega')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

-- Define o vínculo entre cada item de baú e sua loot table.
INSERT INTO chest_definitions (
    code, name, description, icon, loot_table_id, item_definition_id,
    tradable, active, created_by, updated_by
)
SELECT chest.code, chest.name, chest.description, chest.icon, table_row.id,
       item.id, TRUE, TRUE, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    ('CHEST_FRAGMENT_ROOKIE',   'Baú de Fragmentos - Rookie',   'Contém um fragmento específico aleatório para evolução Rookie.',   'chest_fragment_rookie',   'LOOT_TABLE_SHOP_FRAGMENT_ROOKIE'),
    ('CHEST_FRAGMENT_CHAMPION', 'Baú de Fragmentos - Champion', 'Contém um fragmento específico aleatório para evolução Champion.', 'chest_fragment_champion', 'LOOT_TABLE_SHOP_FRAGMENT_CHAMPION'),
    ('CHEST_FRAGMENT_ULTIMATE', 'Baú de Fragmentos - Ultimate', 'Contém um fragmento específico aleatório para evolução Ultimate.', 'chest_fragment_ultimate', 'LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE'),
    ('CHEST_FRAGMENT_MEGA',     'Baú de Fragmentos - Mega',     'Contém um fragmento específico aleatório para evolução Mega.',     'chest_fragment_mega',     'LOOT_TABLE_SHOP_FRAGMENT_MEGA')
) AS chest(code, name, description, icon, table_code)
JOIN loot_tables table_row ON table_row.code = chest.table_code
JOIN item_definitions item ON item.code = chest.code
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM';

-- Produtos principais da loja. O código coincide com a definição do baú para
-- manter a integração com o inventário e com POST /inventory/chests/open.
INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, equipment_template_name, price, sell_price, active
)
VALUES
    ('CHEST_FRAGMENT_ROOKIE',
     'Baú de Fragmentos - Rookie',
     'Baú com um fragmento específico aleatório para evolução Rookie.',
     'ITEM', 'CHEST', 'LOOT_CHEST',
     'CHEST_FRAGMENT_ROOKIE', NULL, 150, 0, TRUE),
    ('CHEST_FRAGMENT_CHAMPION',
     'Baú de Fragmentos - Champion',
     'Baú com um fragmento específico aleatório para evolução Champion.',
     'ITEM', 'CHEST', 'LOOT_CHEST',
     'CHEST_FRAGMENT_CHAMPION', NULL, 300, 0, TRUE),
    ('CHEST_FRAGMENT_ULTIMATE',
     'Baú de Fragmentos - Ultimate',
     'Baú com um fragmento específico aleatório para evolução Ultimate.',
     'ITEM', 'CHEST', 'LOOT_CHEST',
     'CHEST_FRAGMENT_ULTIMATE', NULL, 600, 0, TRUE),
    ('CHEST_FRAGMENT_MEGA',
     'Baú de Fragmentos - Mega',
     'Baú com um fragmento específico aleatório para evolução Mega.',
     'ITEM', 'CHEST', 'LOOT_CHEST',
     'CHEST_FRAGMENT_MEGA', NULL, 1000, 0, TRUE)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    product_type = EXCLUDED.product_type,
    category = EXCLUDED.category,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    price = EXCLUDED.price,
    sell_price = EXCLUDED.sell_price,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';
