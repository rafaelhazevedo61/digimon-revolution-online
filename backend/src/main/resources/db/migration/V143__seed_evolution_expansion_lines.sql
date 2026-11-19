BEGIN;

INSERT INTO evolution_lines (
    code,
    name,
    description,
    content_id,
    active
)
SELECT
    data.code,
    data.name,
    data.description,
    content.id,
    TRUE
FROM (
    VALUES
        ('GUILMON_LINE_1',
         'Linha Guilmon',
         'Linha evolutiva de fogo e dragão que culmina em Dukemon.'),
        ('DORUMON_LINE_1',
         'Linha DORUmon',
         'Linha evolutiva de fera dracônica e protótipo X que culmina em DORUgoramon.'),
        ('DIABLOMON_LINE_1',
         'Linha Diablomon',
         'Linha evolutiva sombria de malware e invasão digital.'),
        ('GRAN_KUWAGAMON_LINE_1',
         'Linha Gran Kuwagamon',
         'Linha evolutiva de inseto e floresta com foco em dano contínuo.'),
        ('SAINT_GALGOMON_LINE_1',
         'Linha Saint Galgomon',
         'Linha evolutiva de fera de longo alcance que culmina em uma máquina pesada.'),
        ('CHERUBIMON_VIRTUE_LINE_1',
         'Linha Cherubimon (Virtue)',
         'Linha evolutiva de coelho marcial e entidade angelical.'),
        ('KAZUCHIMON_LINE_1',
         'Linha Kazuchimon',
         'Linha evolutiva elétrica focada em velocidade, combos e iniciativa.'),
        ('IMPERIALDRAMON_LINE_1',
         'Linha Imperialdramon',
         'Linha evolutiva de dragão blindado com progressão de fusão.'),
        ('HEXEBLAUMON_LINE_1',
         'Linha Hexeblaumon',
         'Linha evolutiva de gelo, controle e defesa elevada.'),
        ('OURYUMON_LINE_1',
         'Linha Ouryumon',
         'Linha evolutiva de lâmina e dragão celestial.' )
) AS data(code, name, description)
JOIN available_contents content
  ON content.code = 'EVOLUTION_EXPANSION_1'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content_id = EXCLUDED.content_id,
    active = EXCLUDED.active;

COMMIT;
