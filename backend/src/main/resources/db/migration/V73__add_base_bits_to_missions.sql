-- Recompensa de bits por missão (além do XP e itens).
-- Valor configurável por missão no admin; default 0.
ALTER TABLE mission_definitions ADD COLUMN base_bits INT NOT NULL DEFAULT 0;

-- Seed inicial: define bits proporcional ao XP base das missões já existentes.
UPDATE mission_definitions SET base_bits = ROUND(base_xp * 0.5);
