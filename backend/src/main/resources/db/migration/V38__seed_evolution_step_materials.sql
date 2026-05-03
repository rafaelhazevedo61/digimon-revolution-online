BEGIN;

INSERT INTO evolution_step_materials (evolution_line_step_id, material_code, quantity, description)
SELECT
    els.id,
    data.material_code,
    data.quantity,
    data.description
FROM (
    VALUES
        -- AGUMON_LINE_1: Botamon → Koromon (step 2)
        ('AGUMON_LINE_1', 2, 'FRAGMENT_KOROMON', 5, 'Fragmento do Koromon'),
        -- AGUMON_LINE_1: Koromon → Agumon (step 3)
        ('AGUMON_LINE_1', 3, 'FRAGMENT_AGUMON', 10, 'Fragmento do Agumon'),
        -- AGUMON_LINE_1: Agumon → Greymon (step 4)
        ('AGUMON_LINE_1', 4, 'FRAGMENT_GREYMON', 20, 'Fragmento do Greymon'),

        -- GABUMON_LINE_1: Punimon → Tsunomon (step 2)
        ('GABUMON_LINE_1', 2, 'FRAGMENT_TSUNOMON', 5, 'Fragmento do Tsunomon'),
        -- GABUMON_LINE_1: Tsunomon → Gabumon (step 3)
        ('GABUMON_LINE_1', 3, 'FRAGMENT_GABUMON', 10, 'Fragmento do Gabumon'),
        -- GABUMON_LINE_1: Gabumon → Garurumon (step 4)
        ('GABUMON_LINE_1', 4, 'FRAGMENT_GARURUMON', 20, 'Fragmento do Garurumon'),

        -- PATAMON_LINE_1: Poyomon → Tokomon (step 2)
        ('PATAMON_LINE_1', 2, 'FRAGMENT_TOKOMON', 5, 'Fragmento do Tokomon'),
        -- PATAMON_LINE_1: Tokomon → Patamon (step 3)
        ('PATAMON_LINE_1', 3, 'FRAGMENT_PATAMON', 10, 'Fragmento do Patamon'),
        -- PATAMON_LINE_1: Patamon → Angemon (step 4)
        ('PATAMON_LINE_1', 4, 'FRAGMENT_ANGEMON', 20, 'Fragmento do Angemon'),

        -- BIYOMON_LINE_1: Yuramon → Yokomon (step 2)
        ('BIYOMON_LINE_1', 2, 'FRAGMENT_YOKOMON', 5, 'Fragmento do Yokomon'),
        -- BIYOMON_LINE_1: Yokomon → Biyomon (step 3)
        ('BIYOMON_LINE_1', 3, 'FRAGMENT_BIYOMON', 10, 'Fragmento do Biyomon'),
        -- BIYOMON_LINE_1: Biyomon → Birdramon (step 4)
        ('BIYOMON_LINE_1', 4, 'FRAGMENT_BIRDRAMON', 20, 'Fragmento do Birdramon'),

        -- TENTOMON_LINE_1: Pabumon → Motimon (step 2)
        ('TENTOMON_LINE_1', 2, 'FRAGMENT_MOTIMON', 5, 'Fragmento do Motimon'),
        -- TENTOMON_LINE_1: Motimon → Tentomon (step 3)
        ('TENTOMON_LINE_1', 3, 'FRAGMENT_TENTOMON', 10, 'Fragmento do Tentomon'),
        -- TENTOMON_LINE_1: Tentomon → Kabuterimon (step 4)
        ('TENTOMON_LINE_1', 4, 'FRAGMENT_KABUTERIMON', 20, 'Fragmento do Kabuterimon'),

        -- GOMAMON_LINE_1: Pichimon → Bukamon (step 2)
        ('GOMAMON_LINE_1', 2, 'FRAGMENT_BUKAMON', 5, 'Fragmento do Bukamon'),
        -- GOMAMON_LINE_1: Bukamon → Gomamon (step 3)
        ('GOMAMON_LINE_1', 3, 'FRAGMENT_GOMAMON', 10, 'Fragmento do Gomamon'),
        -- GOMAMON_LINE_1: Gomamon → Ikkakumon (step 4)
        ('GOMAMON_LINE_1', 4, 'FRAGMENT_IKKAKUMON', 20, 'Fragmento do Ikkakumon')
) AS data(line_code, step_order, material_code, quantity, description)
JOIN evolution_lines el ON el.code = data.line_code
JOIN evolution_line_steps els ON els.evolution_line_id = el.id AND els.step_order = data.step_order
ON CONFLICT DO NOTHING;

COMMIT;
