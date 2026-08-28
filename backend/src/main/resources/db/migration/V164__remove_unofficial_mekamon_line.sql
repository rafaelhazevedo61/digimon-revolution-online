BEGIN;

-- Mekamon não é um Digimon oficial e não deve permanecer na Digitama Metal.
-- A limpeza é idempotente para funcionar tanto em bases novas quanto em bases
-- onde a V163 já tenha sido executada.
DELETE FROM digitama_pool_entries
WHERE digimon_info_id IN (
    SELECT id FROM digimon_infos WHERE name = 'Mekamon'
);

DELETE FROM evolution_step_materials
WHERE evolution_line_step_id IN (
    SELECT step.id
    FROM evolution_line_steps step
    JOIN evolution_lines line ON line.id = step.evolution_line_id
    WHERE line.code = 'MEKAMON_LINE_1'
);

DELETE FROM evolution_line_steps
WHERE evolution_line_id IN (
    SELECT id FROM evolution_lines WHERE code = 'MEKAMON_LINE_1'
);

DELETE FROM evolution_lines
WHERE code = 'MEKAMON_LINE_1';

DELETE FROM item_definitions
WHERE code IN (
    'FRAGMENT_THUNDERBALLMON',
    'FRAGMENT_MEGADRAMON',
    'FRAGMENT_MACHINEDRAMON'
)
AND NOT EXISTS (
    SELECT 1
    FROM evolution_step_materials material
    WHERE material.material_code = item_definitions.code
);

DELETE FROM digimon_infos
WHERE name IN ('Mekamon', 'Thunderballmon', 'Megadramon', 'Machinedramon')
AND NOT EXISTS (
    SELECT 1
    FROM evolution_line_steps step
    WHERE step.digimon_info_id = digimon_infos.id
);

COMMIT;
