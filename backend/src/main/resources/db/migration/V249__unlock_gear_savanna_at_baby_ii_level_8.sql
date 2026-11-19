-- A área Savana Gear acompanha os requisitos da missão de entrada.
UPDATE mission_definitions
SET required_stage = 'BABY_II', required_level = 8
WHERE id = 'MISSION_2' AND area = 'GEAR_SAVANNA';
