-- Permite que os itens comprados na Arena ocupem o stack completo de 999 unidades.
UPDATE item_definitions
SET max_stack = 999
WHERE code IN ('POTION_SMALL', 'TRAINING_STONE', 'DATA_CORE');

COMMIT;
