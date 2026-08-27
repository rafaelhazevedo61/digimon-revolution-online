-- V142 usou o rótulo externo ELECTRIC, mas o domínio Java e os demais
-- catálogos do jogo usam THUNDER para o elemento elétrico.
UPDATE digimon_infos
SET element = 'THUNDER'
WHERE name IN (
    'Gargomon',
    'Rapidmon Perfect',
    'Saint Galgomon',
    'Pusurimon',
    'Pulsemon',
    'Bulkmon',
    'Boutmon',
    'Kazuchimon'
)
  AND element = 'ELECTRIC';
