-- Seed dos 4 sets × 3 slots × 10 tiers = 120 equipment templates
-- Multiplicadores de tier: T1=1x, T2=1.5x, T3=2x, T4=2.5x, T5=3x, T6=3.5x, T7=4x, T8=4.5x, T9=5x, T10=5.5x
-- Refinamento é aplicado em runtime, não nos base stats

-- ============================================================
-- SET BERSERKER (escala ATK)
-- ============================================================
-- Arma: ATK base=10
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Garra Berserker T1',  'WEAPON', 'COMMON',    0, 10, 0, 'BERSERKER', 1,  true),
('Garra Berserker T2',  'WEAPON', 'COMMON',    0, 15, 0, 'BERSERKER', 2,  true),
('Garra Berserker T3',  'WEAPON', 'RARE',      0, 20, 0, 'BERSERKER', 3,  true),
('Garra Berserker T4',  'WEAPON', 'RARE',      0, 25, 0, 'BERSERKER', 4,  true),
('Garra Berserker T5',  'WEAPON', 'EPIC',      0, 30, 0, 'BERSERKER', 5,  true),
('Garra Berserker T6',  'WEAPON', 'EPIC',      0, 35, 0, 'BERSERKER', 6,  true),
('Garra Berserker T7',  'WEAPON', 'LEGENDARY', 0, 40, 0, 'BERSERKER', 7,  true),
('Garra Berserker T8',  'WEAPON', 'LEGENDARY', 0, 45, 0, 'BERSERKER', 8,  true),
('Garra Berserker T9',  'WEAPON', 'LEGENDARY', 0, 50, 0, 'BERSERKER', 9,  true),
('Garra Berserker T10', 'WEAPON', 'LEGENDARY', 0, 55, 0, 'BERSERKER', 10, true);

-- Armadura: HP=5, ATK=4, DEF=3
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Couraça Berserker T1',  'ARMOR', 'COMMON',    5,  4,  3, 'BERSERKER', 1,  true),
('Couraça Berserker T2',  'ARMOR', 'COMMON',    8,  6,  5, 'BERSERKER', 2,  true),
('Couraça Berserker T3',  'ARMOR', 'RARE',     10,  8,  6, 'BERSERKER', 3,  true),
('Couraça Berserker T4',  'ARMOR', 'RARE',     13, 10,  8, 'BERSERKER', 4,  true),
('Couraça Berserker T5',  'ARMOR', 'EPIC',     15, 12,  9, 'BERSERKER', 5,  true),
('Couraça Berserker T6',  'ARMOR', 'EPIC',     18, 14, 11, 'BERSERKER', 6,  true),
('Couraça Berserker T7',  'ARMOR', 'LEGENDARY',20, 16, 12, 'BERSERKER', 7,  true),
('Couraça Berserker T8',  'ARMOR', 'LEGENDARY',23, 18, 14, 'BERSERKER', 8,  true),
('Couraça Berserker T9',  'ARMOR', 'LEGENDARY',25, 20, 15, 'BERSERKER', 9,  true),
('Couraça Berserker T10', 'ARMOR', 'LEGENDARY',28, 22, 17, 'BERSERKER', 10, true);

-- Acessório: ATK=6, DEF=2
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Emblema Berserker T1',  'ACCESSORY', 'COMMON',    0,  6,  2, 'BERSERKER', 1,  true),
('Emblema Berserker T2',  'ACCESSORY', 'COMMON',    0,  9,  3, 'BERSERKER', 2,  true),
('Emblema Berserker T3',  'ACCESSORY', 'RARE',      0, 12,  4, 'BERSERKER', 3,  true),
('Emblema Berserker T4',  'ACCESSORY', 'RARE',      0, 15,  5, 'BERSERKER', 4,  true),
('Emblema Berserker T5',  'ACCESSORY', 'EPIC',      0, 18,  6, 'BERSERKER', 5,  true),
('Emblema Berserker T6',  'ACCESSORY', 'EPIC',      0, 21,  7, 'BERSERKER', 6,  true),
('Emblema Berserker T7',  'ACCESSORY', 'LEGENDARY', 0, 24,  8, 'BERSERKER', 7,  true),
('Emblema Berserker T8',  'ACCESSORY', 'LEGENDARY', 0, 27,  9, 'BERSERKER', 8,  true),
('Emblema Berserker T9',  'ACCESSORY', 'LEGENDARY', 0, 30, 10, 'BERSERKER', 9,  true),
('Emblema Berserker T10', 'ACCESSORY', 'LEGENDARY', 0, 33, 11, 'BERSERKER', 10, true);

-- ============================================================
-- SET GUARDIÃO (escala DEF/HP)
-- ============================================================
-- Arma: ATK=4, DEF=4
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Lança Guardiã T1',  'WEAPON', 'COMMON',    0,  4,  4, 'GUARDIAN', 1,  true),
('Lança Guardiã T2',  'WEAPON', 'COMMON',    0,  6,  6, 'GUARDIAN', 2,  true),
('Lança Guardiã T3',  'WEAPON', 'RARE',      0,  8,  8, 'GUARDIAN', 3,  true),
('Lança Guardiã T4',  'WEAPON', 'RARE',      0, 10, 10, 'GUARDIAN', 4,  true),
('Lança Guardiã T5',  'WEAPON', 'EPIC',      0, 12, 12, 'GUARDIAN', 5,  true),
('Lança Guardiã T6',  'WEAPON', 'EPIC',      0, 14, 14, 'GUARDIAN', 6,  true),
('Lança Guardiã T7',  'WEAPON', 'LEGENDARY', 0, 16, 16, 'GUARDIAN', 7,  true),
('Lança Guardiã T8',  'WEAPON', 'LEGENDARY', 0, 18, 18, 'GUARDIAN', 8,  true),
('Lança Guardiã T9',  'WEAPON', 'LEGENDARY', 0, 20, 20, 'GUARDIAN', 9,  true),
('Lança Guardiã T10', 'WEAPON', 'LEGENDARY', 0, 22, 22, 'GUARDIAN', 10, true);

-- Armadura: HP=15, DEF=8
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Armadura Guardiã T1',  'ARMOR', 'COMMON',    15, 0,  8, 'GUARDIAN', 1,  true),
('Armadura Guardiã T2',  'ARMOR', 'COMMON',    23, 0, 12, 'GUARDIAN', 2,  true),
('Armadura Guardiã T3',  'ARMOR', 'RARE',      30, 0, 16, 'GUARDIAN', 3,  true),
('Armadura Guardiã T4',  'ARMOR', 'RARE',      38, 0, 20, 'GUARDIAN', 4,  true),
('Armadura Guardiã T5',  'ARMOR', 'EPIC',      45, 0, 24, 'GUARDIAN', 5,  true),
('Armadura Guardiã T6',  'ARMOR', 'EPIC',      53, 0, 28, 'GUARDIAN', 6,  true),
('Armadura Guardiã T7',  'ARMOR', 'LEGENDARY', 60, 0, 32, 'GUARDIAN', 7,  true),
('Armadura Guardiã T8',  'ARMOR', 'LEGENDARY', 68, 0, 36, 'GUARDIAN', 8,  true),
('Armadura Guardiã T9',  'ARMOR', 'LEGENDARY', 75, 0, 40, 'GUARDIAN', 9,  true),
('Armadura Guardiã T10', 'ARMOR', 'LEGENDARY', 83, 0, 44, 'GUARDIAN', 10, true);

-- Acessório: HP=8, DEF=5
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Medalha Guardiã T1',  'ACCESSORY', 'COMMON',     8, 0,  5, 'GUARDIAN', 1,  true),
('Medalha Guardiã T2',  'ACCESSORY', 'COMMON',    12, 0,  8, 'GUARDIAN', 2,  true),
('Medalha Guardiã T3',  'ACCESSORY', 'RARE',      16, 0, 10, 'GUARDIAN', 3,  true),
('Medalha Guardiã T4',  'ACCESSORY', 'RARE',      20, 0, 13, 'GUARDIAN', 4,  true),
('Medalha Guardiã T5',  'ACCESSORY', 'EPIC',      24, 0, 15, 'GUARDIAN', 5,  true),
('Medalha Guardiã T6',  'ACCESSORY', 'EPIC',      28, 0, 18, 'GUARDIAN', 6,  true),
('Medalha Guardiã T7',  'ACCESSORY', 'LEGENDARY', 32, 0, 20, 'GUARDIAN', 7,  true),
('Medalha Guardiã T8',  'ACCESSORY', 'LEGENDARY', 36, 0, 23, 'GUARDIAN', 8,  true),
('Medalha Guardiã T9',  'ACCESSORY', 'LEGENDARY', 40, 0, 25, 'GUARDIAN', 9,  true),
('Medalha Guardiã T10', 'ACCESSORY', 'LEGENDARY', 44, 0, 28, 'GUARDIAN', 10, true);

-- ============================================================
-- SET VITALIDADE (escala HP)
-- ============================================================
-- Arma: HP=8, ATK=3
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Cetro da Vitalidade T1',  'WEAPON', 'COMMON',     8,  3, 0, 'VITALITY', 1,  true),
('Cetro da Vitalidade T2',  'WEAPON', 'COMMON',    12,  5, 0, 'VITALITY', 2,  true),
('Cetro da Vitalidade T3',  'WEAPON', 'RARE',      16,  6, 0, 'VITALITY', 3,  true),
('Cetro da Vitalidade T4',  'WEAPON', 'RARE',      20,  8, 0, 'VITALITY', 4,  true),
('Cetro da Vitalidade T5',  'WEAPON', 'EPIC',      24,  9, 0, 'VITALITY', 5,  true),
('Cetro da Vitalidade T6',  'WEAPON', 'EPIC',      28, 11, 0, 'VITALITY', 6,  true),
('Cetro da Vitalidade T7',  'WEAPON', 'LEGENDARY', 32, 12, 0, 'VITALITY', 7,  true),
('Cetro da Vitalidade T8',  'WEAPON', 'LEGENDARY', 36, 14, 0, 'VITALITY', 8,  true),
('Cetro da Vitalidade T9',  'WEAPON', 'LEGENDARY', 40, 15, 0, 'VITALITY', 9,  true),
('Cetro da Vitalidade T10', 'WEAPON', 'LEGENDARY', 44, 17, 0, 'VITALITY', 10, true);

-- Armadura: HP=20, DEF=4
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Vestes da Vitalidade T1',  'ARMOR', 'COMMON',     20, 0,  4, 'VITALITY', 1,  true),
('Vestes da Vitalidade T2',  'ARMOR', 'COMMON',     30, 0,  6, 'VITALITY', 2,  true),
('Vestes da Vitalidade T3',  'ARMOR', 'RARE',       40, 0,  8, 'VITALITY', 3,  true),
('Vestes da Vitalidade T4',  'ARMOR', 'RARE',       50, 0, 10, 'VITALITY', 4,  true),
('Vestes da Vitalidade T5',  'ARMOR', 'EPIC',       60, 0, 12, 'VITALITY', 5,  true),
('Vestes da Vitalidade T6',  'ARMOR', 'EPIC',       70, 0, 14, 'VITALITY', 6,  true),
('Vestes da Vitalidade T7',  'ARMOR', 'LEGENDARY',  80, 0, 16, 'VITALITY', 7,  true),
('Vestes da Vitalidade T8',  'ARMOR', 'LEGENDARY',  90, 0, 18, 'VITALITY', 8,  true),
('Vestes da Vitalidade T9',  'ARMOR', 'LEGENDARY', 100, 0, 20, 'VITALITY', 9,  true),
('Vestes da Vitalidade T10', 'ARMOR', 'LEGENDARY', 110, 0, 22, 'VITALITY', 10, true);

-- Acessório: HP=15, DEF=2
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Emblema da Vitalidade T1',  'ACCESSORY', 'COMMON',    15, 0,  2, 'VITALITY', 1,  true),
('Emblema da Vitalidade T2',  'ACCESSORY', 'COMMON',    23, 0,  3, 'VITALITY', 2,  true),
('Emblema da Vitalidade T3',  'ACCESSORY', 'RARE',      30, 0,  4, 'VITALITY', 3,  true),
('Emblema da Vitalidade T4',  'ACCESSORY', 'RARE',      38, 0,  5, 'VITALITY', 4,  true),
('Emblema da Vitalidade T5',  'ACCESSORY', 'EPIC',      45, 0,  6, 'VITALITY', 5,  true),
('Emblema da Vitalidade T6',  'ACCESSORY', 'EPIC',      53, 0,  7, 'VITALITY', 6,  true),
('Emblema da Vitalidade T7',  'ACCESSORY', 'LEGENDARY', 60, 0,  8, 'VITALITY', 7,  true),
('Emblema da Vitalidade T8',  'ACCESSORY', 'LEGENDARY', 68, 0,  9, 'VITALITY', 8,  true),
('Emblema da Vitalidade T9',  'ACCESSORY', 'LEGENDARY', 75, 0, 10, 'VITALITY', 9,  true),
('Emblema da Vitalidade T10', 'ACCESSORY', 'LEGENDARY', 83, 0, 11, 'VITALITY', 10, true);

-- ============================================================
-- SET EQUILIBRADO (escala TUDO)
-- Base: Arma HP=4,ATK=4,DEF=4 / Armadura HP=10,ATK=3,DEF=6 / Acessório HP=5,ATK=4,DEF=4
-- ============================================================
-- Arma: HP=4, ATK=4, DEF=4
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Lâmina do Equilíbrio T1',  'WEAPON', 'COMMON',     4,  4,  4, 'BALANCED', 1,  true),
('Lâmina do Equilíbrio T2',  'WEAPON', 'COMMON',     6,  6,  6, 'BALANCED', 2,  true),
('Lâmina do Equilíbrio T3',  'WEAPON', 'RARE',       8,  8,  8, 'BALANCED', 3,  true),
('Lâmina do Equilíbrio T4',  'WEAPON', 'RARE',      10, 10, 10, 'BALANCED', 4,  true),
('Lâmina do Equilíbrio T5',  'WEAPON', 'EPIC',      12, 12, 12, 'BALANCED', 5,  true),
('Lâmina do Equilíbrio T6',  'WEAPON', 'EPIC',      14, 14, 14, 'BALANCED', 6,  true),
('Lâmina do Equilíbrio T7',  'WEAPON', 'LEGENDARY', 16, 16, 16, 'BALANCED', 7,  true),
('Lâmina do Equilíbrio T8',  'WEAPON', 'LEGENDARY', 18, 18, 18, 'BALANCED', 8,  true),
('Lâmina do Equilíbrio T9',  'WEAPON', 'LEGENDARY', 20, 20, 20, 'BALANCED', 9,  true),
('Lâmina do Equilíbrio T10', 'WEAPON', 'LEGENDARY', 22, 22, 22, 'BALANCED', 10, true);

-- Armadura: HP=10, ATK=3, DEF=6
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Armadura do Equilíbrio T1',  'ARMOR', 'COMMON',    10,  3,  6, 'BALANCED', 1,  true),
('Armadura do Equilíbrio T2',  'ARMOR', 'COMMON',    15,  5,  9, 'BALANCED', 2,  true),
('Armadura do Equilíbrio T3',  'ARMOR', 'RARE',      20,  6, 12, 'BALANCED', 3,  true),
('Armadura do Equilíbrio T4',  'ARMOR', 'RARE',      25,  8, 15, 'BALANCED', 4,  true),
('Armadura do Equilíbrio T5',  'ARMOR', 'EPIC',      30,  9, 18, 'BALANCED', 5,  true),
('Armadura do Equilíbrio T6',  'ARMOR', 'EPIC',      35, 11, 21, 'BALANCED', 6,  true),
('Armadura do Equilíbrio T7',  'ARMOR', 'LEGENDARY', 40, 12, 24, 'BALANCED', 7,  true),
('Armadura do Equilíbrio T8',  'ARMOR', 'LEGENDARY', 45, 14, 27, 'BALANCED', 8,  true),
('Armadura do Equilíbrio T9',  'ARMOR', 'LEGENDARY', 50, 15, 30, 'BALANCED', 9,  true),
('Armadura do Equilíbrio T10', 'ARMOR', 'LEGENDARY', 55, 17, 33, 'BALANCED', 10, true);

-- Acessório: HP=5, ATK=4, DEF=4
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Símbolo do Equilíbrio T1',  'ACCESSORY', 'COMMON',     5,  4,  4, 'BALANCED', 1,  true),
('Símbolo do Equilíbrio T2',  'ACCESSORY', 'COMMON',     8,  6,  6, 'BALANCED', 2,  true),
('Símbolo do Equilíbrio T3',  'ACCESSORY', 'RARE',      10,  8,  8, 'BALANCED', 3,  true),
('Símbolo do Equilíbrio T4',  'ACCESSORY', 'RARE',      13, 10, 10, 'BALANCED', 4,  true),
('Símbolo do Equilíbrio T5',  'ACCESSORY', 'EPIC',      15, 12, 12, 'BALANCED', 5,  true),
('Símbolo do Equilíbrio T6',  'ACCESSORY', 'EPIC',      18, 14, 14, 'BALANCED', 6,  true),
('Símbolo do Equilíbrio T7',  'ACCESSORY', 'LEGENDARY', 20, 16, 16, 'BALANCED', 7,  true),
('Símbolo do Equilíbrio T8',  'ACCESSORY', 'LEGENDARY', 23, 18, 18, 'BALANCED', 8,  true),
('Símbolo do Equilíbrio T9',  'ACCESSORY', 'LEGENDARY', 25, 20, 20, 'BALANCED', 9,  true),
('Símbolo do Equilíbrio T10', 'ACCESSORY', 'LEGENDARY', 28, 22, 22, 'BALANCED', 10, true);
