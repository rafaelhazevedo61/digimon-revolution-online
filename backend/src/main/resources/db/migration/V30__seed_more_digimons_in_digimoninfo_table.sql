BEGIN;

-- Garante que os IDs gerados pela sequência não colidam com os IDs explícitos inseridos em V25
ALTER SEQUENCE IF EXISTS digimon_infos_id_seq RESTART WITH 54;

INSERT INTO digimon_infos (
    name,
    stage,
    attribute,
    element,
    specie,
    base_hp,
    base_atk,
    base_def
) VALUES
-- Botamon → Koromon → Agumon → Greymon
('Koromon', 'BABY_II', 'DATA',    'FIRE',  'NONE', 22, 7, 8),
('Agumon',  'ROOKIE',  'VACCINE', 'FIRE',  'NONE', 38, 15, 12),
('Greymon', 'CHAMPION','VACCINE', 'FIRE',  'NONE', 68, 28, 22),

-- Punimon → Tsunomon → Gabumon → Garurumon
('Tsunomon',  'BABY_II', 'VACCINE', 'WATER', 'NONE', 24, 8, 7),
('Gabumon',   'ROOKIE',  'VACCINE', 'WATER', 'NONE', 36, 14, 13),
('Garurumon', 'CHAMPION','VACCINE', 'WATER', 'NONE', 62, 27, 24),

-- Poyomon → Tokomon → Patamon → Angemon
('Tokomon', 'BABY_II', 'DATA',    'LIGHT', 'NONE', 21, 7, 9),
('Patamon', 'ROOKIE',  'VACCINE', 'LIGHT', 'NONE', 35, 13, 14),
('Angemon', 'CHAMPION','VACCINE', 'LIGHT', 'NONE', 60, 30, 23),

-- Yuramon → Yokomon → Biyomon → Birdramon
('Yokomon',  'BABY_II', 'DATA',    'WIND', 'NONE', 22, 8, 7),
('Biyomon',  'ROOKIE',  'VACCINE', 'WIND', 'NONE', 34, 16, 11),
('Birdramon','CHAMPION','VACCINE', 'WIND', 'NONE', 58, 31, 21),

-- Pabumon → Motimon → Tentomon → Kabuterimon
('Motimon',     'BABY_II', 'VACCINE', 'WOOD', 'NONE', 25, 6, 9),
('Tentomon',    'ROOKIE',  'VACCINE', 'WOOD', 'NONE', 37, 13, 15),
('Kabuterimon', 'CHAMPION','VACCINE', 'WOOD', 'NONE', 72, 24, 28),

-- Pichimon → Bukamon → Gomamon → Ikkakumon
('Bukamon',   'BABY_II', 'DATA',    'WATER', 'NONE', 23, 7, 8),
('Gomamon',   'ROOKIE',  'VACCINE', 'WATER', 'NONE', 39, 13, 14),
('Ikkakumon', 'CHAMPION','VACCINE', 'WATER', 'NONE', 76, 23, 27);

COMMIT;