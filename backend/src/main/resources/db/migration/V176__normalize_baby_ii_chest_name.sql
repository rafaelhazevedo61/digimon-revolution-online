-- Normaliza a apresentação do estágio no nome do baú de fragmentos BABY II.
-- Os demais baús usam a forma "Baby II"/nome de estágio capitalizado,
-- portanto a correção é aplicada nos registros relacionados ao mesmo código.

UPDATE loot_tables
SET name = 'Loot Table Baú de Fragmentos - Baby II',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code = 'LOOT_TABLE_SHOP_FRAGMENT_BABY_II';

UPDATE item_definitions
SET name = 'Baú de Fragmentos - Baby II'
WHERE code = 'CHEST_FRAGMENT_BABY_II';

UPDATE chest_definitions
SET name = 'Baú de Fragmentos - Baby II',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code = 'CHEST_FRAGMENT_BABY_II';
