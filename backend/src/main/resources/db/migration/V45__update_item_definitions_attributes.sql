BEGIN;

-- Consumíveis
UPDATE item_definitions SET buy_price = 50,   sell_price = 15,  tradable = true,  sellable = true,  usable = true,  max_stack = 99, rarity = 'COMMON', icon = 'potion_small'     WHERE code = 'POTION_SMALL';
UPDATE item_definitions SET buy_price = 100,  sell_price = 25,  tradable = true,  sellable = true,  usable = true,  max_stack = 99, rarity = 'COMMON', icon = 'training_stone'   WHERE code = 'TRAINING_STONE';
UPDATE item_definitions SET buy_price = 150,  sell_price = 40,  tradable = true,  sellable = true,  usable = false, max_stack = 99, rarity = 'RARE',   icon = 'data_core'        WHERE code = 'DATA_CORE';

-- Digitamas (não vendíveis, não tradáveis, usáveis para chocar)
UPDATE item_definitions SET buy_price = NULL, sell_price = NULL, tradable = false, sellable = false, usable = true,  max_stack = 10, rarity = 'COMMON',    icon = 'digitama_starter' WHERE code = 'DIGITAMA_STARTER';
UPDATE item_definitions SET buy_price = NULL, sell_price = NULL, tradable = false, sellable = false, usable = true,  max_stack = 10, rarity = 'RARE',      icon = 'digitama_fire'    WHERE code = 'DIGITAMA_FIRE';
UPDATE item_definitions SET buy_price = NULL, sell_price = NULL, tradable = false, sellable = false, usable = true,  max_stack = 10, rarity = 'RARE',      icon = 'digitama_water'   WHERE code = 'DIGITAMA_WATER';
UPDATE item_definitions SET buy_price = NULL, sell_price = NULL, tradable = false, sellable = false, usable = true,  max_stack = 10, rarity = 'RARE',      icon = 'digitama_nature'  WHERE code = 'DIGITAMA_NATURE';

-- Incubadoras (compráveis, não tradáveis)
UPDATE item_definitions SET buy_price = 500,  sell_price = 125, tradable = false, sellable = true,  usable = true,  max_stack = 10, rarity = 'COMMON',    icon = 'incubator_common' WHERE code = 'INCUBATOR_COMMON';
UPDATE item_definitions SET buy_price = NULL, sell_price = 200, tradable = false, sellable = true,  usable = true,  max_stack = 10, rarity = 'RARE',      icon = 'incubator_rare'   WHERE code = 'INCUBATOR_RARE';
UPDATE item_definitions SET buy_price = NULL, sell_price = 400, tradable = false, sellable = true,  usable = true,  max_stack = 10, rarity = 'EPIC',      icon = 'incubator_epic'   WHERE code = 'INCUBATOR_EPIC';

-- Fragmentos legado (genéricos — compráveis na shop)
UPDATE item_definitions SET buy_price = NULL, sell_price = 50,  tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'COMMON',   icon = 'fragment_rookie'   WHERE code = 'FRAGMENT_ROOKIE';
UPDATE item_definitions SET buy_price = 300,  sell_price = 75,  tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'RARE',     icon = 'fragment_champion' WHERE code = 'FRAGMENT_CHAMPION';
UPDATE item_definitions SET buy_price = 600,  sell_price = 150, tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'EPIC',     icon = 'fragment_ultimate' WHERE code = 'FRAGMENT_ULTIMATE';
UPDATE item_definitions SET buy_price = 1000, sell_price = 250, tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'LEGENDARY', icon = 'fragment_mega'    WHERE code = 'FRAGMENT_MEGA';

-- Fragmentos específicos BABY_II (dropaveis, tradáveis, baratos)
UPDATE item_definitions SET buy_price = NULL, sell_price = 10,  tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'COMMON', icon = 'fragment_baby2'   WHERE code IN ('FRAGMENT_KOROMON','FRAGMENT_TSUNOMON','FRAGMENT_TOKOMON','FRAGMENT_YOKOMON','FRAGMENT_MOTIMON','FRAGMENT_BUKAMON');

-- Fragmentos específicos ROOKIE
UPDATE item_definitions SET buy_price = NULL, sell_price = 25,  tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'COMMON', icon = 'fragment_rookie_specific' WHERE code IN ('FRAGMENT_AGUMON','FRAGMENT_GABUMON','FRAGMENT_PATAMON','FRAGMENT_BIYOMON','FRAGMENT_TENTOMON','FRAGMENT_GOMAMON');

-- Fragmentos específicos CHAMPION
UPDATE item_definitions SET buy_price = NULL, sell_price = 60,  tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'RARE', icon = 'fragment_champion_specific' WHERE code IN ('FRAGMENT_GREYMON','FRAGMENT_GARURUMON','FRAGMENT_ANGEMON','FRAGMENT_BIRDRAMON','FRAGMENT_KABUTERIMON','FRAGMENT_IKKAKUMON');

-- Fragmentos específicos ULTIMATE
UPDATE item_definitions SET buy_price = NULL, sell_price = 120, tradable = true,  sellable = true,  usable = false, max_stack = 999, rarity = 'EPIC', icon = 'fragment_ultimate_specific' WHERE code IN ('FRAGMENT_METALGREYMON','FRAGMENT_WEREGARURUMON','FRAGMENT_MAGNAANGEMON','FRAGMENT_GARUDAMON','FRAGMENT_MEGAKABUTERIMON','FRAGMENT_ZUDOMON');

-- Fragmentos específicos MEGA
UPDATE item_definitions SET buy_price = NULL, sell_price = 250, tradable = false, sellable = true,  usable = false, max_stack = 999, rarity = 'LEGENDARY', icon = 'fragment_mega_specific' WHERE code IN ('FRAGMENT_WARGREYMON','FRAGMENT_METALGARURUMON','FRAGMENT_SERAPHIMON','FRAGMENT_PHOENIXMON','FRAGMENT_HERCULESKABUTERIMON','FRAGMENT_VIKEMON');

COMMIT;
