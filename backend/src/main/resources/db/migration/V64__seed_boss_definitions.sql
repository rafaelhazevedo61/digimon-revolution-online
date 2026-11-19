-- Seed boss definitions
-- Stage: ROOKIE (Fixed + Daily + Weekly + Monthly)
INSERT INTO boss_definitions (code, name, boss_type, required_stage, required_level, required_rebirths, hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent, image_url) VALUES
('KUWAGAMON',       'Kuwagamon',       'NORMAL',  'ROOKIE',   10, 0, 300, 50, 30, 5, 360, 150, 80, 10, 'https://digimon.shadowsmith.com/img/kuwagamon.jpg'),
('MERAMON',         'Meramon',         'DAILY',   'ROOKIE',   12, 0, 350, 55, 35, 5, 1440, 200, 100, 10, 'https://digimon.shadowsmith.com/img/meramon.jpg'),
('SHELLMON',        'Shellmon',        'DAILY',   'ROOKIE',   12, 0, 400, 45, 40, 5, 1440, 200, 100, 10, 'https://digimon.shadowsmith.com/img/shellmon.jpg'),
('BAKEMON',         'Bakemon',         'DAILY',   'ROOKIE',   12, 0, 320, 60, 25, 5, 1440, 200, 100, 10, 'https://digimon.shadowsmith.com/img/bakemon.jpg'),
('TYRANNOMON',      'Tyrannomon',      'WEEKLY',  'ROOKIE',   15, 0, 500, 70, 50, 8, 10080, 500, 250, 10, 'https://digimon.shadowsmith.com/img/tyrannomon.jpg'),
('PARROTMON',       'Parrotmon',       'MONTHLY', 'ROOKIE',   15, 0, 700, 85, 60, 10, 43200, 1000, 500, 10, 'https://digimon.shadowsmith.com/img/parrotmon.jpg');

-- Stage: CHAMPION (Fixed + Daily + Weekly + Monthly)
INSERT INTO boss_definitions (code, name, boss_type, required_stage, required_level, required_rebirths, hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent, image_url) VALUES
('DEVIMON',         'Devimon',         'NORMAL',  'CHAMPION', 30, 0, 800, 120, 80, 5, 360, 350, 150, 10, 'https://digimon.shadowsmith.com/img/devimon.jpg'),
('LEOMON',          'Leomon',          'DAILY',   'CHAMPION', 32, 0, 850, 130, 90, 5, 1440, 450, 200, 10, 'https://digimon.shadowsmith.com/img/leomon.jpg'),
('OGREMON',         'Ogremon',         'DAILY',   'CHAMPION', 32, 0, 750, 140, 70, 5, 1440, 450, 200, 10, 'https://digimon.shadowsmith.com/img/ogremon.jpg'),
('MONOCHROMON',     'Monochromon',     'DAILY',   'CHAMPION', 32, 0, 900, 110, 100, 5, 1440, 450, 200, 10, 'https://digimon.shadowsmith.com/img/monochromon.jpg'),
('MYOTISMON',       'Myotismon',       'WEEKLY',  'CHAMPION', 35, 0, 1200, 160, 120, 8, 10080, 1000, 500, 10, 'https://digimon.shadowsmith.com/img/myotismon.jpg'),
('ETEMON',          'Etemon',          'MONTHLY', 'CHAMPION', 35, 0, 1500, 180, 140, 10, 43200, 2000, 1000, 10, 'https://digimon.shadowsmith.com/img/etemon.jpg');

-- Stage: ULTIMATE (Fixed + Daily + Weekly + Monthly)
INSERT INTO boss_definitions (code, name, boss_type, required_stage, required_level, required_rebirths, hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent, image_url) VALUES
('ETEMON_BOSS',     'Etemon',          'NORMAL',  'ULTIMATE', 50, 1, 2000, 250, 180, 5, 360, 800, 350, 10, 'https://digimon.shadowsmith.com/img/etemon.jpg'),
('ANDROMON',        'Andromon',        'DAILY',   'ULTIMATE', 52, 1, 2200, 240, 200, 5, 1440, 1000, 450, 10, 'https://digimon.shadowsmith.com/img/andromon.jpg'),
('SKULLGREYMON',    'SkullGreymon',    'DAILY',   'ULTIMATE', 52, 1, 1800, 300, 150, 5, 1440, 1000, 450, 10, 'https://digimon.shadowsmith.com/img/skullgreymon.jpg'),
('VADEMON',         'Vademon',         'DAILY',   'ULTIMATE', 52, 1, 2500, 200, 220, 5, 1440, 1000, 450, 10, 'https://digimon.shadowsmith.com/img/vademon.jpg'),
('VENOMMYOTISMON',  'VenomMyotismon',  'WEEKLY',  'ULTIMATE', 55, 1, 3500, 350, 280, 8, 10080, 2500, 1000, 10, 'https://digimon.shadowsmith.com/img/venommyotismon.jpg'),
('MACHINEDRAMON',   'Machinedramon',   'MONTHLY', 'ULTIMATE', 55, 1, 5000, 400, 350, 10, 43200, 5000, 2500, 10, 'https://digimon.shadowsmith.com/img/machinedramon.jpg');

-- Stage: MEGA (Fixed + Daily + Weekly + Monthly)
INSERT INTO boss_definitions (code, name, boss_type, required_stage, required_level, required_rebirths, hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent, image_url) VALUES
('PIEDMON',         'Piedmon',         'NORMAL',  'MEGA',     70, 2, 6000, 500, 400, 5, 360, 1500, 700, 10, 'https://digimon.shadowsmith.com/img/piedmon.jpg'),
('PUPPETMON',       'Puppetmon',       'DAILY',   'MEGA',     72, 2, 5500, 550, 380, 5, 1440, 2000, 900, 10, 'https://digimon.shadowsmith.com/img/puppetmon.jpg'),
('METALSEADRAMON',  'MetalSeadramon',  'DAILY',   'MEGA',     72, 2, 7000, 480, 450, 5, 1440, 2000, 900, 10, 'https://digimon.shadowsmith.com/img/metalseadramon.jpg'),
('MUGENDRAMON',     'Mugendramon',     'DAILY',   'MEGA',     72, 2, 8000, 450, 500, 5, 1440, 2000, 900, 10, 'https://digimon.shadowsmith.com/img/mugendramon.jpg'),
('BLACKWARGREYMON', 'BlackWarGreymon', 'WEEKLY',  'MEGA',     75, 2, 10000, 600, 550, 8, 10080, 5000, 2500, 10, 'https://digimon.shadowsmith.com/img/blackwargreymon.jpg'),
('APOCALYMON',      'Apocalymon',      'MONTHLY', 'MEGA',     75, 3, 15000, 800, 700, 10, 43200, 10000, 5000, 10, 'https://digimon.shadowsmith.com/img/apocalymon.jpg');

-- Seed boss drops
-- ROOKIE bosses
INSERT INTO boss_drops (boss_id, drop_type, item_code, chance, min_quantity, max_quantity) VALUES
((SELECT id FROM boss_definitions WHERE code='KUWAGAMON'), 'ITEM', 'REFINEMENT_STONE', 30, 1, 1),
((SELECT id FROM boss_definitions WHERE code='KUWAGAMON'), 'ITEM', 'POTION_SMALL', 60, 1, 2),
((SELECT id FROM boss_definitions WHERE code='TYRANNOMON'), 'ITEM', 'REFINEMENT_STONE', 50, 1, 2),
((SELECT id FROM boss_definitions WHERE code='TYRANNOMON'), 'EQUIPMENT', NULL, 15, 1, 1),
((SELECT id FROM boss_definitions WHERE code='PARROTMON'), 'ITEM', 'REFINEMENT_STONE', 70, 2, 3),
((SELECT id FROM boss_definitions WHERE code='PARROTMON'), 'EQUIPMENT', NULL, 25, 1, 1);

-- Set template_name and rarity for equipment drops (Rookie → T2-T3)
UPDATE boss_drops SET template_name = 'Garra Berserker T2', equipment_rarity = 'RARE' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='TYRANNOMON') AND drop_type = 'EQUIPMENT';
UPDATE boss_drops SET template_name = 'Garra Berserker T3', equipment_rarity = 'RARE' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='PARROTMON') AND drop_type = 'EQUIPMENT';

-- CHAMPION bosses
INSERT INTO boss_drops (boss_id, drop_type, item_code, chance, min_quantity, max_quantity) VALUES
((SELECT id FROM boss_definitions WHERE code='DEVIMON'), 'ITEM', 'REFINEMENT_STONE', 40, 1, 2),
((SELECT id FROM boss_definitions WHERE code='DEVIMON'), 'ITEM', 'POTION_SMALL', 50, 2, 3),
((SELECT id FROM boss_definitions WHERE code='MYOTISMON'), 'ITEM', 'REFINEMENT_STONE', 60, 2, 3),
((SELECT id FROM boss_definitions WHERE code='MYOTISMON'), 'EQUIPMENT', NULL, 20, 1, 1),
((SELECT id FROM boss_definitions WHERE code='ETEMON'), 'ITEM', 'REFINEMENT_STONE', 80, 3, 5),
((SELECT id FROM boss_definitions WHERE code='ETEMON'), 'EQUIPMENT', NULL, 30, 1, 1);

UPDATE boss_drops SET template_name = 'Armadura Guardia T4', equipment_rarity = 'EPIC' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='MYOTISMON') AND drop_type = 'EQUIPMENT';
UPDATE boss_drops SET template_name = 'Couraça Berserker T5', equipment_rarity = 'EPIC' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='ETEMON') AND drop_type = 'EQUIPMENT';

-- ULTIMATE bosses
INSERT INTO boss_drops (boss_id, drop_type, item_code, chance, min_quantity, max_quantity) VALUES
((SELECT id FROM boss_definitions WHERE code='ETEMON_BOSS'), 'ITEM', 'REFINEMENT_STONE', 50, 2, 3),
((SELECT id FROM boss_definitions WHERE code='VENOMMYOTISMON'), 'ITEM', 'REFINEMENT_STONE', 70, 3, 5),
((SELECT id FROM boss_definitions WHERE code='VENOMMYOTISMON'), 'EQUIPMENT', NULL, 25, 1, 1),
((SELECT id FROM boss_definitions WHERE code='MACHINEDRAMON'), 'ITEM', 'REFINEMENT_STONE', 90, 4, 6),
((SELECT id FROM boss_definitions WHERE code='MACHINEDRAMON'), 'EQUIPMENT', NULL, 35, 1, 1);

UPDATE boss_drops SET template_name = 'Lança Guardia T6', equipment_rarity = 'EPIC' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='VENOMMYOTISMON') AND drop_type = 'EQUIPMENT';
UPDATE boss_drops SET template_name = 'Cetro da Vitalidade T7', equipment_rarity = 'LEGENDARY' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='MACHINEDRAMON') AND drop_type = 'EQUIPMENT';

-- MEGA bosses
INSERT INTO boss_drops (boss_id, drop_type, item_code, chance, min_quantity, max_quantity) VALUES
((SELECT id FROM boss_definitions WHERE code='PIEDMON'), 'ITEM', 'REFINEMENT_STONE', 60, 3, 5),
((SELECT id FROM boss_definitions WHERE code='BLACKWARGREYMON'), 'ITEM', 'REFINEMENT_STONE', 80, 5, 8),
((SELECT id FROM boss_definitions WHERE code='BLACKWARGREYMON'), 'EQUIPMENT', NULL, 30, 1, 1),
((SELECT id FROM boss_definitions WHERE code='APOCALYMON'), 'ITEM', 'REFINEMENT_STONE', 95, 6, 10),
((SELECT id FROM boss_definitions WHERE code='APOCALYMON'), 'EQUIPMENT', NULL, 40, 1, 1);

UPDATE boss_drops SET template_name = 'Lamina do Equilibrio T8', equipment_rarity = 'LEGENDARY' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='BLACKWARGREYMON') AND drop_type = 'EQUIPMENT';
UPDATE boss_drops SET template_name = 'Lamina do Equilibrio T10', equipment_rarity = 'LEGENDARY' WHERE boss_id = (SELECT id FROM boss_definitions WHERE code='APOCALYMON') AND drop_type = 'EQUIPMENT';
