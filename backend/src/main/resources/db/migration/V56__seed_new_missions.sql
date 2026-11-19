-- =====================================================================
-- V56: Expansão de missões — novas missões para todas as 6 áreas
-- =====================================================================
-- Áreas:        NATIVE_FOREST, GEAR_SAVANNA, FACTORIAL_TOWN, FREEZELAND, SERVER_DESERT, INFINITY_MOUNTAIN
-- Stages:       BABY, BABY_II, ROOKIE, CHAMPION, ULTIMATE, MEGA
-- Raridades:    COMMON, RARE, EPIC, LEGENDARY
-- ItemTypes:    POTION_SMALL, TRAINING_STONE, DATA_CORE, DIGITAMA_STARTER, DIGITAMA_FIRE, DIGITAMA_WATER,
--               DIGITAMA_NATURE, INCUBATOR_COMMON, INCUBATOR_RARE, INCUBATOR_EPIC,
--               FRAGMENT_ROOKIE, FRAGMENT_CHAMPION, FRAGMENT_ULTIMATE, FRAGMENT_MEGA, EVOLUTION_MATERIAL
-- =====================================================================

-- =====================
-- NATIVE FOREST (2 novas — early game, BABY/BABY_II)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_NF_2', 'Coleta de Dados na Floresta', 'Reúna fragmentos de dados espalhados pela floresta para análise.', 'NATIVE_FOREST', 'BABY', 3, 40, 4, 15),
('MISSION_NF_3', 'Emboscada dos Insetos', 'Digimons inseto estão agitados na floresta. Investigue a causa.', 'NATIVE_FOREST', 'BABY_II', 5, 55, 5, 20);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_NF_2', 'TRAINING_STONE', 2),
('MISSION_NF_2', 'DATA_CORE', 1),
('MISSION_NF_3', 'TRAINING_STONE', 2),
('MISSION_NF_3', 'DATA_CORE', 2);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_NF_2', 'COMMON', 70),
('MISSION_NF_2', 'RARE', 20),
('MISSION_NF_2', 'EPIC', 8),
('MISSION_NF_2', 'LEGENDARY', 2),
('MISSION_NF_3', 'COMMON', 65),
('MISSION_NF_3', 'RARE', 22),
('MISSION_NF_3', 'EPIC', 10),
('MISSION_NF_3', 'LEGENDARY', 3);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_NF_2', 'COMMON', 'TRAINING_STONE', 1),
('MISSION_NF_2', 'RARE', 'DATA_CORE', 2),
('MISSION_NF_2', 'EPIC', 'INCUBATOR_COMMON', 1),
('MISSION_NF_2', 'LEGENDARY', 'FRAGMENT_ROOKIE', 2),
('MISSION_NF_3', 'COMMON', 'DATA_CORE', 1),
('MISSION_NF_3', 'RARE', 'DIGITAMA_NATURE', 1),
('MISSION_NF_3', 'EPIC', 'INCUBATOR_COMMON', 1),
('MISSION_NF_3', 'LEGENDARY', 'FRAGMENT_CHAMPION', 1);

-- =====================
-- GEAR SAVANNA (2 novas — ROOKIE tier)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_GS_2', 'Patrulha nas Engrenagens', 'Verifique o funcionamento das engrenagens gigantes da savana mecânica.', 'GEAR_SAVANNA', 'ROOKIE', 10, 75, 6, 35),
('MISSION_GS_3', 'Caça ao Clockmon Rebelde', 'Um Clockmon está desregulando as máquinas da savana. Encontre-o e pare-o.', 'GEAR_SAVANNA', 'ROOKIE', 12, 90, 7, 45);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_GS_2', 'TRAINING_STONE', 2),
('MISSION_GS_2', 'DATA_CORE', 2),
('MISSION_GS_3', 'DATA_CORE', 3),
('MISSION_GS_3', 'FRAGMENT_ROOKIE', 1);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_GS_2', 'COMMON', 60),
('MISSION_GS_2', 'RARE', 25),
('MISSION_GS_2', 'EPIC', 12),
('MISSION_GS_2', 'LEGENDARY', 3),
('MISSION_GS_3', 'COMMON', 58),
('MISSION_GS_3', 'RARE', 25),
('MISSION_GS_3', 'EPIC', 13),
('MISSION_GS_3', 'LEGENDARY', 4);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_GS_2', 'COMMON', 'TRAINING_STONE', 2),
('MISSION_GS_2', 'RARE', 'DIGITAMA_FIRE', 1),
('MISSION_GS_2', 'EPIC', 'INCUBATOR_RARE', 1),
('MISSION_GS_2', 'LEGENDARY', 'FRAGMENT_CHAMPION', 2),
('MISSION_GS_3', 'COMMON', 'DATA_CORE', 2),
('MISSION_GS_3', 'RARE', 'DIGITAMA_WATER', 1),
('MISSION_GS_3', 'EPIC', 'INCUBATOR_RARE', 1),
('MISSION_GS_3', 'LEGENDARY', 'FRAGMENT_CHAMPION', 3);

-- =====================
-- FACTORIAL TOWN (2 novas — ROOKIE tier avançado)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_FT_2', 'Resgate na Fábrica Abandonada', 'Um Digimon aliado ficou preso nas profundezas da fábrica. Resgate-o antes que as máquinas o destruam.', 'FACTORIAL_TOWN', 'ROOKIE', 18, 120, 7, 70),
('MISSION_FT_3', 'Sabotagem Industrial', 'Desative os geradores de energia que alimentam os Digimons máquina hostis.', 'FACTORIAL_TOWN', 'ROOKIE', 20, 140, 8, 80);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_FT_2', 'DATA_CORE', 3),
('MISSION_FT_2', 'FRAGMENT_ROOKIE', 2),
('MISSION_FT_3', 'DATA_CORE', 3),
('MISSION_FT_3', 'FRAGMENT_CHAMPION', 2);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_FT_2', 'COMMON', 58),
('MISSION_FT_2', 'RARE', 24),
('MISSION_FT_2', 'EPIC', 13),
('MISSION_FT_2', 'LEGENDARY', 5),
('MISSION_FT_3', 'COMMON', 55),
('MISSION_FT_3', 'RARE', 25),
('MISSION_FT_3', 'EPIC', 14),
('MISSION_FT_3', 'LEGENDARY', 6);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_FT_2', 'COMMON', 'DATA_CORE', 2),
('MISSION_FT_2', 'RARE', 'DIGITAMA_NATURE', 1),
('MISSION_FT_2', 'EPIC', 'INCUBATOR_RARE', 1),
('MISSION_FT_2', 'LEGENDARY', 'FRAGMENT_CHAMPION', 3),
('MISSION_FT_3', 'COMMON', 'TRAINING_STONE', 3),
('MISSION_FT_3', 'RARE', 'DIGITAMA_FIRE', 1),
('MISSION_FT_3', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_FT_3', 'LEGENDARY', 'FRAGMENT_CHAMPION', 5);

-- =====================
-- FREEZELAND (2 novas — CHAMPION tier)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_FL_2', 'Caverna de Cristal', 'Explore as cavernas geladas em busca de cristais de dados raros escondidos sob o gelo.', 'FREEZELAND', 'CHAMPION', 28, 200, 8, 130),
('MISSION_FL_3', 'Tempestade de Neve', 'Uma nevasca violenta tomou Freezeland. Sobreviva e proteja os Digimons mais fracos da região.', 'FREEZELAND', 'CHAMPION', 32, 230, 9, 150);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_FL_2', 'DATA_CORE', 3),
('MISSION_FL_2', 'FRAGMENT_CHAMPION', 2),
('MISSION_FL_3', 'FRAGMENT_CHAMPION', 3),
('MISSION_FL_3', 'INCUBATOR_COMMON', 1);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_FL_2', 'COMMON', 52),
('MISSION_FL_2', 'RARE', 26),
('MISSION_FL_2', 'EPIC', 15),
('MISSION_FL_2', 'LEGENDARY', 7),
('MISSION_FL_3', 'COMMON', 50),
('MISSION_FL_3', 'RARE', 27),
('MISSION_FL_3', 'EPIC', 15),
('MISSION_FL_3', 'LEGENDARY', 8);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_FL_2', 'COMMON', 'DATA_CORE', 3),
('MISSION_FL_2', 'RARE', 'DIGITAMA_WATER', 1),
('MISSION_FL_2', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_FL_2', 'LEGENDARY', 'FRAGMENT_ULTIMATE', 2),
('MISSION_FL_3', 'COMMON', 'FRAGMENT_CHAMPION', 2),
('MISSION_FL_3', 'RARE', 'DIGITAMA_FIRE', 1),
('MISSION_FL_3', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_FL_3', 'LEGENDARY', 'FRAGMENT_ULTIMATE', 3);

-- =====================
-- SERVER DESERT (2 novas — ULTIMATE tier)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_SD_2', 'Oásis Corrompido', 'O único oásis do deserto foi corrompido por dados maliciosos. Purifique a fonte antes que se espalhe.', 'SERVER_DESERT', 'ULTIMATE', 45, 350, 9, 200),
('MISSION_SD_3', 'Ruínas do Server Antigo', 'Explorar as ruínas de um servidor abandonado repleto de Digimons corrompidos e tesouros esquecidos.', 'SERVER_DESERT', 'ULTIMATE', 50, 400, 10, 240);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_SD_2', 'DATA_CORE', 4),
('MISSION_SD_2', 'FRAGMENT_ULTIMATE', 2),
('MISSION_SD_3', 'FRAGMENT_ULTIMATE', 3),
('MISSION_SD_3', 'INCUBATOR_RARE', 1);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_SD_2', 'COMMON', 48),
('MISSION_SD_2', 'RARE', 27),
('MISSION_SD_2', 'EPIC', 17),
('MISSION_SD_2', 'LEGENDARY', 8),
('MISSION_SD_3', 'COMMON', 45),
('MISSION_SD_3', 'RARE', 28),
('MISSION_SD_3', 'EPIC', 18),
('MISSION_SD_3', 'LEGENDARY', 9);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_SD_2', 'COMMON', 'FRAGMENT_CHAMPION', 3),
('MISSION_SD_2', 'RARE', 'DIGITAMA_NATURE', 1),
('MISSION_SD_2', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_SD_2', 'LEGENDARY', 'FRAGMENT_ULTIMATE', 5),
('MISSION_SD_3', 'COMMON', 'DATA_CORE', 4),
('MISSION_SD_3', 'RARE', 'FRAGMENT_ULTIMATE', 2),
('MISSION_SD_3', 'EPIC', 'INCUBATOR_EPIC', 1),
('MISSION_SD_3', 'LEGENDARY', 'FRAGMENT_MEGA', 3);

-- =====================
-- INFINITY MOUNTAIN (2 novas — MEGA tier / endgame)
-- =====================

INSERT INTO mission_definitions (id, name, description, area, required_stage, required_level, base_xp, energy_cost, duration_seconds) VALUES
('MISSION_IM_2', 'Portão do Mundo das Trevas', 'Um portão dimensional se abriu no topo da montanha. Feche-o antes que Digimons sombrios invadam.', 'INFINITY_MOUNTAIN', 'MEGA', 65, 600, 10, 360),
('MISSION_IM_3', 'Desafio do Soberano', 'Enfrente o teste final dos Quatro Soberanos Sagrados na arena celestial da montanha.', 'INFINITY_MOUNTAIN', 'MEGA', 70, 750, 10, 420);

INSERT INTO mission_rewards (mission_id, item_type, base_quantity) VALUES
('MISSION_IM_2', 'FRAGMENT_ULTIMATE', 4),
('MISSION_IM_2', 'FRAGMENT_MEGA', 2),
('MISSION_IM_3', 'FRAGMENT_MEGA', 3),
('MISSION_IM_3', 'INCUBATOR_EPIC', 1);

INSERT INTO mission_loot_chances (mission_id, rarity, chance) VALUES
('MISSION_IM_2', 'COMMON', 42),
('MISSION_IM_2', 'RARE', 28),
('MISSION_IM_2', 'EPIC', 19),
('MISSION_IM_2', 'LEGENDARY', 11),
('MISSION_IM_3', 'COMMON', 38),
('MISSION_IM_3', 'RARE', 28),
('MISSION_IM_3', 'EPIC', 20),
('MISSION_IM_3', 'LEGENDARY', 14);

INSERT INTO mission_loot_items (mission_id, rarity, item_type, quantity) VALUES
('MISSION_IM_2', 'COMMON', 'FRAGMENT_CHAMPION', 4),
('MISSION_IM_2', 'RARE', 'FRAGMENT_ULTIMATE', 3),
('MISSION_IM_2', 'EPIC', 'INCUBATOR_EPIC', 2),
('MISSION_IM_2', 'LEGENDARY', 'FRAGMENT_MEGA', 5),
('MISSION_IM_3', 'COMMON', 'FRAGMENT_ULTIMATE', 3),
('MISSION_IM_3', 'RARE', 'FRAGMENT_MEGA', 2),
('MISSION_IM_3', 'EPIC', 'INCUBATOR_EPIC', 2),
('MISSION_IM_3', 'LEGENDARY', 'FRAGMENT_MEGA', 10);
