BEGIN;

INSERT INTO digitama_pools (
    code,
    name,
    description,
    content_id,
    active
)
SELECT
    'DIGITAMA_STARTER',
    'Digitama Inicial',
    'Pool inicial de Digitamas disponíveis no MVP.',
    ac.id,
    true
FROM available_contents ac
WHERE ac.code = 'MVP_INITIAL'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    content_id = EXCLUDED.content_id,
    active = EXCLUDED.active;

INSERT INTO digitama_pool_entries (
    digitama_pool_id,
    digimon_info_id,
    weight,
    active
)
SELECT
    dp.id,
    di.id,
    data.weight,
    data.active
FROM (
    VALUES
        ('Botamon',  20, true),
        ('Punimon',  20, true),
        ('Pichimon', 20, true),
        ('Poyomon',  20, true),
        ('Pabumon',  10, true),
        ('Yuramon',  10, true)
) AS data(digimon_name, weight, active)
JOIN digitama_pools dp
    ON dp.code = 'DIGITAMA_STARTER'
JOIN digimon_infos di
    ON di.name = data.digimon_name
   AND di.stage = 'BABY'
ON CONFLICT (digitama_pool_id, digimon_info_id) DO UPDATE SET
    weight = EXCLUDED.weight,
    active = EXCLUDED.active;

COMMIT;