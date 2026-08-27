BEGIN;

-- Espécies novas das dez linhas evolutivas da Expansão Evolutiva I.
-- Os dez BABYs de origem já existem em V25 e não são duplicados aqui.
INSERT INTO digimon_infos (
    name,
    stage,
    attribute,
    element,
    specie,
    base_hp,
    base_atk,
    base_def,
    image_url
)
VALUES
    -- Jyarimon → Gigimon → Guilmon → Growmon → Megalo Growmon → Dukemon
    ('Gigimon',                    'BABY_II', 'VIRUS',   'FIRE',  'NONE',   22,  7,  8, 'https://digi-api.com/images/digimon/w/Gigimon.png'),
    ('Guilmon',                    'ROOKIE',  'VIRUS',   'FIRE',  'DRAGON', 38, 16, 11, 'https://digi-api.com/images/digimon/w/Guilmon.png'),
    ('Growmon',                    'CHAMPION','VIRUS',   'FIRE',  'DRAGON', 70, 29, 22, 'https://digi-api.com/images/digimon/w/Growmon.png'),
    ('Megalo Growmon',             'ULTIMATE','VIRUS',   'FIRE',  'DRAGON',108, 49, 38, 'https://digi-api.com/images/digimon/w/Megalo_Growmon.png'),
    ('Dukemon',                    'MEGA',    'VACCINE', 'LIGHT', 'HOLY',  152, 74, 58, 'https://digi-api.com/images/digimon/w/Dukemon.png'),

    -- Dodomon → Dorimon → DORUmon → DORUgamon → DORUguremon → DORUgoramon
    ('Dorimon',                    'BABY_II', 'DATA',    'EARTH', 'NONE',   23,  7,  9, 'https://digi-api.com/images/digimon/w/Dorimon.png'),
    ('DORUmon',                    'ROOKIE',  'DATA',    'EARTH', 'BEAST',  40, 17, 12, 'https://digi-api.com/images/digimon/w/DORUmon.png'),
    ('DORUgamon',                  'CHAMPION','DATA',    'EARTH', 'DRAGON', 74, 30, 24, 'https://digi-api.com/images/digimon/w/DORUgamon.png'),
    ('DORUguremon',                'ULTIMATE','VACCINE', 'EARTH', 'DRAGON',120, 54, 44, 'https://digi-api.com/images/digimon/w/DORUguremon.png'),
    ('DORUgoramon',                'MEGA',    'VACCINE', 'EARTH', 'DRAGON',175, 78, 66, 'https://digi-api.com/images/digimon/w/DORUgoramon.png'),

    -- Kuramon → Tsumemon → Keramon → Chrysalimon → Infermon → Diablomon
    ('Tsumemon',                   'BABY_II', 'VIRUS',   'DARK',  'NONE',   21,  8,  6, 'https://digi-api.com/images/digimon/w/Tsumemon.png'),
    ('Keramon',                    'ROOKIE',  'VIRUS',   'DARK',  'DARK',   37, 17, 10, 'https://digi-api.com/images/digimon/w/Keramon.png'),
    ('Chrysalimon',                'CHAMPION','VIRUS',   'DARK',  'DARK',   65, 31, 19, 'https://digi-api.com/images/digimon/w/Chrysalimon.png'),
    ('Infermon',                   'ULTIMATE','VIRUS',   'DARK',  'DARK',  112, 52, 36, 'https://digi-api.com/images/digimon/w/Infermon.png'),
    ('Diablomon',                  'MEGA',    'VIRUS',   'DARK',  'DARK',  160, 78, 50, 'https://digi-api.com/images/digimon/w/Diablomon.png'),

    -- Leafmon → Minomon → Wormmon → Stingmon → Jewelbeemon → Gran Kuwagamon
    ('Minomon',                    'BABY_II', 'DATA',    'WOOD',  'NONE',   24,  6,  9, 'https://digi-api.com/images/digimon/w/Minomon.png'),
    ('Wormmon',                    'ROOKIE',  'DATA',    'WOOD',  'INSECT', 36, 14, 13, 'https://digi-api.com/images/digimon/w/Wormmon.png'),
    ('Stingmon',                   'CHAMPION','VIRUS',   'WOOD',  'INSECT', 67, 34, 18, 'https://digi-api.com/images/digimon/w/Stingmon.png'),
    ('Jewelbeemon',                'ULTIMATE','VACCINE', 'WOOD',  'INSECT',108, 52, 36, 'https://digi-api.com/images/digimon/w/Jewelbeemon.png'),
    ('Gran Kuwagamon',              'MEGA',    'VIRUS',   'WOOD',  'INSECT',164, 76, 52, 'https://digi-api.com/images/digimon/w/Gran_Kuwagamon.png'),

    -- Zerimon → Gummymon → Terriermon → Gargomon → Rapidmon Perfect → Saint Galgomon
    ('Gummymon',                   'BABY_II', 'DATA',    'NEUTRAL','NONE',  22,  7,  8, 'https://digi-api.com/images/digimon/w/Gummymon.png'),
    ('Terriermon',                 'ROOKIE',  'VACCINE', 'NEUTRAL','BEAST', 37, 16, 12, 'https://digi-api.com/images/digimon/w/Terriermon.png'),
    ('Gargomon',                   'CHAMPION','VACCINE', 'ELECTRIC','BEAST',62, 28, 24, 'https://digi-api.com/images/digimon/w/Gargomon.png'),
    ('Rapidmon Perfect',           'ULTIMATE','VACCINE', 'ELECTRIC','MACHINE',112,51, 41, 'https://digi-api.com/images/digimon/w/Rapidmon_Perfect.png'),
    ('Saint Galgomon',              'MEGA',    'VACCINE', 'ELECTRIC','MACHINE',168,70,67, 'https://digi-api.com/images/digimon/w/Saint_Galgomon.png'),

    -- Cocomon → Chocomon → Lopmon → Turuiemon → Andiramon → Cherubimon (Virtue)
    ('Chocomon',                   'BABY_II', 'DATA',    'NEUTRAL','NONE',  22,  7,  8, 'https://digi-api.com/images/digimon/w/Chocomon.png'),
    ('Lopmon',                     'ROOKIE',  'DATA',    'NEUTRAL','BEAST', 36, 15, 13, 'https://digi-api.com/images/digimon/w/Lopmon.png'),
    ('Turuiemon',                  'CHAMPION','DATA',    'EARTH', 'BEAST',  64, 29, 24, 'https://digi-api.com/images/digimon/w/Turuiemon.png'),
    ('Andiramon',                  'ULTIMATE','VACCINE', 'EARTH', 'HOLY',  106, 48, 44, 'https://digi-api.com/images/digimon/w/Andiramon.png'),
    ('Cherubimon (Virtue)',        'MEGA',    'VACCINE', 'LIGHT', 'HOLY',  154, 70, 66, 'https://digi-api.com/images/digimon/w/Cherubimon_(Virtue).png'),

    -- Pusumon → Pusurimon → Pulsemon → Bulkmon → Boutmon → Kazuchimon
    ('Pusurimon',                 'BABY_II', 'VACCINE', 'ELECTRIC','NONE', 20,  9,  6, 'https://digi-api.com/images/digimon/w/Pusurimon.png'),
    ('Pulsemon',                   'ROOKIE',  'VACCINE', 'ELECTRIC','BEAST', 38, 19, 10, 'https://digi-api.com/images/digimon/w/Pulsemon.png'),
    ('Bulkmon',                    'CHAMPION','VACCINE', 'ELECTRIC','DRAGON',70, 35, 19, 'https://digi-api.com/images/digimon/w/Bulkmon.png'),
    ('Boutmon',                    'ULTIMATE','VACCINE', 'ELECTRIC','BEAST',116, 58, 38, 'https://digi-api.com/images/digimon/w/Boutmon.png'),
    ('Kazuchimon',                 'MEGA',    'VACCINE', 'ELECTRIC','HOLY',178, 80, 58, 'https://digi-api.com/images/digimon/w/Kazuchimon.png'),

    -- Chicomon → Chibimon → V-mon → XV-mon → Paildramon → Imperialdramon (Dragon Mode)
    ('Chibimon',                   'BABY_II', 'DATA',    'LIGHT', 'NONE',  23,  7,  8, 'https://digi-api.com/images/digimon/w/Chibimon.png'),
    ('V-mon',                      'ROOKIE',  'VACCINE', 'LIGHT', 'DRAGON',40, 18, 12, 'https://digi-api.com/images/digimon/w/V-mon.png'),
    ('XV-mon',                     'CHAMPION','VACCINE', 'LIGHT', 'DRAGON',78, 32, 25, 'https://digi-api.com/images/digimon/w/XV-mon.png'),
    ('Paildramon',                 'ULTIMATE','DATA',    'LIGHT', 'DRAGON',124, 56, 42, 'https://digi-api.com/images/digimon/w/Paildramon.png'),
    ('Imperialdramon (Dragon Mode)','MEGA',   'VACCINE', 'LIGHT', 'DRAGON',182, 82, 64, 'https://digi-api.com/images/digimon/w/Imperialdramon(Dragon_Mode).png'),

    -- Yukimi Botamon → Hiyarimon → Blucomon → Paledramon → Crys Paledramon → Hexeblaumon
    ('Hiyarimon',                  'BABY_II', 'VACCINE', 'ICE',   'NONE',  24,  6, 10, 'https://digi-api.com/images/digimon/w/Hiyarimon.png'),
    ('Blucomon',                   'ROOKIE',  'DATA',    'ICE',   'DRAGON',40, 15, 15, 'https://digi-api.com/images/digimon/w/Blucomon.png'),
    ('Paledramon',                 'CHAMPION','DATA',    'ICE',   'DRAGON',76, 27, 29, 'https://digi-api.com/images/digimon/w/Paledramon.png'),
    ('Crys Paledramon',            'ULTIMATE','DATA',    'ICE',   'DRAGON',126, 48, 48, 'https://digi-api.com/images/digimon/w/Crys_Paledramon.png'),
    ('Hexeblaumon',                'MEGA',    'DATA',    'ICE',   'DRAGON',178, 69, 76, 'https://digi-api.com/images/digimon/w/Hexeblaumon.png'),

    -- Sakumon → Sakuttomon → Ryudamon → Ginryumon → Hisyaryumon → Ouryumon
    ('Sakuttomon',                 'BABY_II', 'DATA',    'EARTH', 'NONE',  23,  8,  7, 'https://digi-api.com/images/digimon/w/Sakuttomon.png'),
    ('Ryudamon',                   'ROOKIE',  'VACCINE', 'EARTH', 'BEAST', 39, 18, 11, 'https://digi-api.com/images/digimon/w/Ryudamon.png'),
    ('Ginryumon',                  'CHAMPION','VACCINE', 'EARTH', 'DRAGON',73, 31, 23, 'https://digi-api.com/images/digimon/w/Ginryumon.png'),
    ('Hisyaryumon',                'ULTIMATE','VACCINE', 'EARTH', 'DRAGON',121,55, 43, 'https://digi-api.com/images/digimon/w/Hisyaryumon.png'),
    ('Ouryumon',                   'MEGA',    'VACCINE', 'EARTH', 'DRAGON',176,80, 63, 'https://digi-api.com/images/digimon/w/Ouryumon.png')
ON CONFLICT (name) DO NOTHING;

COMMIT;
