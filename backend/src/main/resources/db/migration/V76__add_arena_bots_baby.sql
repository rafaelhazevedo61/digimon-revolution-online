-- Bots de arena para os stages iniciais (BABY / BABY_II), já que novos jogadores começam em BABY
-- e a regra de matchmaking só permite mesmo stage ou adjacente.

INSERT INTO digimons
    (id, player_id, name, type, stage, level, experience,
     hp, attack, defense, iv_hp, iv_attack, iv_defense,
     created_at, rarity, personality, energy, max_energy, last_energy_update,
     bits, rebirth_count, status, grade, arena_rating, arena_wins, arena_losses, is_bot)
VALUES
    -- BABY (hp 40 / atk 10 / def 8)
    ('b0700000-0000-0000-0000-000000000021', 'b0700000-0000-0000-0000-0000000000c0', 'Botamon', 'DATA', 'BABY', 3, 0, 40, 10, 8, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000022', 'b0700000-0000-0000-0000-0000000000c0', 'Punimon', 'DATA', 'BABY', 3, 0, 40, 10, 8, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000023', 'b0700000-0000-0000-0000-0000000000c0', 'Poyomon', 'DATA', 'BABY', 4, 0, 40, 10, 8, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000024', 'b0700000-0000-0000-0000-0000000000c0', 'Pabumon', 'DATA', 'BABY', 4, 0, 40, 10, 8, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000025', 'b0700000-0000-0000-0000-0000000000c0', 'Yuramon', 'DATA', 'BABY', 5, 0, 40, 10, 8, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 20, 20, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE),

    -- BABY_II (hp 65 / atk 18 / def 14)
    ('b0700000-0000-0000-0000-000000000026', 'b0700000-0000-0000-0000-0000000000c0', 'Koromon',     'DATA', 'BABY_II', 6, 0, 65, 18, 14, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 22, 22, NOW(), 0, 0, 'ACTIVE', 'C',  860, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000027', 'b0700000-0000-0000-0000-0000000000c0', 'Tsunomon',    'DATA', 'BABY_II', 6, 0, 65, 18, 14, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 22, 22, NOW(), 0, 0, 'ACTIVE', 'C',  930, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000028', 'b0700000-0000-0000-0000-0000000000c0', 'Tokomon',     'DATA', 'BABY_II', 7, 0, 65, 18, 14, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 22, 22, NOW(), 0, 0, 'ACTIVE', 'C', 1000, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-000000000029', 'b0700000-0000-0000-0000-0000000000c0', 'Tanemon',     'DATA', 'BABY_II', 7, 0, 65, 18, 14, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 22, 22, NOW(), 0, 0, 'ACTIVE', 'C', 1070, 0, 0, TRUE),
    ('b0700000-0000-0000-0000-00000000002a', 'b0700000-0000-0000-0000-0000000000c0', 'Motimon',     'DATA', 'BABY_II', 8, 0, 65, 18, 14, 50, 50, 50, NOW(), 'COMMON', 'DURABLE', 22, 22, NOW(), 0, 0, 'ACTIVE', 'C', 1140, 0, 0, TRUE);
