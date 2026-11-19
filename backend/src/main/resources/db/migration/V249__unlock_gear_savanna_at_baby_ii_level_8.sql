-- Savana Gear precisa fornecer fragmentos Rookie antes da evolução a Rookie.
-- A área usa os requisitos desta missão como condição de entrada.
UPDATE mission_definitions
SET required_stage = 'BABY_II',
    required_level = 8
WHERE id = 'MISSION_2'
  AND area = 'GEAR_SAVANNA';
