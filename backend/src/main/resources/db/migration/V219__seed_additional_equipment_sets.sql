-- Novos sets de equipamentos: 5 sets x 3 slots x 10 tiers.
-- Progressão de tier: T1=1x, T2=1.5x, ... T10=5.5x sobre os atributos-base T1.
-- Raridade não faz parte desta definição: COMMON é o valor neutro do template;
-- a raridade da instância e seu multiplicador são aplicados em runtime.

-- ============================================================
-- SET OVERCLOCK
-- ============================================================
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Lâmina Overclock T1', 'WEAPON', 'COMMON', 3, 8, 0, 'OVERCLOCK', 1, true),
('Lâmina Overclock T2', 'WEAPON', 'COMMON', 5, 12, 0, 'OVERCLOCK', 2, true),
('Lâmina Overclock T3', 'WEAPON', 'COMMON', 6, 16, 0, 'OVERCLOCK', 3, true),
('Lâmina Overclock T4', 'WEAPON', 'COMMON', 8, 20, 0, 'OVERCLOCK', 4, true),
('Lâmina Overclock T5', 'WEAPON', 'COMMON', 9, 24, 0, 'OVERCLOCK', 5, true),
('Lâmina Overclock T6', 'WEAPON', 'COMMON', 11, 28, 0, 'OVERCLOCK', 6, true),
('Lâmina Overclock T7', 'WEAPON', 'COMMON', 12, 32, 0, 'OVERCLOCK', 7, true),
('Lâmina Overclock T8', 'WEAPON', 'COMMON', 14, 36, 0, 'OVERCLOCK', 8, true),
('Lâmina Overclock T9', 'WEAPON', 'COMMON', 15, 40, 0, 'OVERCLOCK', 9, true),
('Lâmina Overclock T10', 'WEAPON', 'COMMON', 17, 44, 0, 'OVERCLOCK', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Frame Overclock T1', 'ARMOR', 'COMMON', 10, 4, 0, 'OVERCLOCK', 1, true),
('Frame Overclock T2', 'ARMOR', 'COMMON', 15, 6, 0, 'OVERCLOCK', 2, true),
('Frame Overclock T3', 'ARMOR', 'COMMON', 20, 8, 0, 'OVERCLOCK', 3, true),
('Frame Overclock T4', 'ARMOR', 'COMMON', 25, 10, 0, 'OVERCLOCK', 4, true),
('Frame Overclock T5', 'ARMOR', 'COMMON', 30, 12, 0, 'OVERCLOCK', 5, true),
('Frame Overclock T6', 'ARMOR', 'COMMON', 35, 14, 0, 'OVERCLOCK', 6, true),
('Frame Overclock T7', 'ARMOR', 'COMMON', 40, 16, 0, 'OVERCLOCK', 7, true),
('Frame Overclock T8', 'ARMOR', 'COMMON', 45, 18, 0, 'OVERCLOCK', 8, true),
('Frame Overclock T9', 'ARMOR', 'COMMON', 50, 20, 0, 'OVERCLOCK', 9, true),
('Frame Overclock T10', 'ARMOR', 'COMMON', 55, 22, 0, 'OVERCLOCK', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Chip Overclock T1', 'ACCESSORY', 'COMMON', 5, 5, 0, 'OVERCLOCK', 1, true),
('Chip Overclock T2', 'ACCESSORY', 'COMMON', 8, 8, 0, 'OVERCLOCK', 2, true),
('Chip Overclock T3', 'ACCESSORY', 'COMMON', 10, 10, 0, 'OVERCLOCK', 3, true),
('Chip Overclock T4', 'ACCESSORY', 'COMMON', 13, 13, 0, 'OVERCLOCK', 4, true),
('Chip Overclock T5', 'ACCESSORY', 'COMMON', 15, 15, 0, 'OVERCLOCK', 5, true),
('Chip Overclock T6', 'ACCESSORY', 'COMMON', 18, 18, 0, 'OVERCLOCK', 6, true),
('Chip Overclock T7', 'ACCESSORY', 'COMMON', 20, 20, 0, 'OVERCLOCK', 7, true),
('Chip Overclock T8', 'ACCESSORY', 'COMMON', 23, 23, 0, 'OVERCLOCK', 8, true),
('Chip Overclock T9', 'ACCESSORY', 'COMMON', 25, 25, 0, 'OVERCLOCK', 9, true),
('Chip Overclock T10', 'ACCESSORY', 'COMMON', 28, 28, 0, 'OVERCLOCK', 10, true);

-- ============================================================
-- SET FIREWALL
-- ============================================================
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Lança Firewall T1', 'WEAPON', 'COMMON', 0, 2, 6, 'FIREWALL', 1, true),
('Lança Firewall T2', 'WEAPON', 'COMMON', 0, 3, 9, 'FIREWALL', 2, true),
('Lança Firewall T3', 'WEAPON', 'COMMON', 0, 4, 12, 'FIREWALL', 3, true),
('Lança Firewall T4', 'WEAPON', 'COMMON', 0, 5, 15, 'FIREWALL', 4, true),
('Lança Firewall T5', 'WEAPON', 'COMMON', 0, 6, 18, 'FIREWALL', 5, true),
('Lança Firewall T6', 'WEAPON', 'COMMON', 0, 7, 21, 'FIREWALL', 6, true),
('Lança Firewall T7', 'WEAPON', 'COMMON', 0, 8, 24, 'FIREWALL', 7, true),
('Lança Firewall T8', 'WEAPON', 'COMMON', 0, 9, 27, 'FIREWALL', 8, true),
('Lança Firewall T9', 'WEAPON', 'COMMON', 0, 10, 30, 'FIREWALL', 9, true),
('Lança Firewall T10', 'WEAPON', 'COMMON', 0, 11, 33, 'FIREWALL', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Couraça Firewall T1', 'ARMOR', 'COMMON', 10, 0, 9, 'FIREWALL', 1, true),
('Couraça Firewall T2', 'ARMOR', 'COMMON', 15, 0, 14, 'FIREWALL', 2, true),
('Couraça Firewall T3', 'ARMOR', 'COMMON', 20, 0, 18, 'FIREWALL', 3, true),
('Couraça Firewall T4', 'ARMOR', 'COMMON', 25, 0, 23, 'FIREWALL', 4, true),
('Couraça Firewall T5', 'ARMOR', 'COMMON', 30, 0, 27, 'FIREWALL', 5, true),
('Couraça Firewall T6', 'ARMOR', 'COMMON', 35, 0, 32, 'FIREWALL', 6, true),
('Couraça Firewall T7', 'ARMOR', 'COMMON', 40, 0, 36, 'FIREWALL', 7, true),
('Couraça Firewall T8', 'ARMOR', 'COMMON', 45, 0, 41, 'FIREWALL', 8, true),
('Couraça Firewall T9', 'ARMOR', 'COMMON', 50, 0, 45, 'FIREWALL', 9, true),
('Couraça Firewall T10', 'ARMOR', 'COMMON', 55, 0, 50, 'FIREWALL', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Núcleo Firewall T1', 'ACCESSORY', 'COMMON', 5, 0, 5, 'FIREWALL', 1, true),
('Núcleo Firewall T2', 'ACCESSORY', 'COMMON', 8, 0, 8, 'FIREWALL', 2, true),
('Núcleo Firewall T3', 'ACCESSORY', 'COMMON', 10, 0, 10, 'FIREWALL', 3, true),
('Núcleo Firewall T4', 'ACCESSORY', 'COMMON', 13, 0, 13, 'FIREWALL', 4, true),
('Núcleo Firewall T5', 'ACCESSORY', 'COMMON', 15, 0, 15, 'FIREWALL', 5, true),
('Núcleo Firewall T6', 'ACCESSORY', 'COMMON', 18, 0, 18, 'FIREWALL', 6, true),
('Núcleo Firewall T7', 'ACCESSORY', 'COMMON', 20, 0, 20, 'FIREWALL', 7, true),
('Núcleo Firewall T8', 'ACCESSORY', 'COMMON', 23, 0, 23, 'FIREWALL', 8, true),
('Núcleo Firewall T9', 'ACCESSORY', 'COMMON', 25, 0, 25, 'FIREWALL', 9, true),
('Núcleo Firewall T10', 'ACCESSORY', 'COMMON', 28, 0, 28, 'FIREWALL', 10, true);

-- ============================================================
-- SET DATA BREAKER
-- ============================================================
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Garra Data Breaker T1', 'WEAPON', 'COMMON', 0, 11, 0, 'DATA_BREAKER', 1, true),
('Garra Data Breaker T2', 'WEAPON', 'COMMON', 0, 17, 0, 'DATA_BREAKER', 2, true),
('Garra Data Breaker T3', 'WEAPON', 'COMMON', 0, 22, 0, 'DATA_BREAKER', 3, true),
('Garra Data Breaker T4', 'WEAPON', 'COMMON', 0, 28, 0, 'DATA_BREAKER', 4, true),
('Garra Data Breaker T5', 'WEAPON', 'COMMON', 0, 33, 0, 'DATA_BREAKER', 5, true),
('Garra Data Breaker T6', 'WEAPON', 'COMMON', 0, 39, 0, 'DATA_BREAKER', 6, true),
('Garra Data Breaker T7', 'WEAPON', 'COMMON', 0, 44, 0, 'DATA_BREAKER', 7, true),
('Garra Data Breaker T8', 'WEAPON', 'COMMON', 0, 50, 0, 'DATA_BREAKER', 8, true),
('Garra Data Breaker T9', 'WEAPON', 'COMMON', 0, 55, 0, 'DATA_BREAKER', 9, true),
('Garra Data Breaker T10', 'WEAPON', 'COMMON', 0, 61, 0, 'DATA_BREAKER', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Couraça Data Breaker T1', 'ARMOR', 'COMMON', 4, 4, 0, 'DATA_BREAKER', 1, true),
('Couraça Data Breaker T2', 'ARMOR', 'COMMON', 6, 6, 0, 'DATA_BREAKER', 2, true),
('Couraça Data Breaker T3', 'ARMOR', 'COMMON', 8, 8, 0, 'DATA_BREAKER', 3, true),
('Couraça Data Breaker T4', 'ARMOR', 'COMMON', 10, 10, 0, 'DATA_BREAKER', 4, true),
('Couraça Data Breaker T5', 'ARMOR', 'COMMON', 12, 12, 0, 'DATA_BREAKER', 5, true),
('Couraça Data Breaker T6', 'ARMOR', 'COMMON', 14, 14, 0, 'DATA_BREAKER', 6, true),
('Couraça Data Breaker T7', 'ARMOR', 'COMMON', 16, 16, 0, 'DATA_BREAKER', 7, true),
('Couraça Data Breaker T8', 'ARMOR', 'COMMON', 18, 18, 0, 'DATA_BREAKER', 8, true),
('Couraça Data Breaker T9', 'ARMOR', 'COMMON', 20, 20, 0, 'DATA_BREAKER', 9, true),
('Couraça Data Breaker T10', 'ARMOR', 'COMMON', 22, 22, 0, 'DATA_BREAKER', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Fragmento Data Breaker T1', 'ACCESSORY', 'COMMON', 0, 5, 0, 'DATA_BREAKER', 1, true),
('Fragmento Data Breaker T2', 'ACCESSORY', 'COMMON', 0, 8, 0, 'DATA_BREAKER', 2, true),
('Fragmento Data Breaker T3', 'ACCESSORY', 'COMMON', 0, 10, 0, 'DATA_BREAKER', 3, true),
('Fragmento Data Breaker T4', 'ACCESSORY', 'COMMON', 0, 13, 0, 'DATA_BREAKER', 4, true),
('Fragmento Data Breaker T5', 'ACCESSORY', 'COMMON', 0, 15, 0, 'DATA_BREAKER', 5, true),
('Fragmento Data Breaker T6', 'ACCESSORY', 'COMMON', 0, 18, 0, 'DATA_BREAKER', 6, true),
('Fragmento Data Breaker T7', 'ACCESSORY', 'COMMON', 0, 20, 0, 'DATA_BREAKER', 7, true),
('Fragmento Data Breaker T8', 'ACCESSORY', 'COMMON', 0, 23, 0, 'DATA_BREAKER', 8, true),
('Fragmento Data Breaker T9', 'ACCESSORY', 'COMMON', 0, 25, 0, 'DATA_BREAKER', 9, true),
('Fragmento Data Breaker T10', 'ACCESSORY', 'COMMON', 0, 28, 0, 'DATA_BREAKER', 10, true);

-- ============================================================
-- SET KERNEL
-- ============================================================
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Martelo Kernel T1', 'WEAPON', 'COMMON', 5, 2, 3, 'KERNEL', 1, true),
('Martelo Kernel T2', 'WEAPON', 'COMMON', 8, 3, 5, 'KERNEL', 2, true),
('Martelo Kernel T3', 'WEAPON', 'COMMON', 10, 4, 6, 'KERNEL', 3, true),
('Martelo Kernel T4', 'WEAPON', 'COMMON', 13, 5, 8, 'KERNEL', 4, true),
('Martelo Kernel T5', 'WEAPON', 'COMMON', 15, 6, 9, 'KERNEL', 5, true),
('Martelo Kernel T6', 'WEAPON', 'COMMON', 18, 7, 11, 'KERNEL', 6, true),
('Martelo Kernel T7', 'WEAPON', 'COMMON', 20, 8, 12, 'KERNEL', 7, true),
('Martelo Kernel T8', 'WEAPON', 'COMMON', 23, 9, 14, 'KERNEL', 8, true),
('Martelo Kernel T9', 'WEAPON', 'COMMON', 25, 10, 15, 'KERNEL', 9, true),
('Martelo Kernel T10', 'WEAPON', 'COMMON', 28, 11, 17, 'KERNEL', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Kernel Shell T1', 'ARMOR', 'COMMON', 17, 0, 6, 'KERNEL', 1, true),
('Kernel Shell T2', 'ARMOR', 'COMMON', 26, 0, 9, 'KERNEL', 2, true),
('Kernel Shell T3', 'ARMOR', 'COMMON', 34, 0, 12, 'KERNEL', 3, true),
('Kernel Shell T4', 'ARMOR', 'COMMON', 43, 0, 15, 'KERNEL', 4, true),
('Kernel Shell T5', 'ARMOR', 'COMMON', 51, 0, 18, 'KERNEL', 5, true),
('Kernel Shell T6', 'ARMOR', 'COMMON', 60, 0, 21, 'KERNEL', 6, true),
('Kernel Shell T7', 'ARMOR', 'COMMON', 68, 0, 24, 'KERNEL', 7, true),
('Kernel Shell T8', 'ARMOR', 'COMMON', 77, 0, 27, 'KERNEL', 8, true),
('Kernel Shell T9', 'ARMOR', 'COMMON', 85, 0, 30, 'KERNEL', 9, true),
('Kernel Shell T10', 'ARMOR', 'COMMON', 94, 0, 33, 'KERNEL', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Kernel Core T1', 'ACCESSORY', 'COMMON', 10, 0, 3, 'KERNEL', 1, true),
('Kernel Core T2', 'ACCESSORY', 'COMMON', 15, 0, 5, 'KERNEL', 2, true),
('Kernel Core T3', 'ACCESSORY', 'COMMON', 20, 0, 6, 'KERNEL', 3, true),
('Kernel Core T4', 'ACCESSORY', 'COMMON', 25, 0, 8, 'KERNEL', 4, true),
('Kernel Core T5', 'ACCESSORY', 'COMMON', 30, 0, 9, 'KERNEL', 5, true),
('Kernel Core T6', 'ACCESSORY', 'COMMON', 35, 0, 11, 'KERNEL', 6, true),
('Kernel Core T7', 'ACCESSORY', 'COMMON', 40, 0, 12, 'KERNEL', 7, true),
('Kernel Core T8', 'ACCESSORY', 'COMMON', 45, 0, 14, 'KERNEL', 8, true),
('Kernel Core T9', 'ACCESSORY', 'COMMON', 50, 0, 15, 'KERNEL', 9, true),
('Kernel Core T10', 'ACCESSORY', 'COMMON', 55, 0, 17, 'KERNEL', 10, true);

-- ============================================================
-- SET OVERLORD
-- ============================================================
INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Lâmina do Overlord T1', 'WEAPON', 'COMMON', 4, 6, 3, 'OVERLORD', 1, true),
('Lâmina do Overlord T2', 'WEAPON', 'COMMON', 6, 9, 5, 'OVERLORD', 2, true),
('Lâmina do Overlord T3', 'WEAPON', 'COMMON', 8, 12, 6, 'OVERLORD', 3, true),
('Lâmina do Overlord T4', 'WEAPON', 'COMMON', 10, 15, 8, 'OVERLORD', 4, true),
('Lâmina do Overlord T5', 'WEAPON', 'COMMON', 12, 18, 9, 'OVERLORD', 5, true),
('Lâmina do Overlord T6', 'WEAPON', 'COMMON', 14, 21, 11, 'OVERLORD', 6, true),
('Lâmina do Overlord T7', 'WEAPON', 'COMMON', 16, 24, 12, 'OVERLORD', 7, true),
('Lâmina do Overlord T8', 'WEAPON', 'COMMON', 18, 27, 14, 'OVERLORD', 8, true),
('Lâmina do Overlord T9', 'WEAPON', 'COMMON', 20, 30, 15, 'OVERLORD', 9, true),
('Lâmina do Overlord T10', 'WEAPON', 'COMMON', 22, 33, 17, 'OVERLORD', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Couraça do Overlord T1', 'ARMOR', 'COMMON', 11, 3, 6, 'OVERLORD', 1, true),
('Couraça do Overlord T2', 'ARMOR', 'COMMON', 17, 5, 9, 'OVERLORD', 2, true),
('Couraça do Overlord T3', 'ARMOR', 'COMMON', 22, 6, 12, 'OVERLORD', 3, true),
('Couraça do Overlord T4', 'ARMOR', 'COMMON', 28, 8, 15, 'OVERLORD', 4, true),
('Couraça do Overlord T5', 'ARMOR', 'COMMON', 33, 9, 18, 'OVERLORD', 5, true),
('Couraça do Overlord T6', 'ARMOR', 'COMMON', 39, 11, 21, 'OVERLORD', 6, true),
('Couraça do Overlord T7', 'ARMOR', 'COMMON', 44, 12, 24, 'OVERLORD', 7, true),
('Couraça do Overlord T8', 'ARMOR', 'COMMON', 50, 14, 27, 'OVERLORD', 8, true),
('Couraça do Overlord T9', 'ARMOR', 'COMMON', 55, 15, 30, 'OVERLORD', 9, true),
('Couraça do Overlord T10', 'ARMOR', 'COMMON', 61, 17, 33, 'OVERLORD', 10, true);

INSERT INTO equipment_templates (name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, set_code, tier, active) VALUES
('Coroa do Overlord T1', 'ACCESSORY', 'COMMON', 5, 5, 3, 'OVERLORD', 1, true),
('Coroa do Overlord T2', 'ACCESSORY', 'COMMON', 8, 8, 5, 'OVERLORD', 2, true),
('Coroa do Overlord T3', 'ACCESSORY', 'COMMON', 10, 10, 6, 'OVERLORD', 3, true),
('Coroa do Overlord T4', 'ACCESSORY', 'COMMON', 13, 13, 8, 'OVERLORD', 4, true),
('Coroa do Overlord T5', 'ACCESSORY', 'COMMON', 15, 15, 9, 'OVERLORD', 5, true),
('Coroa do Overlord T6', 'ACCESSORY', 'COMMON', 18, 18, 11, 'OVERLORD', 6, true),
('Coroa do Overlord T7', 'ACCESSORY', 'COMMON', 20, 20, 12, 'OVERLORD', 7, true),
('Coroa do Overlord T8', 'ACCESSORY', 'COMMON', 23, 23, 14, 'OVERLORD', 8, true),
('Coroa do Overlord T9', 'ACCESSORY', 'COMMON', 25, 25, 15, 'OVERLORD', 9, true),
('Coroa do Overlord T10', 'ACCESSORY', 'COMMON', 28, 28, 17, 'OVERLORD', 10, true);

-- Validação de cardinalidade e cobertura após o seed.
DO $$
DECLARE seeded_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO seeded_count FROM equipment_templates WHERE set_code IN ('OVERCLOCK', 'FIREWALL', 'DATA_BREAKER', 'KERNEL', 'OVERLORD') AND tier BETWEEN 1 AND 10;
    IF seeded_count <> 150 THEN
        RAISE EXCEPTION 'Expected 150 new equipment templates, found %', seeded_count;
    END IF;
END $$;
