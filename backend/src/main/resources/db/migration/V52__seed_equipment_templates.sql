-- Templates de equipamentos iniciais
-- slot:   WEAPON | ARMOR | ACCESSORY
-- rarity: COMMON | RARE | EPIC | LEGENDARY

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense) VALUES
-- Weapons
('Iron Claw',              'WEAPON',    'COMMON',    0,  5,  0),
('Steel Blade',            'WEAPON',    'RARE',      0, 12,  0),
('Chrome Digizoid Sword',  'WEAPON',    'EPIC',      0, 25,  3),
('Omega Blade',            'WEAPON',    'LEGENDARY',  5, 40,  5),
-- Armors
('Leather Armor',          'ARMOR',     'COMMON',    5,  0,  5),
('Digi-Armor',             'ARMOR',     'RARE',     10,  0, 12),
('Chrome Digizoid Armor',  'ARMOR',     'EPIC',     20,  0, 25),
('Royal Knight Armor',     'ARMOR',     'LEGENDARY', 35,  5, 40),
-- Accessories
('Holy Ring',              'ACCESSORY', 'COMMON',    3,  3,  3),
('Digivice',               'ACCESSORY', 'RARE',      5,  5,  5),
('Crest of Courage',       'ACCESSORY', 'EPIC',     10, 10, 10),
('Digi-Egg of Miracles',   'ACCESSORY', 'LEGENDARY', 20, 20, 20);
