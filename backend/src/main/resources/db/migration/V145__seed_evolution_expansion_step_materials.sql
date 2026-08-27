BEGIN;

INSERT INTO evolution_step_materials (
    evolution_line_step_id,
    material_code,
    quantity,
    description
)
SELECT
    step.id,
    data.material_code,
    data.quantity,
    data.description
FROM (
    VALUES
        -- Linha Guilmon
        ('GUILMON_LINE_1', 2, 'FRAGMENT_GIGIMON',                    5,  'Fragmento do Gigimon'),
        ('GUILMON_LINE_1', 3, 'FRAGMENT_GUILMON',                   10,  'Fragmento do Guilmon'),
        ('GUILMON_LINE_1', 4, 'FRAGMENT_GROWMON',                   20,  'Fragmento do Growmon'),
        ('GUILMON_LINE_1', 5, 'FRAGMENT_MEGALOGROWMON',              30,  'Fragmento do Megalo Growmon'),
        ('GUILMON_LINE_1', 6, 'FRAGMENT_DUKEMON',                    50,  'Fragmento do Dukemon'),

        -- Linha DORUmon
        ('DORUMON_LINE_1', 2, 'FRAGMENT_DORIMON',                   5,  'Fragmento do Dorimon'),
        ('DORUMON_LINE_1', 3, 'FRAGMENT_DORUMON',                  10,  'Fragmento do DORUmon'),
        ('DORUMON_LINE_1', 4, 'FRAGMENT_DORUGAMON',                20,  'Fragmento do DORUgamon'),
        ('DORUMON_LINE_1', 5, 'FRAGMENT_DORUGUREMON',              30,  'Fragmento do DORUguremon'),
        ('DORUMON_LINE_1', 6, 'FRAGMENT_DORUGORAMON',              50,  'Fragmento do DORUgoramon'),

        -- Linha Diablomon
        ('DIABLOMON_LINE_1', 2, 'FRAGMENT_TSUMEMON',                5,  'Fragmento do Tsumemon'),
        ('DIABLOMON_LINE_1', 3, 'FRAGMENT_KERAMON',                10,  'Fragmento do Keramon'),
        ('DIABLOMON_LINE_1', 4, 'FRAGMENT_CHRYSALIMON',            20,  'Fragmento do Chrysalimon'),
        ('DIABLOMON_LINE_1', 5, 'FRAGMENT_INFERMON',               30,  'Fragmento do Infermon'),
        ('DIABLOMON_LINE_1', 6, 'FRAGMENT_DIABLOMON',               50,  'Fragmento do Diablomon'),

        -- Linha Gran Kuwagamon
        ('GRAN_KUWAGAMON_LINE_1', 2, 'FRAGMENT_MINOMON',             5,  'Fragmento do Minomon'),
        ('GRAN_KUWAGAMON_LINE_1', 3, 'FRAGMENT_WORMMON',             10,  'Fragmento do Wormmon'),
        ('GRAN_KUWAGAMON_LINE_1', 4, 'FRAGMENT_STINGMON',            20,  'Fragmento do Stingmon'),
        ('GRAN_KUWAGAMON_LINE_1', 5, 'FRAGMENT_JEWELBEEMON',         30,  'Fragmento do Jewelbeemon'),
        ('GRAN_KUWAGAMON_LINE_1', 6, 'FRAGMENT_GRANKUWAGAMON',       50,  'Fragmento do Gran Kuwagamon'),

        -- Linha Saint Galgomon
        ('SAINT_GALGOMON_LINE_1', 2, 'FRAGMENT_GUMMYMON',             5,  'Fragmento do Gummymon'),
        ('SAINT_GALGOMON_LINE_1', 3, 'FRAGMENT_TERRIERMON',          10,  'Fragmento do Terriermon'),
        ('SAINT_GALGOMON_LINE_1', 4, 'FRAGMENT_GARGOMON',             20,  'Fragmento do Gargomon'),
        ('SAINT_GALGOMON_LINE_1', 5, 'FRAGMENT_RAPIDMONPERFECT',      30,  'Fragmento do Rapidmon Perfect'),
        ('SAINT_GALGOMON_LINE_1', 6, 'FRAGMENT_SAINTGALGOMON',        50,  'Fragmento do Saint Galgomon'),

        -- Linha Cherubimon (Virtue)
        ('CHERUBIMON_VIRTUE_LINE_1', 2, 'FRAGMENT_CHOCOMON',          5,  'Fragmento do Chocomon'),
        ('CHERUBIMON_VIRTUE_LINE_1', 3, 'FRAGMENT_LOPMON',            10,  'Fragmento do Lopmon'),
        ('CHERUBIMON_VIRTUE_LINE_1', 4, 'FRAGMENT_TURUIEMON',         20,  'Fragmento do Turuiemon'),
        ('CHERUBIMON_VIRTUE_LINE_1', 5, 'FRAGMENT_ANDIRAMON',          30,  'Fragmento do Andiramon'),
        ('CHERUBIMON_VIRTUE_LINE_1', 6, 'FRAGMENT_CHERUBIMONVIRTUE',   50,  'Fragmento do Cherubimon (Virtue)'),

        -- Linha Kazuchimon
        ('KAZUCHIMON_LINE_1', 2, 'FRAGMENT_PUSURIMON',               5,  'Fragmento do Pusurimon'),
        ('KAZUCHIMON_LINE_1', 3, 'FRAGMENT_PULSEMON',                10,  'Fragmento do Pulsemon'),
        ('KAZUCHIMON_LINE_1', 4, 'FRAGMENT_BULKMON',                  20,  'Fragmento do Bulkmon'),
        ('KAZUCHIMON_LINE_1', 5, 'FRAGMENT_BOUTMON',                  30,  'Fragmento do Boutmon'),
        ('KAZUCHIMON_LINE_1', 6, 'FRAGMENT_KAZUCHIMON',               50,  'Fragmento do Kazuchimon'),

        -- Linha Imperialdramon
        ('IMPERIALDRAMON_LINE_1', 2, 'FRAGMENT_CHIBIMON',             5,  'Fragmento do Chibimon'),
        ('IMPERIALDRAMON_LINE_1', 3, 'FRAGMENT_VMON',                  10,  'Fragmento do V-mon'),
        ('IMPERIALDRAMON_LINE_1', 4, 'FRAGMENT_XVMON',                 20,  'Fragmento do XV-mon'),
        ('IMPERIALDRAMON_LINE_1', 5, 'FRAGMENT_PAILDRAMON',            30,  'Fragmento do Paildramon'),
        ('IMPERIALDRAMON_LINE_1', 6, 'FRAGMENT_IMPERIALDRAMONDRAGONMODE', 50, 'Fragmento do Imperialdramon (Dragon Mode)'),

        -- Linha Hexeblaumon
        ('HEXEBLAUMON_LINE_1', 2, 'FRAGMENT_HIYARIMON',               5,  'Fragmento do Hiyarimon'),
        ('HEXEBLAUMON_LINE_1', 3, 'FRAGMENT_BLUCOMON',                10,  'Fragmento do Blucomon'),
        ('HEXEBLAUMON_LINE_1', 4, 'FRAGMENT_PALEDRAMON',               20,  'Fragmento do Paledramon'),
        ('HEXEBLAUMON_LINE_1', 5, 'FRAGMENT_CRYSPALEDRAMON',           30,  'Fragmento do Crys Paledramon'),
        ('HEXEBLAUMON_LINE_1', 6, 'FRAGMENT_HEXEBLAUMON',              50,  'Fragmento do Hexeblaumon'),

        -- Linha Ouryumon
        ('OURYUMON_LINE_1', 2, 'FRAGMENT_SAKUTTOMON',                5,  'Fragmento do Sakuttomon'),
        ('OURYUMON_LINE_1', 3, 'FRAGMENT_RYUDAMON',                  10,  'Fragmento do Ryudamon'),
        ('OURYUMON_LINE_1', 4, 'FRAGMENT_GINRYUMON',                 20,  'Fragmento do Ginryumon'),
        ('OURYUMON_LINE_1', 5, 'FRAGMENT_HISYARYUMON',               30,  'Fragmento do Hisyaryumon'),
        ('OURYUMON_LINE_1', 6, 'FRAGMENT_OURYUMON',                  50,  'Fragmento do Ouryumon')
) AS data(line_code, step_order, material_code, quantity, description)
JOIN evolution_lines line ON line.code = data.line_code
JOIN evolution_line_steps step ON step.evolution_line_id = line.id AND step.step_order = data.step_order
WHERE NOT EXISTS (
    SELECT 1
    FROM evolution_step_materials existing
    WHERE existing.evolution_line_step_id = step.id
      AND existing.material_code = data.material_code
);

DO $$
DECLARE
    material_count INTEGER;
BEGIN
    SELECT COUNT(*)
      INTO material_count
      FROM evolution_step_materials material
      JOIN evolution_line_steps step ON step.id = material.evolution_line_step_id
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

    IF material_count <> 50 THEN
        RAISE EXCEPTION 'Expected 50 evolution materials for EVOLUTION_EXPANSION_1, found %', material_count;
    END IF;
END $$;

COMMIT;
