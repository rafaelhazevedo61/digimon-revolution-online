BEGIN;

-- Pool: DIGITAMA_FIRE (Botamon, Punimon)
INSERT INTO digitama_pools (code, name, description, content_id, active)
SELECT 'DIGITAMA_FIRE', 'Digitama de Fogo', 'Pool de Digitamas do elemento Fogo.', ac.id, true
FROM available_contents ac WHERE ac.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, content_id = EXCLUDED.content_id, active = EXCLUDED.active;

-- Pool: DIGITAMA_WATER (Pichimon, Poyomon)
INSERT INTO digitama_pools (code, name, description, content_id, active)
SELECT 'DIGITAMA_WATER', 'Digitama de Água', 'Pool de Digitamas do elemento Água.', ac.id, true
FROM available_contents ac WHERE ac.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, content_id = EXCLUDED.content_id, active = EXCLUDED.active;

-- Pool: DIGITAMA_NATURE (Pabumon, Yuramon)
INSERT INTO digitama_pools (code, name, description, content_id, active)
SELECT 'DIGITAMA_NATURE', 'Digitama de Natureza', 'Pool de Digitamas do elemento Natureza.', ac.id, true
FROM available_contents ac WHERE ac.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description, content_id = EXCLUDED.content_id, active = EXCLUDED.active;

-- Entries: DIGITAMA_FIRE
INSERT INTO digitama_pool_entries (digitama_pool_id, digimon_info_id, weight, active)
SELECT dp.id, di.id, data.weight, data.active
FROM (VALUES ('Botamon', 50, true), ('Punimon', 50, true)) AS data(digimon_name, weight, active)
JOIN digitama_pools dp ON dp.code = 'DIGITAMA_FIRE'
JOIN digimon_infos di ON di.name = data.digimon_name AND di.stage = 'BABY'
ON CONFLICT (digitama_pool_id, digimon_info_id) DO UPDATE SET weight = EXCLUDED.weight, active = EXCLUDED.active;

-- Entries: DIGITAMA_WATER
INSERT INTO digitama_pool_entries (digitama_pool_id, digimon_info_id, weight, active)
SELECT dp.id, di.id, data.weight, data.active
FROM (VALUES ('Pichimon', 50, true), ('Poyomon', 50, true)) AS data(digimon_name, weight, active)
JOIN digitama_pools dp ON dp.code = 'DIGITAMA_WATER'
JOIN digimon_infos di ON di.name = data.digimon_name AND di.stage = 'BABY'
ON CONFLICT (digitama_pool_id, digimon_info_id) DO UPDATE SET weight = EXCLUDED.weight, active = EXCLUDED.active;

-- Entries: DIGITAMA_NATURE
INSERT INTO digitama_pool_entries (digitama_pool_id, digimon_info_id, weight, active)
SELECT dp.id, di.id, data.weight, data.active
FROM (VALUES ('Pabumon', 50, true), ('Yuramon', 50, true)) AS data(digimon_name, weight, active)
JOIN digitama_pools dp ON dp.code = 'DIGITAMA_NATURE'
JOIN digimon_infos di ON di.name = data.digimon_name AND di.stage = 'BABY'
ON CONFLICT (digitama_pool_id, digimon_info_id) DO UPDATE SET weight = EXCLUDED.weight, active = EXCLUDED.active;

COMMIT;
