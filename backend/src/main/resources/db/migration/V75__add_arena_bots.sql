-- Bots de arena: preenchem o lobby quando há poucos oponentes reais.
-- Bots têm rating fixo (não alterado pelas partidas) e ficam fora dos rankings gerais.

ALTER TABLE digimons ADD COLUMN is_bot BOOLEAN NOT NULL DEFAULT FALSE;

-- Jogador de sistema dono de todos os bots (não é usado para login).
INSERT INTO players (id, username, email, password, created_at, has_selected_starter, user_type, max_digimon_slots, max_storage_slots)
VALUES ('b0700000-0000-0000-0000-0000000000c0', 'CPU', 'cpu@system.local', '!disabled!', NOW(), TRUE, 'PLAYER', 3, 50);

-- hp/atk/def por stage calibrados para produzir poder compatível com o combate existente.
INSERT INTO digimons
    (id, player_id, name, type, stage, level, experience,
     hp, attack, defense, iv_hp, iv_attack, iv_defense,
     created_at, rarity, personality, energy, max_energy, last_energy_update,
     bits, rebirth_count, status, grade, arena_rating, arena_wins, arena_losses, is_bot)
VALUES
    -- ROOKIE (hp 90 / atk 28 / def 22)
    ('b0700000-0000-0000-0000-000000000001', 'b0700000-0000-0000-0000-0000000000c0', 'Agumon',  'DATA', 'ROOKIE', 10, 0, 90, 28, 22, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000002', 'b0700000-0000-0000-0000-0000000000c0', 'Gabumon', 'DATA', 'ROOKIE', 10, 0, 90, 28, 22, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000003', 'b0700000-0000-0000-0000-0000000000c0', 'Veemon',  'DATA', 'ROOKIE', 12, 0, 90, 28, 22, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000004', 'b0700000-0000-0000-0000-0000000000c0', 'Guilmon', 'DATA', 'ROOKIE', 13, 0, 90, 28, 22, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000005', 'b0700000-0000-0000-0000-0000000000c0', 'Renamon', 'DATA', 'ROOKIE', 14, 0, 90, 28, 22, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE),

    -- CHAMPION (hp 150 / atk 42 / def 34)
    ('b0700000-0000-0000-0000-000000000006', 'b0700000-0000-0000-0000-0000000000c0', 'Greymon',      'DATA', 'CHAMPION', 20, 0, 150, 42, 34, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 25, 25, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000007', 'b0700000-0000-0000-0000-0000000000c0', 'Garurumon',    'DATA', 'CHAMPION', 20, 0, 150, 42, 34, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 25, 25, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000008', 'b0700000-0000-0000-0000-0000000000c0', 'Kabuterimon',  'DATA', 'CHAMPION', 22, 0, 150, 42, 34, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 25, 25, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000009', 'b0700000-0000-0000-0000-0000000000c0', 'Angemon',      'DATA', 'CHAMPION', 23, 0, 150, 42, 34, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 25, 25, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000000a', 'b0700000-0000-0000-0000-0000000000c0', 'Kyubimon',     'DATA', 'CHAMPION', 24, 0, 150, 42, 34, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 25, 25, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE),

    -- ULTIMATE (hp 230 / atk 64 / def 50)
    ('b0700000-0000-0000-0000-00000000000b', 'b0700000-0000-0000-0000-0000000000c0', 'MetalGreymon',   'DATA', 'ULTIMATE', 30, 0, 230, 64, 50, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 30, 30, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000000c', 'b0700000-0000-0000-0000-0000000000c0', 'WereGarurumon',  'DATA', 'ULTIMATE', 30, 0, 230, 64, 50, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 30, 30, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000000d', 'b0700000-0000-0000-0000-0000000000c0', 'MegaKabuterimon','DATA', 'ULTIMATE', 32, 0, 230, 64, 50, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 30, 30, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000000e', 'b0700000-0000-0000-0000-0000000000c0', 'MagnaAngemon',   'DATA', 'ULTIMATE', 33, 0, 230, 64, 50, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 30, 30, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000000f', 'b0700000-0000-0000-0000-0000000000c0', 'Taomon',         'DATA', 'ULTIMATE', 34, 0, 230, 64, 50, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 30, 30, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE),

    -- MEGA (hp 330 / atk 95 / def 76)
    ('b0700000-0000-0000-0000-000000000010', 'b0700000-0000-0000-0000-0000000000c0', 'WarGreymon',         'DATA', 'MEGA', 40, 0, 330, 95, 76, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 35, 35, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000011', 'b0700000-0000-0000-0000-0000000000c0', 'MetalGarurumon',     'DATA', 'MEGA', 40, 0, 330, 95, 76, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 35, 35, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000012', 'b0700000-0000-0000-0000-0000000000c0', 'HerculesKabuterimon','DATA', 'MEGA', 42, 0, 330, 95, 76, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 35, 35, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000013', 'b0700000-0000-0000-0000-0000000000c0', 'Seraphimon',         'DATA', 'MEGA', 43, 0, 330, 95, 76, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 35, 35, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000014', 'b0700000-0000-0000-0000-0000000000c0', 'Sakuyamon',          'DATA', 'MEGA', 44, 0, 330, 95, 76, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 35, 35, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE);

CREATE INDEX idx_digimons_is_bot ON digimons (is_bot);
