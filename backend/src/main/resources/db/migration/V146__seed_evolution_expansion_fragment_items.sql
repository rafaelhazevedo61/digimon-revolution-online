BEGIN;

-- Fragmentos específicos das dez novas linhas.
-- Estes registros apenas catalogam os itens usados pelos steps de evolução.
-- Drops, baús e produtos de loja não são configurados nesta migration.
INSERT INTO item_definitions (
    code,
    name,
    description,
    category,
    stackable,
    buy_price,
    sell_price,
    tradable,
    sellable,
    usable,
    max_stack,
    rarity,
    icon
)
VALUES
    -- BABY_II — comuns
    ('FRAGMENT_GIGIMON',                    'Fragmento do Gigimon',                    'Fragmento para evoluir para Gigimon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_DORIMON',                    'Fragmento do Dorimon',                    'Fragmento para evoluir para Dorimon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_TSUMEMON',                   'Fragmento do Tsumemon',                   'Fragmento para evoluir para Tsumemon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_MINOMON',                    'Fragmento do Minomon',                    'Fragmento para evoluir para Minomon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_GUMMYMON',                   'Fragmento do Gummymon',                   'Fragmento para evoluir para Gummymon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_CHOCOMON',                   'Fragmento do Chocomon',                   'Fragmento para evoluir para Chocomon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_PUSURIMON',                  'Fragmento do Pusurimon',                  'Fragmento para evoluir para Pusurimon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_CHIBIMON',                   'Fragmento do Chibimon',                   'Fragmento para evoluir para Chibimon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_HIYARIMON',                  'Fragmento do Hiyarimon',                  'Fragmento para evoluir para Hiyarimon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_SAKUTTOMON',                 'Fragmento do Sakuttomon',                 'Fragmento para evoluir para Sakuttomon.',                 'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),

    -- ROOKIE — comuns
    ('FRAGMENT_GUILMON',                    'Fragmento do Guilmon',                    'Fragmento para evoluir para Guilmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_DORUMON',                    'Fragmento do DORUmon',                    'Fragmento para evoluir para DORUmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_KERAMON',                    'Fragmento do Keramon',                    'Fragmento para evoluir para Keramon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_WORMMON',                    'Fragmento do Wormmon',                    'Fragmento para evoluir para Wormmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_TERRIERMON',                 'Fragmento do Terriermon',                 'Fragmento para evoluir para Terriermon.',                 'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_LOPMON',                     'Fragmento do Lopmon',                     'Fragmento para evoluir para Lopmon.',                     'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_PULSEMON',                   'Fragmento do Pulsemon',                   'Fragmento para evoluir para Pulsemon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_VMON',                       'Fragmento do V-mon',                       'Fragmento para evoluir para V-mon.',                       'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_BLUCOMON',                   'Fragmento do Blucomon',                   'Fragmento para evoluir para Blucomon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),
    ('FRAGMENT_RYUDAMON',                   'Fragmento do Ryudamon',                   'Fragmento para evoluir para Ryudamon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 25, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_rookie_specific'),

    -- CHAMPION — raros
    ('FRAGMENT_GROWMON',                    'Fragmento do Growmon',                    'Fragmento para evoluir para Growmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_DORUGAMON',                  'Fragmento do DORUgamon',                  'Fragmento para evoluir para DORUgamon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_CHRYSALIMON',                'Fragmento do Chrysalimon',                'Fragmento para evoluir para Chrysalimon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_STINGMON',                   'Fragmento do Stingmon',                   'Fragmento para evoluir para Stingmon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_GARGOMON',                   'Fragmento do Gargomon',                   'Fragmento para evoluir para Gargomon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_TURUIEMON',                  'Fragmento do Turuiemon',                  'Fragmento para evoluir para Turuiemon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_BULKMON',                    'Fragmento do Bulkmon',                    'Fragmento para evoluir para Bulkmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_XVMON',                     'Fragmento do XV-mon',                     'Fragmento para evoluir para XV-mon.',                     'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_PALEDRAMON',                 'Fragmento do Paledramon',                 'Fragmento para evoluir para Paledramon.',                 'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_GINRYUMON',                  'Fragmento do Ginryumon',                  'Fragmento para evoluir para Ginryumon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),

    -- ULTIMATE — épicos
    ('FRAGMENT_MEGALOGROWMON',              'Fragmento do Megalo Growmon',              'Fragmento para evoluir para Megalo Growmon.',              'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_DORUGUREMON',                'Fragmento do DORUguremon',                'Fragmento para evoluir para DORUguremon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_INFERMON',                   'Fragmento do Infermon',                   'Fragmento para evoluir para Infermon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_JEWELBEEMON',                'Fragmento do Jewelbeemon',                'Fragmento para evoluir para Jewelbeemon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_RAPIDMONPERFECT',             'Fragmento do Rapidmon Perfect',             'Fragmento para evoluir para Rapidmon Perfect.',             'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_ANDIRAMON',                  'Fragmento do Andiramon',                  'Fragmento para evoluir para Andiramon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_BOUTMON',                    'Fragmento do Boutmon',                    'Fragmento para evoluir para Boutmon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_PAILDRAMON',                 'Fragmento do Paildramon',                 'Fragmento para evoluir para Paildramon.',                 'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_CRYSPALEDRAMON',             'Fragmento do Crys Paledramon',             'Fragmento para evoluir para Crys Paledramon.',             'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_HISYARYUMON',                'Fragmento do Hisyaryumon',                'Fragmento para evoluir para Hisyaryumon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),

    -- MEGA — lendários
    ('FRAGMENT_DUKEMON',                    'Fragmento do Dukemon',                    'Fragmento para evoluir para Dukemon.',                    'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_DORUGORAMON',                'Fragmento do DORUgoramon',                'Fragmento para evoluir para DORUgoramon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_DIABLOMON',                  'Fragmento do Diablomon',                  'Fragmento para evoluir para Diablomon.',                  'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_GRANKUWAGAMON',              'Fragmento do Gran Kuwagamon',              'Fragmento para evoluir para Gran Kuwagamon.',              'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_SAINTGALGOMON',              'Fragmento do Saint Galgomon',              'Fragmento para evoluir para Saint Galgomon.',              'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_CHERUBIMONVIRTUE',           'Fragmento do Cherubimon (Virtue)',           'Fragmento para evoluir para Cherubimon (Virtue).',           'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_KAZUCHIMON',                 'Fragmento do Kazuchimon',                 'Fragmento para evoluir para Kazuchimon.',                 'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_IMPERIALDRAMONDRAGONMODE',   'Fragmento do Imperialdramon (Dragon Mode)', 'Fragmento para evoluir para Imperialdramon (Dragon Mode).', 'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_HEXEBLAUMON',                'Fragmento do Hexeblaumon',                'Fragmento para evoluir para Hexeblaumon.',                'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific'),
    ('FRAGMENT_OURYUMON',                   'Fragmento do Ouryumon',                   'Fragmento para evoluir para Ouryumon.',                   'EVOLUTION_MATERIAL', TRUE, NULL, 250, FALSE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    buy_price = EXCLUDED.buy_price,
    sell_price = EXCLUDED.sell_price,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

COMMIT;
