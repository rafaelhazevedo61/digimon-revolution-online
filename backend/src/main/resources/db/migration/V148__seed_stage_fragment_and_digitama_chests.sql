-- Expande os baús de fragmentos para todos os fragmentos específicos
-- cadastrados por estágio e adiciona o baú de Digitamas aleatórias.
-- Nenhum produto de loja é criado nesta migration.

-- Todos os baús de fragmentos entregam exatamente um tipo de fragmento,
-- sorteado dentro da pool do estágio, em quantidade de 1 a 5.
UPDATE loot_tables
SET min_items = 1,
    max_items = 1,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code IN (
    'LOOT_TABLE_SHOP_FRAGMENT_BABY_II',
    'LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',
    'LOOT_TABLE_SHOP_FRAGMENT_CHAMPION',
    'LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE',
    'LOOT_TABLE_SHOP_FRAGMENT_MEGA'
);

UPDATE loot_table_entries entry
SET min_quantity = 1,
    max_quantity = 5,
    active = TRUE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code IN (
      'LOOT_TABLE_SHOP_FRAGMENT_BABY_II',
      'LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',
      'LOOT_TABLE_SHOP_FRAGMENT_CHAMPION',
      'LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE',
      'LOOT_TABLE_SHOP_FRAGMENT_MEGA'
  )
  AND entry.item_type = 'EVOLUTION_MATERIAL';

-- Novo pool de fragmentos BABY_II e pool de Digitamas aleatórias.
INSERT INTO loot_tables (
    code, name, description, active, min_items, max_items,
    created_by, updated_by
)
VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II',
     'Loot Table Baú de Fragmentos - BABY II',
     'Pool de fragmentos específicos para evoluções BABY II.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM'),
    ('LOOT_TABLE_DIGITAMA_RANDOM',
     'Loot Table Baú de Digitamas',
     'Pool de Digitamas elegíveis; cada abertura entrega exatamente uma Digitama.',
     TRUE, 1, 1, 'SYSTEM', 'SYSTEM')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    min_items = EXCLUDED.min_items,
    max_items = EXCLUDED.max_items,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

-- Os dois pools novos usam somente a raridade COMMON. Os pesos zero mantêm
-- a configuração completa exigida pelo domínio para todas as raridades oficiais.
INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT table_row.id, weights.rarity, weights.weight
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON',    100),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'RARE',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'EPIC',        0),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'LEGENDARY',   0),
    ('LOOT_TABLE_DIGITAMA_RANDOM',       'COMMON',    100),
    ('LOOT_TABLE_DIGITAMA_RANDOM',       'RARE',        0),
    ('LOOT_TABLE_DIGITAMA_RANDOM',       'EPIC',        0),
    ('LOOT_TABLE_DIGITAMA_RANDOM',       'LEGENDARY',   0)
) AS weights(table_code, rarity, weight)
    ON weights.table_code = table_row.code
ON CONFLICT (loot_table_id, rarity) DO UPDATE SET
    weight = EXCLUDED.weight;

-- Cada entrada de fragmento possui peso uniforme dentro da própria pool.
-- Os fragmentos legados genéricos não entram: as pools usam somente
-- FRAGMENT_<DIGIMON> específicos dos estágios cadastrados.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, entries.rarity, entries.item_type::VARCHAR, entries.material_code,
       entries.weight, entries.min_quantity, entries.max_quantity, entries.active
FROM loot_tables table_row
JOIN (VALUES
    -- BABY_II: seis fragmentos das linhas existentes + dez da expansão.
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_KOROMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_TSUNOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_TOKOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_YOKOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_MOTIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_BUKAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_GIGIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_DORIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_TSUMEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_MINOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_GUMMYMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_CHOCOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_PUSURIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_CHIBIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_HIYARIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_BABY_II', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_SAKUTTOMON', 1, 1, 5, TRUE),

    -- ROOKIE: seis fragmentos das linhas existentes + dez da expansão.
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_GABUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_PATAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_BIYOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_TENTOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_GOMAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_GUILMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_DORUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_KERAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_WORMMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_TERRIERMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_LOPMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_PULSEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_VMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_BLUCOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE', 'COMMON', 'EVOLUTION_MATERIAL', 'FRAGMENT_RYUDAMON', 1, 1, 5, TRUE),

    -- CHAMPION: seis fragmentos das linhas existentes + dez da expansão.
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_GREYMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_GARURUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_ANGEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_BIRDRAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_KABUTERIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_IKKAKUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_GROWMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_DORUGAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_CHRYSALIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_STINGMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_GARGOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_TURUIEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_BULKMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_XVMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_PALEDRAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE', 'EVOLUTION_MATERIAL', 'FRAGMENT_GINRYUMON', 1, 1, 5, TRUE),

    -- ULTIMATE: seis fragmentos das linhas existentes + dez da expansão.
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_METALGREYMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_WEREGARURUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_MAGNAANGEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_GARUDAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_MEGAKABUTERIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_ZUDOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_MEGALOGROWMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_DORUGUREMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_INFERMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_JEWELBEEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_RAPIDMONPERFECT', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_ANDIRAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_BOUTMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_PAILDRAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_CRYSPALEDRAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC', 'EVOLUTION_MATERIAL', 'FRAGMENT_HISYARYUMON', 1, 1, 5, TRUE),

    -- MEGA: seis fragmentos das linhas existentes + dez da expansão.
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_WARGREYMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_METALGARURUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_SERAPHIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_PHOENIXMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_HERCULESKABUTERIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_VIKEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_DUKEMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_DORUGORAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_DIABLOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_GRANKUWAGAMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_SAINTGALGOMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_CHERUBIMONVIRTUE', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_KAZUCHIMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_IMPERIALDRAMONDRAGONMODE', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_HEXEBLAUMON', 1, 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_OURYUMON', 1, 1, 5, TRUE)
) AS entries(table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity, active)
    ON entries.table_code = table_row.code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = table_row.id
      AND existing.rarity = entries.rarity
      AND existing.item_type = entries.item_type::VARCHAR
      AND existing.material_code = entries.material_code
);

-- Digitamas existentes: cada entrada representa uma Digitama e sempre tem
-- quantidade fixa 1. O item_type identifica diretamente o item do inventário.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'COMMON', entries.item_type, NULL, 1, 1, 1, TRUE
FROM loot_tables table_row
JOIN (VALUES
    ('DIGITAMA_STARTER'),
    ('DIGITAMA_FIRE'),
    ('DIGITAMA_WATER'),
    ('DIGITAMA_NATURE'),
    ('DIGITAMA_EARTH'),
    ('DIGITAMA_WIND'),
    ('DIGITAMA_LIGHT'),
    ('DIGITAMA_DARK'),
    ('DIGITAMA_THUNDER'),
    ('DIGITAMA_NEUTRAL'),
    ('DIGITAMA_ICE'),
    ('DIGITAMA_STEEL')
) AS entries(item_type)
    ON table_row.code = 'LOOT_TABLE_DIGITAMA_RANDOM'
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = table_row.id
      AND existing.item_type = entries.item_type
      AND existing.material_code IS NULL
);

-- Catálogo dos dois novos baús. Não há produto de loja associado.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    ('CHEST_FRAGMENT_BABY_II',
     'Baú de Fragmentos - BABY II',
     'Contém um fragmento específico aleatório para evolução BABY II.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_fragment_baby2'),
    ('CHEST_DIGITAMA_RANDOM',
     'Baú de Digitamas',
     'Contém exatamente uma Digitama aleatória entre as Digitamas cadastradas.',
     'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_digitama_random')
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

-- Define os vínculos entre os itens de baú e suas loot tables.
INSERT INTO chest_definitions (
    code, name, description, icon, loot_table_id, item_definition_id,
    tradable, active, created_by, updated_by
)
SELECT chest.code, chest.name, chest.description, chest.icon, table_row.id,
       item.id, TRUE, TRUE, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    ('CHEST_FRAGMENT_BABY_II', 'Baú de Fragmentos - BABY II', 'Contém um fragmento específico aleatório para evolução BABY II.', 'chest_fragment_baby2', 'LOOT_TABLE_SHOP_FRAGMENT_BABY_II'),
    ('CHEST_DIGITAMA_RANDOM',  'Baú de Digitamas',               'Contém exatamente uma Digitama aleatória entre as Digitamas cadastradas.', 'chest_digitama_random', 'LOOT_TABLE_DIGITAMA_RANDOM')
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
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';
