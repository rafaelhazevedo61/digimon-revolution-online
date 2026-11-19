-- Reativa a economia de energia com a nova capacidade baseada no nível.
-- A capacidade base é 100 no nível 1, com +5 por nível, além do bônus de trait.
BEGIN;

UPDATE digimons
SET max_energy = 100 + ((GREATEST(1, LEAST(level, 100)) - 1) * 5)
              + CASE WHEN trait = 'ENERGETIC' THEN 5 ELSE 0 END,
    energy = 100 + ((GREATEST(1, LEAST(level, 100)) - 1) * 5)
           + CASE WHEN trait = 'ENERGETIC' THEN 5 ELSE 0 END,
    last_energy_update = NOW();

COMMIT;
