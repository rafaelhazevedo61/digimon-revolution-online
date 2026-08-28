BEGIN;

-- Corrige as imagens para URLs públicas; não altera migrations anteriores já aplicadas.
UPDATE digimon_infos
SET image_url = CASE name
    WHEN 'MetalKoromon' THEN 'https://static.wikia.nocookie.net/digimon/images/c/c8/MetalKoromon_b.jpg/revision/latest?cb=20090616063459'
    WHEN 'Kapurimon' THEN 'https://static.wikia.nocookie.net/digimon/images/9/9d/Kapurimon_b.jpg/revision/latest?cb=20090128050524'
    WHEN 'HiAndromon' THEN 'https://static.wikia.nocookie.net/digimon/images/1/11/HiAndromon_b.jpg/revision/latest?cb=20090127181114'
    WHEN 'MetalEtemon' THEN 'https://static.wikia.nocookie.net/digimon/images/b/ba/MetalEtemon_b.jpg/revision/latest?cb=20090127182609'
    WHEN 'MetalSeadramon' THEN 'https://static.wikia.nocookie.net/digimon/images/d/d0/MetalSeadramon_b.jpg/revision/latest?cb=20110916015608'
END
WHERE name IN (
    'MetalKoromon',
    'Kapurimon',
    'HiAndromon',
    'MetalEtemon',
    'MetalSeadramon'
);

COMMIT;
