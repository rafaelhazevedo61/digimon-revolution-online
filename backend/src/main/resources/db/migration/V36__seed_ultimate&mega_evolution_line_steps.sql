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
        -- Botamon → Koromon → Agumon → Greymon → MetalGreymon → WarGreymon
        ('AGUMON_LINE_1', 'MetalGreymon', 'ULTIMATE', 5),
        ('AGUMON_LINE_1', 'WarGreymon',   'MEGA',     6),

        -- Punimon → Tsunomon → Gabumon → Garurumon → WereGarurumon → MetalGarurumon
        ('GABUMON_LINE_1', 'WereGarurumon',  'ULTIMATE', 5),
        ('GABUMON_LINE_1', 'MetalGarurumon', 'MEGA',     6),

        -- Poyomon → Tokomon → Patamon → Angemon → MagnaAngemon → Seraphimon
        ('PATAMON_LINE_1', 'MagnaAngemon', 'ULTIMATE', 5),
        ('PATAMON_LINE_1', 'Seraphimon',   'MEGA',     6),

        -- Yuramon → Yokomon → Biyomon → Birdramon → Garudamon → Phoenixmon
        ('BIYOMON_LINE_1', 'Garudamon',  'ULTIMATE', 5),
        ('BIYOMON_LINE_1', 'Phoenixmon', 'MEGA',     6),

        -- Pabumon → Motimon → Tentomon → Kabuterimon → MegaKabuterimon → HerculesKabuterimon
        ('TENTOMON_LINE_1', 'MegaKabuterimon',     'ULTIMATE', 5),
        ('TENTOMON_LINE_1', 'HerculesKabuterimon', 'MEGA',     6),

        -- Pichimon → Bukamon → Gomamon → Ikkakumon → Zudomon → Vikemon
        ('GOMAMON_LINE_1', 'Zudomon', 'ULTIMATE', 5),
        ('GOMAMON_LINE_1', 'Vikemon', 'MEGA',     6)
) AS data(line_code, digimon_name, stage, step_order)
JOIN evolution_lines el
    ON el.code = data.line_code
JOIN digimon_infos di
    ON di.name = data.digimon_name
   AND di.stage = data.stage
ON CONFLICT (evolution_line_id, step_order) DO UPDATE SET
    digimon_info_id = EXCLUDED.digimon_info_id,
    stage = EXCLUDED.stage;

COMMIT;