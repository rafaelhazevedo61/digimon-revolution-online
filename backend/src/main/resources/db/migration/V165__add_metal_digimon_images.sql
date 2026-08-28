BEGIN;

-- Usa assets empacotados no backend para evitar URLs externas inexistentes.
UPDATE digimon_infos
SET image_url = CASE name
    WHEN 'MetalKoromon' THEN '/digimon/MetalKoromon.png'
    WHEN 'Kapurimon' THEN '/digimon/Kapurimon.png'
    WHEN 'HiAndromon' THEN '/digimon/HiAndromon.png'
    WHEN 'MetalEtemon' THEN '/digimon/MetalEtemon.png'
    WHEN 'MetalSeadramon' THEN '/digimon/MetalSeadramon.png'
END
WHERE name IN (
    'MetalKoromon',
    'Kapurimon',
    'HiAndromon',
    'MetalEtemon',
    'MetalSeadramon'
);

COMMIT;
