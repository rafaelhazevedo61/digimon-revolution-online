-- Seed mission definitions (from MissionCatalog.java)

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_ADMIN', 'Missão de Teste', 'Missão administrativa para testes e debug.', 'NATIVE_FOREST', 'BABY', 1, 999, 1, 5),
('MISSION_1', 'Patrulha na Floresta Nativa', 'Explore a floresta onde os Digimons recém-nascidos dão seus primeiros passos.', 'NATIVE_FOREST', 'BABY', 1, 30, 5, 10),
('MISSION_2', 'Caçada em Gear Savanna', 'Enfrente Digimons selvagens nas vastas planícies mecânicas de Gear Savanna.', 'GEAR_SAVANNA', 'ROOKIE', 8, 60, 6, 30),
('MISSION_3', 'Investigação em Factorial Town', 'Infiltre-se na cidade-fábrica e colete dados das máquinas descontroladas.', 'FACTORIAL_TOWN', 'ROOKIE', 15, 100, 7, 60),
('MISSION_4', 'Expedição em Freezeland', 'Sobreviva ao frio extremo de Freezeland e derrote os Digimons de gelo.', 'FREEZELAND', 'CHAMPION', 25, 180, 8, 120),
('MISSION_5', 'Travessia do Deserto do Server', 'Cruze o vasto deserto do Server Continent enfrentando Digimons poderosos.', 'SERVER_DESERT', 'ULTIMATE', 40, 300, 9, 180),
('MISSION_6', 'Ascensão à Infinity Mountain', 'Escale a lendária Infinity Mountain e enfrente os Digimons mais poderosos do Mundo Digital.', 'INFINITY_MOUNTAIN', 'MEGA', 60, 500, 10, 300);

-- Seed fixed rewards
INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
-- MISSION_ADMIN
('MISSION_ADMIN', 'TRAINING_STONE', 10),
('MISSION_ADMIN', 'DATA_CORE', 5),
('MISSION_ADMIN', 'FRAGMENT_CHAMPION', 3),
('MISSION_ADMIN', 'FRAGMENT_MEGA', 1),
-- MISSION_1
('MISSION_1', 'TRAINING_STONE', 1),
-- MISSION_2
('MISSION_2', 'TRAINING_STONE', 1),
('MISSION_2', 'DATA_CORE', 1),
-- MISSION_3
('MISSION_3', 'DATA_CORE', 2),
('MISSION_3', 'FRAGMENT_CHAMPION', 1),
-- MISSION_4
('MISSION_4', 'DATA_CORE', 2),
('MISSION_4', 'FRAGMENT_CHAMPION', 2),
-- MISSION_5
('MISSION_5', 'DATA_CORE', 3),
('MISSION_5', 'FRAGMENT_ULTIMATE', 2),
-- MISSION_6
('MISSION_6', 'FRAGMENT_ULTIMATE', 3),
('MISSION_6', 'INCUBATOR_RARE', 1);

-- Seed loot rarity chances
INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
-- MISSION_ADMIN
('MISSION_ADMIN', 'COMMON', 40),
('MISSION_ADMIN', 'RARE', 30),
('MISSION_ADMIN', 'EPIC', 20),
('MISSION_ADMIN', 'LEGENDARY', 10),
-- MISSION_1
('MISSION_1', 'COMMON', 70),
('MISSION_1', 'RARE', 20),
('MISSION_1', 'EPIC', 8),
('MISSION_1', 'LEGENDARY', 2),
-- MISSION_2
('MISSION_2', 'COMMON', 65),
('MISSION_2', 'RARE', 22),
('MISSION_2', 'EPIC', 10),
('MISSION_2', 'LEGENDARY', 3),
-- MISSION_3
('MISSION_3', 'COMMON', 60),
('MISSION_3', 'RARE', 23),
('MISSION_3', 'EPIC', 12),
('MISSION_3', 'LEGENDARY', 5),
-- MISSION_4
('MISSION_4', 'COMMON', 55),
('MISSION_4', 'RARE', 25),
('MISSION_4', 'EPIC', 14),
('MISSION_4', 'LEGENDARY', 6),
-- MISSION_5
('MISSION_5', 'COMMON', 50),
('MISSION_5', 'RARE', 27),
('MISSION_5', 'EPIC', 16),
('MISSION_5', 'LEGENDARY', 7),
-- MISSION_6
('MISSION_6', 'COMMON', 45),
('MISSION_6', 'RARE', 28),
('MISSION_6', 'EPIC', 18),
('MISSION_6', 'LEGENDARY', 9);

-- Seed loot items
INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
-- MISSION_ADMIN
('MISSION_ADMIN', 'COMMON', 'TRAINING_STONE', 5),
('MISSION_ADMIN', 'RARE', 'DIGITAMA_FIRE', 1),
('MISSION_ADMIN', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_ADMIN', 'LEGENDARY', 'FRAGMENT_MEGA', 50),
-- MISSION_1
('MISSION_1', 'COMMON', 'TRAINING_STONE', 1),
('MISSION_1', 'RARE', 'DATA_CORE', 1),
('MISSION_1', 'EPIC', 'DIGITAMA_FIRE', 1),
('MISSION_1', 'LEGENDARY', 'FRAGMENT_CHAMPION', 1),
-- MISSION_2
('MISSION_2', 'COMMON', 'DATA_CORE', 1),
('MISSION_2', 'RARE', 'DIGITAMA_WATER', 1),
('MISSION_2', 'EPIC', 'INCUBATOR_RARE', 1),
('MISSION_2', 'LEGENDARY', 'FRAGMENT_CHAMPION', 3),
-- MISSION_3
('MISSION_3', 'COMMON', 'TRAINING_STONE', 2),
('MISSION_3', 'RARE', 'DIGITAMA_NATURE', 1),
('MISSION_3', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_3', 'LEGENDARY', 'FRAGMENT_CHAMPION', 5),
-- MISSION_4
('MISSION_4', 'COMMON', 'DATA_CORE', 2),
('MISSION_4', 'RARE', 'DIGITAMA_FIRE', 1),
('MISSION_4', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_4', 'LEGENDARY', 'FRAGMENT_ULTIMATE', 3),
-- MISSION_5
('MISSION_5', 'COMMON', 'FRAGMENT_CHAMPION', 2),
('MISSION_5', 'RARE', 'DIGITAMA_WATER', 1),
('MISSION_5', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_5', 'LEGENDARY', 'FRAGMENT_ULTIMATE', 5),
-- MISSION_6
('MISSION_6', 'COMMON', 'FRAGMENT_CHAMPION', 3),
('MISSION_6', 'RARE', 'FRAGMENT_ULTIMATE', 2),
('MISSION_6', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_6', 'LEGENDARY', 'FRAGMENT_MEGA', 10);
