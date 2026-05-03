BEGIN;

INSERT INTO digimon_infos (
    name,
    stage,
    attribute,
    element,
    specie,
    base_hp,
    base_atk,
    base_def
)
SELECT
    data.name,
    data.stage,
    data.attribute,
    data.element,
    data.specie,
    data.base_hp,
    data.base_atk,
    data.base_def
FROM (
    VALUES
        -- Agumon line
        ('MetalGreymon', 'ULTIMATE', 'VACCINE', 'FIRE',  'NONE', 105, 44, 34),
        ('WarGreymon',   'MEGA',     'VACCINE', 'FIRE',  'NONE', 150, 68, 52),

        -- Gabumon line
        ('WereGarurumon',  'ULTIMATE', 'VACCINE', 'WATER', 'NONE', 98, 46, 36),
        ('MetalGarurumon', 'MEGA',     'VACCINE', 'WATER', 'NONE', 140, 66, 55),

        -- Patamon line
        ('MagnaAngemon', 'ULTIMATE', 'VACCINE', 'LIGHT', 'NONE', 94, 48, 38),
        ('Seraphimon',   'MEGA',     'VACCINE', 'LIGHT', 'NONE', 135, 70, 58),

        -- Biyomon line
        ('Garudamon',  'ULTIMATE', 'VACCINE', 'WIND', 'NONE', 92, 50, 34),
        ('Phoenixmon', 'MEGA',     'VACCINE', 'FIRE', 'NONE', 132, 72, 50),

        -- Tentomon line
        ('MegaKabuterimon',     'ULTIMATE', 'VACCINE', 'WOOD', 'NONE', 115, 40, 48),
        ('HerculesKabuterimon', 'MEGA',     'VACCINE', 'WOOD', 'NONE', 165, 60, 70),

        -- Gomamon line
        ('Zudomon', 'ULTIMATE', 'VACCINE', 'WATER', 'NONE', 118, 42, 46),
        ('Vikemon', 'MEGA',     'VACCINE', 'ICE',   'NONE', 170, 62, 68)
) AS data(name, stage, attribute, element, specie, base_hp, base_atk, base_def)
WHERE NOT EXISTS (
    SELECT 1
    FROM digimon_infos di
    WHERE di.name = data.name
      AND di.stage = data.stage
);

COMMIT;