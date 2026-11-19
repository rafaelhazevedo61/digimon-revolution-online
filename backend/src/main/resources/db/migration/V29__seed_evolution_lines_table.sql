INSERT INTO evolution_lines
(code, name, description, content_id, active)
VALUES
(
    'AGUMON_LINE_1',
    'Linha Agumon 1',
    'Linha evolutiva inicial focada em fogo e dragão.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
),
(
    'GABUMON_LINE_1',
    'Linha Gabumon 1',
    'Linha evolutiva inicial focada em fera e gelo.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
),
(
    'GOMAMON_LINE_1',
    'Linha Gomamon 1',
    'Linha evolutiva inicial focada em água.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
),
(
    'PATAMON_LINE_1',
    'Linha Patamon 1',
    'Linha evolutiva inicial focada em luz.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
),
(
    'TENTOMON_LINE_1',
    'Linha Tentomon 1',
    'Linha evolutiva inicial focada em inseto e natureza.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
),
(
    'BIYOMON_LINE_1',
    'Linha Biyomon 1',
    'Linha evolutiva inicial focada em vento e ave.',
    (SELECT id FROM available_contents WHERE code = 'MVP_INITIAL'),
    TRUE
);