BEGIN;

-- Corrige URLs quebradas das imagens de Digimon adicionadas na expansão.
-- A atualização é idempotente e não altera migrations já aplicadas.
UPDATE digimon_infos
SET image_url = CASE name
    WHEN 'Phantomon' THEN 'https://digimon.shadowsmith.com/img/phantomon.jpg'
    WHEN 'Myotismon' THEN 'https://digimon.shadowsmith.com/img/myotismon.jpg'
END
WHERE name IN ('Phantomon', 'Myotismon');

COMMIT;
