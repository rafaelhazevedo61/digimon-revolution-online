BEGIN;

INSERT INTO evolution_line_steps (
    evolution_line_id,
    digimon_info_id,
    stage,
    step_order,
    required_level
)
SELECT
    line.id,
    info.id,
    data.stage,
    data.step_order,
    CASE data.step_order
        WHEN 1 THEN 1
        WHEN 2 THEN 10
        WHEN 3 THEN 15
        WHEN 4 THEN 25
        WHEN 5 THEN 50
        WHEN 6 THEN 75
        ELSE 1
    END
FROM (
    VALUES
        -- Jyarimon → Gigimon → Guilmon → Growmon → Megalo Growmon → Dukemon
        ('GUILMON_LINE_1', 1, 'Jyarimon',         'BABY'),
        ('GUILMON_LINE_1', 2, 'Gigimon',          'BABY_II'),
        ('GUILMON_LINE_1', 3, 'Guilmon',          'ROOKIE'),
        ('GUILMON_LINE_1', 4, 'Growmon',          'CHAMPION'),
        ('GUILMON_LINE_1', 5, 'Megalo Growmon',   'ULTIMATE'),
        ('GUILMON_LINE_1', 6, 'Dukemon',          'MEGA'),

        -- Dodomon → Dorimon → DORUmon → DORUgamon → DORUguremon → DORUgoramon
        ('DORUMON_LINE_1', 1, 'Dodomon',          'BABY'),
        ('DORUMON_LINE_1', 2, 'Dorimon',          'BABY_II'),
        ('DORUMON_LINE_1', 3, 'DORUmon',          'ROOKIE'),
        ('DORUMON_LINE_1', 4, 'DORUgamon',        'CHAMPION'),
        ('DORUMON_LINE_1', 5, 'DORUguremon',      'ULTIMATE'),
        ('DORUMON_LINE_1', 6, 'DORUgoramon',      'MEGA'),

        -- Kuramon → Tsumemon → Keramon → Chrysalimon → Infermon → Diablomon
        ('DIABLOMON_LINE_1', 1, 'Kuramon',        'BABY'),
        ('DIABLOMON_LINE_1', 2, 'Tsumemon',       'BABY_II'),
        ('DIABLOMON_LINE_1', 3, 'Keramon',        'ROOKIE'),
        ('DIABLOMON_LINE_1', 4, 'Chrysalimon',    'CHAMPION'),
        ('DIABLOMON_LINE_1', 5, 'Infermon',       'ULTIMATE'),
        ('DIABLOMON_LINE_1', 6, 'Diablomon',      'MEGA'),

        -- Leafmon → Minomon → Wormmon → Stingmon → Jewelbeemon → Gran Kuwagamon
        ('GRAN_KUWAGAMON_LINE_1', 1, 'Leafmon',       'BABY'),
        ('GRAN_KUWAGAMON_LINE_1', 2, 'Minomon',       'BABY_II'),
        ('GRAN_KUWAGAMON_LINE_1', 3, 'Wormmon',       'ROOKIE'),
        ('GRAN_KUWAGAMON_LINE_1', 4, 'Stingmon',      'CHAMPION'),
        ('GRAN_KUWAGAMON_LINE_1', 5, 'Jewelbeemon',   'ULTIMATE'),
        ('GRAN_KUWAGAMON_LINE_1', 6, 'Gran Kuwagamon', 'MEGA'),

        -- Zerimon → Gummymon → Terriermon → Gargomon → Rapidmon Perfect → Saint Galgomon
        ('SAINT_GALGOMON_LINE_1', 1, 'Zerimon',          'BABY'),
        ('SAINT_GALGOMON_LINE_1', 2, 'Gummymon',         'BABY_II'),
        ('SAINT_GALGOMON_LINE_1', 3, 'Terriermon',       'ROOKIE'),
        ('SAINT_GALGOMON_LINE_1', 4, 'Gargomon',          'CHAMPION'),
        ('SAINT_GALGOMON_LINE_1', 5, 'Rapidmon Perfect', 'ULTIMATE'),
        ('SAINT_GALGOMON_LINE_1', 6, 'Saint Galgomon',   'MEGA'),

        -- Cocomon → Chocomon → Lopmon → Turuiemon → Andiramon → Cherubimon (Virtue)
        ('CHERUBIMON_VIRTUE_LINE_1', 1, 'Cocomon',            'BABY'),
        ('CHERUBIMON_VIRTUE_LINE_1', 2, 'Chocomon',            'BABY_II'),
        ('CHERUBIMON_VIRTUE_LINE_1', 3, 'Lopmon',              'ROOKIE'),
        ('CHERUBIMON_VIRTUE_LINE_1', 4, 'Turuiemon',           'CHAMPION'),
        ('CHERUBIMON_VIRTUE_LINE_1', 5, 'Andiramon',           'ULTIMATE'),
        ('CHERUBIMON_VIRTUE_LINE_1', 6, 'Cherubimon (Virtue)', 'MEGA'),

        -- Pusumon → Pusurimon → Pulsemon → Bulkmon → Boutmon → Kazuchimon
        ('KAZUCHIMON_LINE_1', 1, 'Pusumon',      'BABY'),
        ('KAZUCHIMON_LINE_1', 2, 'Pusurimon',    'BABY_II'),
        ('KAZUCHIMON_LINE_1', 3, 'Pulsemon',     'ROOKIE'),
        ('KAZUCHIMON_LINE_1', 4, 'Bulkmon',       'CHAMPION'),
        ('KAZUCHIMON_LINE_1', 5, 'Boutmon',       'ULTIMATE'),
        ('KAZUCHIMON_LINE_1', 6, 'Kazuchimon',    'MEGA'),

        -- Chicomon → Chibimon → V-mon → XV-mon → Paildramon → Imperialdramon (Dragon Mode)
        ('IMPERIALDRAMON_LINE_1', 1, 'Chicomon',                    'BABY'),
        ('IMPERIALDRAMON_LINE_1', 2, 'Chibimon',                    'BABY_II'),
        ('IMPERIALDRAMON_LINE_1', 3, 'V-mon',                       'ROOKIE'),
        ('IMPERIALDRAMON_LINE_1', 4, 'XV-mon',                      'CHAMPION'),
        ('IMPERIALDRAMON_LINE_1', 5, 'Paildramon',                  'ULTIMATE'),
        ('IMPERIALDRAMON_LINE_1', 6, 'Imperialdramon (Dragon Mode)', 'MEGA'),

        -- Yukimi Botamon → Hiyarimon → Blucomon → Paledramon → Crys Paledramon → Hexeblaumon
        ('HEXEBLAUMON_LINE_1', 1, 'Yukimi Botamon', 'BABY'),
        ('HEXEBLAUMON_LINE_1', 2, 'Hiyarimon',      'BABY_II'),
        ('HEXEBLAUMON_LINE_1', 3, 'Blucomon',       'ROOKIE'),
        ('HEXEBLAUMON_LINE_1', 4, 'Paledramon',     'CHAMPION'),
        ('HEXEBLAUMON_LINE_1', 5, 'Crys Paledramon','ULTIMATE'),
        ('HEXEBLAUMON_LINE_1', 6, 'Hexeblaumon',    'MEGA'),

        -- Sakumon → Sakuttomon → Ryudamon → Ginryumon → Hisyaryumon → Ouryumon
        ('OURYUMON_LINE_1', 1, 'Sakumon',     'BABY'),
        ('OURYUMON_LINE_1', 2, 'Sakuttomon',  'BABY_II'),
        ('OURYUMON_LINE_1', 3, 'Ryudamon',    'ROOKIE'),
        ('OURYUMON_LINE_1', 4, 'Ginryumon',   'CHAMPION'),
        ('OURYUMON_LINE_1', 5, 'Hisyaryumon', 'ULTIMATE'),
        ('OURYUMON_LINE_1', 6, 'Ouryumon',    'MEGA')
) AS data(line_code, step_order, digimon_name, stage)
JOIN evolution_lines line ON line.code = data.line_code
JOIN digimon_infos info ON info.name = data.digimon_name AND info.stage = data.stage
ON CONFLICT (evolution_line_id, step_order) DO UPDATE SET
    digimon_info_id = EXCLUDED.digimon_info_id,
    stage = EXCLUDED.stage,
    required_level = EXCLUDED.required_level;

DO $$
DECLARE
    inserted_steps INTEGER;
BEGIN
    SELECT COUNT(*)
      INTO inserted_steps
      FROM evolution_line_steps step
      JOIN evolution_lines line ON line.id = step.evolution_line_id
     WHERE line.code IN (
         'GUILMON_LINE_1',
         'DORUMON_LINE_1',
         'DIABLOMON_LINE_1',
         'GRAN_KUWAGAMON_LINE_1',
         'SAINT_GALGOMON_LINE_1',
         'CHERUBIMON_VIRTUE_LINE_1',
         'KAZUCHIMON_LINE_1',
         'IMPERIALDRAMON_LINE_1',
         'HEXEBLAUMON_LINE_1',
         'OURYUMON_LINE_1'
     );

    IF inserted_steps <> 60 THEN
        RAISE EXCEPTION 'Expected 60 evolution steps for EVOLUTION_EXPANSION_1, found %', inserted_steps;
    END IF;
END $$;

COMMIT;
