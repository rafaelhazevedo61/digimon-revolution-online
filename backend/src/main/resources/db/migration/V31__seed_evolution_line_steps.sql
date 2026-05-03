BEGIN;

INSERT INTO evolution_line_steps (
    evolution_line_id,
    digimon_info_id,
    stage,
    step_order
)
SELECT
    el.id,
    di.id,
    data.stage,
    data.step_order
FROM (
    VALUES
        -- Botamon → Koromon → Agumon → Greymon
        ('AGUMON_LINE_1', 'Botamon', 'BABY',     1),
        ('AGUMON_LINE_1', 'Koromon', 'BABY_II',  2),
        ('AGUMON_LINE_1', 'Agumon',  'ROOKIE',   3),
        ('AGUMON_LINE_1', 'Greymon', 'CHAMPION', 4),

        -- Punimon → Tsunomon → Gabumon → Garurumon
        ('GABUMON_LINE_1', 'Punimon',   'BABY',     1),
        ('GABUMON_LINE_1', 'Tsunomon',  'BABY_II',  2),
        ('GABUMON_LINE_1', 'Gabumon',   'ROOKIE',   3),
        ('GABUMON_LINE_1', 'Garurumon', 'CHAMPION', 4),

        -- Poyomon → Tokomon → Patamon → Angemon
        ('PATAMON_LINE_1', 'Poyomon', 'BABY',     1),
        ('PATAMON_LINE_1', 'Tokomon', 'BABY_II',  2),
        ('PATAMON_LINE_1', 'Patamon', 'ROOKIE',   3),
        ('PATAMON_LINE_1', 'Angemon', 'CHAMPION', 4),

        -- Yuramon → Yokomon → Biyomon → Birdramon
        ('BIYOMON_LINE_1', 'Yuramon',   'BABY',     1),
        ('BIYOMON_LINE_1', 'Yokomon',   'BABY_II',  2),
        ('BIYOMON_LINE_1', 'Biyomon',   'ROOKIE',   3),
        ('BIYOMON_LINE_1', 'Birdramon', 'CHAMPION', 4),

        -- Pabumon → Motimon → Tentomon → Kabuterimon
        ('TENTOMON_LINE_1', 'Pabumon',     'BABY',     1),
        ('TENTOMON_LINE_1', 'Motimon',     'BABY_II',  2),
        ('TENTOMON_LINE_1', 'Tentomon',    'ROOKIE',   3),
        ('TENTOMON_LINE_1', 'Kabuterimon', 'CHAMPION', 4),

        -- Pichimon → Bukamon → Gomamon → Ikkakumon
        ('GOMAMON_LINE_1', 'Pichimon',   'BABY',     1),
        ('GOMAMON_LINE_1', 'Bukamon',    'BABY_II',  2),
        ('GOMAMON_LINE_1', 'Gomamon',    'ROOKIE',   3),
        ('GOMAMON_LINE_1', 'Ikkakumon',  'CHAMPION', 4)
) AS data(line_code, digimon_name, stage, step_order)
JOIN evolution_lines el
    ON el.code = data.line_code
JOIN digimon_infos di
    ON di.name = data.digimon_name
   AND di.stage = data.stage
ON CONFLICT DO NOTHING;

COMMIT;