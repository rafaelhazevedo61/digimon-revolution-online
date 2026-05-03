BEGIN;

INSERT INTO item_definitions (code, name, description, category) VALUES

    -- Consumíveis
    ('POTION_SMALL',      'Poção Pequena',        'Restaura uma pequena quantidade de HP.',  'CONSUMABLE'),
    ('TRAINING_STONE',    'Pedra de Treino',       'Item básico para melhorar o crescimento.','CONSUMABLE'),
    ('DATA_CORE',         'Núcleo de Dados',       'Material usado em upgrades digitais.',    'MATERIAL'),

    -- Digitamas
    ('DIGITAMA_STARTER',  'Digitama Inicial',      'Digitama de início de jogo.',             'DIGITAMA'),
    ('DIGITAMA_FIRE',     'Digitama de Fogo',      'Digitama do elemento fogo.',              'DIGITAMA'),
    ('DIGITAMA_WATER',    'Digitama de Água',      'Digitama do elemento água.',              'DIGITAMA'),
    ('DIGITAMA_NATURE',   'Digitama da Natureza',  'Digitama do elemento natureza.',          'DIGITAMA'),

    -- Incubadoras
    ('INCUBATOR_COMMON',  'Incubadora Comum',      'Incubadora comum para Digitamas.',        'INCUBATOR'),
    ('INCUBATOR_RARE',    'Incubadora Rara',       'Incubadora rara para Digitamas.',         'INCUBATOR'),
    ('INCUBATOR_EPIC',    'Incubadora Épica',      'Incubadora épica para Digitamas.',        'INCUBATOR'),

    -- Fragmentos legado (genéricos)
    ('FRAGMENT_ROOKIE',   'Fragmento Rookie',      'Fragmento genérico para evolução Rookie.',   'FRAGMENT'),
    ('FRAGMENT_CHAMPION', 'Fragmento Champion',    'Fragmento genérico para evolução Champion.', 'FRAGMENT'),
    ('FRAGMENT_ULTIMATE', 'Fragmento Ultimate',    'Fragmento genérico para evolução Ultimate.', 'FRAGMENT'),
    ('FRAGMENT_MEGA',     'Fragmento Mega',        'Fragmento genérico para evolução Mega.',     'FRAGMENT'),

    -- Fragmentos específicos — BABY_II
    ('FRAGMENT_KOROMON',    'Fragmento do Koromon',    'Fragmento para evoluir para Koromon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_TSUNOMON',   'Fragmento do Tsunomon',   'Fragmento para evoluir para Tsunomon.',   'EVOLUTION_MATERIAL'),
    ('FRAGMENT_TOKOMON',    'Fragmento do Tokomon',    'Fragmento para evoluir para Tokomon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_YOKOMON',    'Fragmento do Yokomon',    'Fragmento para evoluir para Yokomon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_MOTIMON',    'Fragmento do Motimon',    'Fragmento para evoluir para Motimon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_BUKAMON',    'Fragmento do Bukamon',    'Fragmento para evoluir para Bukamon.',    'EVOLUTION_MATERIAL'),

    -- Fragmentos específicos — ROOKIE
    ('FRAGMENT_AGUMON',     'Fragmento do Agumon',     'Fragmento para evoluir para Agumon.',     'EVOLUTION_MATERIAL'),
    ('FRAGMENT_GABUMON',    'Fragmento do Gabumon',    'Fragmento para evoluir para Gabumon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_PATAMON',    'Fragmento do Patamon',    'Fragmento para evoluir para Patamon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_BIYOMON',    'Fragmento do Biyomon',    'Fragmento para evoluir para Biyomon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_TENTOMON',   'Fragmento do Tentomon',   'Fragmento para evoluir para Tentomon.',   'EVOLUTION_MATERIAL'),
    ('FRAGMENT_GOMAMON',    'Fragmento do Gomamon',    'Fragmento para evoluir para Gomamon.',    'EVOLUTION_MATERIAL'),

    -- Fragmentos específicos — CHAMPION
    ('FRAGMENT_GREYMON',      'Fragmento do Greymon',      'Fragmento para evoluir para Greymon.',      'EVOLUTION_MATERIAL'),
    ('FRAGMENT_GARURUMON',    'Fragmento do Garurumon',    'Fragmento para evoluir para Garurumon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_ANGEMON',      'Fragmento do Angemon',      'Fragmento para evoluir para Angemon.',      'EVOLUTION_MATERIAL'),
    ('FRAGMENT_BIRDRAMON',    'Fragmento do Birdramon',    'Fragmento para evoluir para Birdramon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_KABUTERIMON',  'Fragmento do Kabuterimon',  'Fragmento para evoluir para Kabuterimon.',  'EVOLUTION_MATERIAL'),
    ('FRAGMENT_IKKAKUMON',    'Fragmento do Ikkakumon',    'Fragmento para evoluir para Ikkakumon.',    'EVOLUTION_MATERIAL'),

    -- Fragmentos específicos — ULTIMATE
    ('FRAGMENT_METALGREYMON',    'Fragmento do MetalGreymon',    'Fragmento para evoluir para MetalGreymon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_WEREGARURUMON',   'Fragmento do WereGarurumon',   'Fragmento para evoluir para WereGarurumon.',   'EVOLUTION_MATERIAL'),
    ('FRAGMENT_MAGNAANGEMON',    'Fragmento do MagnaAngemon',    'Fragmento para evoluir para MagnaAngemon.',    'EVOLUTION_MATERIAL'),
    ('FRAGMENT_GARUDAMON',       'Fragmento do Garudamon',       'Fragmento para evoluir para Garudamon.',       'EVOLUTION_MATERIAL'),
    ('FRAGMENT_MEGAKABUTERIMON', 'Fragmento do MegaKabuterimon', 'Fragmento para evoluir para MegaKabuterimon.', 'EVOLUTION_MATERIAL'),
    ('FRAGMENT_ZUDOMON',         'Fragmento do Zudomon',         'Fragmento para evoluir para Zudomon.',         'EVOLUTION_MATERIAL'),

    -- Fragmentos específicos — MEGA
    ('FRAGMENT_WARGREYMON',          'Fragmento do WarGreymon',          'Fragmento para evoluir para WarGreymon.',          'EVOLUTION_MATERIAL'),
    ('FRAGMENT_METALGARURUMON',      'Fragmento do MetalGarurumon',      'Fragmento para evoluir para MetalGarurumon.',      'EVOLUTION_MATERIAL'),
    ('FRAGMENT_SERAPHIMON',          'Fragmento do Seraphimon',          'Fragmento para evoluir para Seraphimon.',          'EVOLUTION_MATERIAL'),
    ('FRAGMENT_PHOENIXMON',          'Fragmento do Phoenixmon',          'Fragmento para evoluir para Phoenixmon.',          'EVOLUTION_MATERIAL'),
    ('FRAGMENT_HERCULESKABUTERIMON', 'Fragmento do HerculesKabuterimon', 'Fragmento para evoluir para HerculesKabuterimon.', 'EVOLUTION_MATERIAL'),
    ('FRAGMENT_VIKEMON',             'Fragmento do Vikemon',             'Fragmento para evoluir para Vikemon.',             'EVOLUTION_MATERIAL')

ON CONFLICT (code) DO NOTHING;

COMMIT;
