-- Adiciona drop tipo EQUIPMENT_POOL para todos os bosses.
-- Ao dropar, o sistema seleciona 1 template aleatorio e rola a raridade
-- usando o perfil do tipo de boss (BOSS_NORMAL, BOSS_DAILY, BOSS_WEEKLY, BOSS_MONTHLY).

INSERT INTO boss_drops (boss_id, drop_type, chance, min_quantity, max_quantity)
SELECT id, 'EQUIPMENT_POOL', 25, 1, 1
FROM boss_definitions;
