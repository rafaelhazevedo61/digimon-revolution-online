
CREATE TABLE digimon_infos (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    stage VARCHAR(20) NOT NULL,
    attribute VARCHAR(20) NOT NULL,
    element VARCHAR(20) NOT NULL,
    specie VARCHAR(20) NOT NULL,
    base_hp INTEGER NOT NULL,
    base_atk INTEGER NOT NULL,
    base_def INTEGER NOT NULL
);

INSERT INTO digimon_infos (name, stage, attribute, element, specie, base_hp, base_atk, base_def) VALUES
('Botamon', 'BABY', 'DATA', 'NEUTRAL','NONE', 10, 5, 3),
('Punimon', 'BABY', 'VACCINE', 'WATER','NONE', 10, 5, 3),
('Pichimon', 'BABY', 'DATA', 'WATER','NONE', 10, 5, 3),
('Poyomon', 'BABY', 'DATA', 'LIGHT','NONE', 10, 5, 3),
('Pabumon', 'BABY', 'VACCINE', 'WIND','NONE', 10, 5, 3),
('Yuramon', 'BABY', 'DATA', 'WOOD','NONE', 10, 5, 3);