BEGIN;

-- Novos itens de Digitama elementais. DIGITAMA_NATURE permanece como o código
-- legado do Digitama de Planta (elemento WOOD).
INSERT INTO item_definitions (code, name, description, category) VALUES
    ('DIGITAMA_EARTH',   'Digitama de Terra',   'Digitama do elemento terra.',   'DIGITAMA'),
    ('DIGITAMA_WIND',    'Digitama de Vento',   'Digitama do elemento vento.',   'DIGITAMA'),
    ('DIGITAMA_LIGHT',   'Digitama de Luz',     'Digitama do elemento luz.',     'DIGITAMA'),
    ('DIGITAMA_DARK',    'Digitama de Trevas',  'Digitama do elemento trevas.',  'DIGITAMA'),
    ('DIGITAMA_THUNDER', 'Digitama de Trovão',  'Digitama do elemento trovão.',  'DIGITAMA'),
    ('DIGITAMA_NEUTRAL', 'Digitama Neutro',     'Digitama do elemento neutro.',  'DIGITAMA'),
    ('DIGITAMA_ICE',     'Digitama de Gelo',    'Digitama do elemento gelo.',    'DIGITAMA'),
    ('DIGITAMA_STEEL',   'Digitama de Metal',   'Digitama do elemento metal.',   'DIGITAMA')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category;

UPDATE item_definitions
SET buy_price = NULL,
    sell_price = NULL,
    tradable = false,
    sellable = false,
    usable = false,
    max_stack = 99,
    rarity = 'COMMON',
    icon = LOWER(code)
WHERE code IN (
    'DIGITAMA_STARTER',
    'DIGITAMA_FIRE',
    'DIGITAMA_WATER',
    'DIGITAMA_NATURE',
    'DIGITAMA_EARTH',
    'DIGITAMA_WIND',
    'DIGITAMA_LIGHT',
    'DIGITAMA_DARK',
    'DIGITAMA_THUNDER',
    'DIGITAMA_NEUTRAL',
    'DIGITAMA_ICE',
    'DIGITAMA_STEEL'
);

-- Um pool por elemento do jogo. O pool STARTER legado é preservado para
-- compatibilidade, mas o fluxo inicial passa a selecionar um pool elemental.
INSERT INTO digitama_pools (code, name, description, content_id, active)
SELECT data.code, data.name, data.description, ac.id, true
FROM (
    VALUES
        ('DIGITAMA_FIRE',    'Digitama de Fogo',    'Pool de Digimons BABY do elemento fogo.'),
        ('DIGITAMA_WATER',   'Digitama de Água',    'Pool de Digimons BABY do elemento água.'),
        ('DIGITAMA_NATURE',  'Digitama de Planta',  'Pool de Digimons BABY do elemento planta.'),
        ('DIGITAMA_EARTH',   'Digitama de Terra',   'Pool de Digimons BABY do elemento terra.'),
        ('DIGITAMA_WIND',    'Digitama de Vento',   'Pool de Digimons BABY do elemento vento.'),
        ('DIGITAMA_LIGHT',   'Digitama de Luz',     'Pool de Digimons BABY do elemento luz.'),
        ('DIGITAMA_DARK',    'Digitama de Trevas',  'Pool de Digimons BABY do elemento trevas.'),
        ('DIGITAMA_THUNDER', 'Digitama de Trovão',  'Pool de Digimons BABY do elemento trovão.'),
        ('DIGITAMA_NEUTRAL', 'Digitama Neutro',     'Pool de Digimons BABY do elemento neutro.'),
        ('DIGITAMA_ICE',     'Digitama de Gelo',    'Pool de Digimons BABY do elemento gelo.'),
        ('DIGITAMA_STEEL',   'Digitama de Metal',   'Pool de Digimons BABY do elemento metal.')
) AS data(code, name, description)
JOIN available_contents ac ON ac.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content_id = EXCLUDED.content_id,
    active = EXCLUDED.active;

-- Remove as associações antigas e recria cada pool somente com BABYs do seu
-- elemento canônico. As flags de habilitação são aplicadas em runtime pelo
-- backend e podem retirar qualquer entrada sem nova migration.
DELETE FROM digitama_pool_entries pool_entry
USING digitama_pools pool
WHERE pool_entry.digitama_pool_id = pool.id
  AND pool.code IN (
      'DIGITAMA_FIRE',
      'DIGITAMA_WATER',
      'DIGITAMA_NATURE',
      'DIGITAMA_EARTH',
      'DIGITAMA_WIND',
      'DIGITAMA_LIGHT',
      'DIGITAMA_DARK',
      'DIGITAMA_THUNDER',
      'DIGITAMA_NEUTRAL',
      'DIGITAMA_ICE',
      'DIGITAMA_STEEL'
  );

INSERT INTO digitama_pool_entries (digitama_pool_id, digimon_info_id, weight, active)
SELECT pool.id, info.id, 50, true
FROM (
    VALUES
        ('DIGITAMA_FIRE',    'FIRE'),
        ('DIGITAMA_WATER',   'WATER'),
        ('DIGITAMA_NATURE',  'WOOD'),
        ('DIGITAMA_EARTH',   'EARTH'),
        ('DIGITAMA_WIND',    'WIND'),
        ('DIGITAMA_LIGHT',   'LIGHT'),
        ('DIGITAMA_DARK',    'DARK'),
        ('DIGITAMA_THUNDER', 'THUNDER'),
        ('DIGITAMA_NEUTRAL', 'NEUTRAL'),
        ('DIGITAMA_ICE',     'ICE'),
        ('DIGITAMA_STEEL',   'STEEL')
) AS mapping(pool_code, element)
JOIN digitama_pools pool ON pool.code = mapping.pool_code
JOIN digimon_infos info
  ON info.stage = 'BABY'
 AND info.element = mapping.element;

COMMIT;
