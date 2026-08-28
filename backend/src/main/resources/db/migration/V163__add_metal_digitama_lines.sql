BEGIN;

-- Digitama Metal: os Babys são exclusivos deste pool.
-- Os estágios seguintes podem ser compartilhados com outras linhas, como é
-- esperado no sistema de ramificações evolutivas.
INSERT INTO digimon_infos (name, stage, attribute, element, specie, base_hp, base_atk, base_def, image_url) VALUES
    ('MetalKoromon', 'BABY', 'DATA', 'STEEL', 'MACHINE', 12, 8, 2, 'https://digi-api.com/images/digimon/w/MetalKoromon.png'),
    ('Mekamon',      'BABY', 'VIRUS', 'STEEL', 'MACHINE', 12, 8, 2, 'https://digi-api.com/images/digimon/w/Mekamon.png'),
    ('Hagurumon',    'ROOKIE', 'VIRUS', 'STEEL', 'MACHINE', 16, 4, 5, 'https://digi-api.com/images/digimon/w/Hagurumon.png'),
    ('Thunderballmon', 'CHAMPION', 'VIRUS', 'STEEL', 'MACHINE', 59, 27, 20, 'https://digi-api.com/images/digimon/w/Thunderballmon.png'),
    ('Megadramon',   'ULTIMATE', 'VIRUS', 'STEEL', 'MACHINE', 102, 49, 37, 'https://digi-api.com/images/digimon/w/Megadramon.png'),
    ('Machinedramon','MEGA', 'VIRUS', 'STEEL', 'MACHINE', 154, 73, 53, 'https://digi-api.com/images/digimon/w/Machinedramon.png')
ON CONFLICT (name) DO UPDATE SET
    stage = EXCLUDED.stage,
    attribute = EXCLUDED.attribute,
    element = EXCLUDED.element,
    specie = EXCLUDED.specie,
    base_hp = EXCLUDED.base_hp,
    base_atk = EXCLUDED.base_atk,
    base_def = EXCLUDED.base_def;

INSERT INTO evolution_lines (code, name, description, content_id, active)
SELECT data.code, data.name, data.description, content.id, TRUE
FROM (
    VALUES
        ('METALKOROMON_LINE_1', 'Linha MetalKoromon', 'Linha evolutiva de máquina metálica até HiAndromon.'),
        ('MEKAMON_LINE_1', 'Linha Mekamon', 'Linha evolutiva de máquina metálica até Machinedramon.')
) AS data(code, name, description)
JOIN available_contents content ON content.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content_id = EXCLUDED.content_id,
    active = EXCLUDED.active;

INSERT INTO evolution_line_steps (evolution_line_id, digimon_info_id, stage, step_order, required_level)
SELECT line.id, info.id, data.stage, data.step_order,
       CASE data.step_order
           WHEN 1 THEN 1
           WHEN 2 THEN 10
           WHEN 3 THEN 15
           WHEN 4 THEN 25
           WHEN 5 THEN 50
           WHEN 6 THEN 75
       END
FROM (
    VALUES
        ('METALKOROMON_LINE_1', 1, 'MetalKoromon', 'BABY'),
        ('METALKOROMON_LINE_1', 2, 'Kapurimon', 'BABY_II'),
        ('METALKOROMON_LINE_1', 3, 'Hagurumon', 'ROOKIE'),
        ('METALKOROMON_LINE_1', 4, 'Guardromon', 'CHAMPION'),
        ('METALKOROMON_LINE_1', 5, 'Andromon', 'ULTIMATE'),
        ('METALKOROMON_LINE_1', 6, 'HiAndromon', 'MEGA'),
        ('MEKAMON_LINE_1', 1, 'Mekamon', 'BABY'),
        ('MEKAMON_LINE_1', 2, 'Kapurimon', 'BABY_II'),
        ('MEKAMON_LINE_1', 3, 'Kokuwamon', 'ROOKIE'),
        ('MEKAMON_LINE_1', 4, 'Thunderballmon', 'CHAMPION'),
        ('MEKAMON_LINE_1', 5, 'Megadramon', 'ULTIMATE'),
        ('MEKAMON_LINE_1', 6, 'Machinedramon', 'MEGA')
) AS data(line_code, step_order, digimon_name, stage)
JOIN evolution_lines line ON line.code = data.line_code
JOIN digimon_infos info ON info.name = data.digimon_name
ON CONFLICT (evolution_line_id, step_order) DO UPDATE SET
    digimon_info_id = EXCLUDED.digimon_info_id,
    stage = EXCLUDED.stage,
    required_level = EXCLUDED.required_level;

-- V137 foi executada antes destes novos Digimons; por isso o pool Steel
-- precisa receber as entradas explicitamente nesta migration.
INSERT INTO digitama_pool_entries (digitama_pool_id, digimon_info_id, weight, active)
SELECT pool.id, info.id, 50, TRUE
FROM digitama_pools pool
JOIN digimon_infos info ON info.name IN ('MetalKoromon', 'Mekamon')
WHERE pool.code = 'DIGITAMA_STEEL'
  AND info.stage = 'BABY'
ON CONFLICT (digitama_pool_id, digimon_info_id) DO UPDATE SET
    weight = EXCLUDED.weight,
    active = EXCLUDED.active;

-- Fragmentos dos estágios novos. Os demais já existem no catálogo.
INSERT INTO item_definitions (code, name, description, category, stackable, buy_price, sell_price, tradable, sellable, usable, max_stack, rarity, icon) VALUES
    ('FRAGMENT_HAGURUMON', 'Fragmento do Hagurumon', 'Fragmento para evoluir para Hagurumon.', 'EVOLUTION_MATERIAL', TRUE, NULL, 10, TRUE, TRUE, FALSE, 999, 'COMMON', 'fragment_baby2'),
    ('FRAGMENT_THUNDERBALLMON', 'Fragmento do Thunderballmon', 'Fragmento para evoluir para Thunderballmon.', 'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_MEGADRAMON', 'Fragmento do Megadramon', 'Fragmento para evoluir para Megadramon.', 'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_MACHINEDRAMON', 'Fragmento do Machinedramon', 'Fragmento para evoluir para Machinedramon.', 'EVOLUTION_MATERIAL', TRUE, NULL, 250, TRUE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    sell_price = EXCLUDED.sell_price,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

INSERT INTO evolution_step_materials (evolution_line_step_id, material_code, quantity, description)
SELECT step.id, data.material_code, data.quantity, data.description
FROM (
    VALUES
        ('METALKOROMON_LINE_1', 2, 'FRAGMENT_KAPURIMON', 5, 'Fragmento do Kapurimon'),
        ('METALKOROMON_LINE_1', 3, 'FRAGMENT_HAGURUMON', 10, 'Fragmento do Hagurumon'),
        ('METALKOROMON_LINE_1', 4, 'FRAGMENT_GUARDROMON', 20, 'Fragmento do Guardromon'),
        ('METALKOROMON_LINE_1', 5, 'FRAGMENT_ANDROMON', 30, 'Fragmento do Andromon'),
        ('METALKOROMON_LINE_1', 6, 'FRAGMENT_HIANDROMON', 50, 'Fragmento do HiAndromon'),
        ('MEKAMON_LINE_1', 2, 'FRAGMENT_KAPURIMON', 5, 'Fragmento do Kapurimon'),
        ('MEKAMON_LINE_1', 3, 'FRAGMENT_KOKUWAMON', 10, 'Fragmento do Kokuwamon'),
        ('MEKAMON_LINE_1', 4, 'FRAGMENT_THUNDERBALLMON', 20, 'Fragmento do Thunderballmon'),
        ('MEKAMON_LINE_1', 5, 'FRAGMENT_MEGADRAMON', 30, 'Fragmento do Megadramon'),
        ('MEKAMON_LINE_1', 6, 'FRAGMENT_MACHINEDRAMON', 50, 'Fragmento do Machinedramon')
) AS data(line_code, step_order, material_code, quantity, description)
JOIN evolution_lines line ON line.code = data.line_code
JOIN evolution_line_steps step
  ON step.evolution_line_id = line.id
 AND step.step_order = data.step_order
ON CONFLICT DO NOTHING;

COMMIT;
