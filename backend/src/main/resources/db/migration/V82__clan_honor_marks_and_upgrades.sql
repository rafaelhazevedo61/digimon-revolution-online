ALTER TABLE clans
    ADD COLUMN honor_marks INT NOT NULL DEFAULT 0;

ALTER TABLE clans
    DROP COLUMN IF EXISTS bought_slots;

CREATE TABLE clan_upgrade_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(280),
    unlocked_at_clan_level INT NOT NULL,
    max_level INT NOT NULL DEFAULT 10,
    base_honor_marks_cost INT NOT NULL,
    cost_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.5,
    effect_per_level DECIMAL(5,4) NOT NULL,
    effect_type VARCHAR(30) NOT NULL,
    stat VARCHAR(30)
);

CREATE TABLE clan_upgrade_purchases (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL REFERENCES clans(id),
    upgrade_code VARCHAR(30) NOT NULL REFERENCES clan_upgrade_types(code),
    level INT NOT NULL DEFAULT 0,
    total_spent_honor_marks INT NOT NULL DEFAULT 0,
    UNIQUE (clan_id, upgrade_code)
);

INSERT INTO clan_upgrade_types (code, name, description, unlocked_at_clan_level, max_level, base_honor_marks_cost, cost_multiplier, effect_per_level, effect_type, stat) VALUES
('MEMBER_CAPACITY', 'Capacidade de Membros', 'Aumenta o limite de membros do clã.', 1, 10, 100, 2.0, 0, 'CAPACITY', 'capacity'),
('ATTACK_BONUS', 'Bônus de Ataque', 'Aumenta o ATK do Digimon ativo em 1% por nível.', 2, 10, 150, 1.8, 0.01, 'ATTRIBUTE', 'attack'),
('DEFENSE_BONUS', 'Bônus de Defesa', 'Aumenta a DEF do Digimon ativo em 1% por nível.', 3, 10, 150, 1.8, 0.01, 'ATTRIBUTE', 'defense'),
('HP_BONUS', 'Bônus de HP', 'Aumenta o HP do Digimon ativo em 1% por nível.', 4, 10, 150, 1.8, 0.01, 'ATTRIBUTE', 'hp'),
('HONOR_MARKS_BONUS', 'Prosperidade do Clã', 'Aumenta o ganho de Honor Marks em missões de clã em 2% por nível.', 5, 10, 200, 1.9, 0.02, 'HONOR_MARKS', 'honor_marks'),
('MAX_ENERGY_BONUS', 'Reservas de Energia', 'Aumenta a energia máxima do Digimon ativo em 1 por nível.', 6, 10, 180, 1.8, 1, 'ENERGY', 'energy'),
('MISSION_BITS_BONUS', 'Fortuna em Missões', 'Aumenta bits ganhos em missões normais em 1% por nível.', 7, 10, 200, 1.9, 0.01, 'RESOURCE', 'bits'),
('MISSION_XP_BONUS', 'Sabedoria em Missões', 'Aumenta XP ganho em missões normais em 1% por nível.', 8, 10, 200, 1.9, 0.01, 'RESOURCE', 'xp'),
('ENERGY_COST_REDUCTION', 'Eficiência de Clã', 'Reduz o custo de energia em missões/bosses. Começa em -5% no nível 0 e sobe -1% por nível.', 9, 10, 250, 2.0, 0.01, 'COST_REDUCTION', 'energy'),
('BOSS_DROP_BONUS', 'Sorte do Clã', 'Aumenta chance/qualidade de drop de equipamentos em bosses em 1% por nível.', 10, 10, 300, 2.0, 0.01, 'DROP', 'equipment');

-- Mantém apenas níveis 1-10 para o clã (conforme proposta de nível máximo 10)
DELETE FROM clan_level_config WHERE level > 10;

-- Garante que nenhum clã fique acima do nível máximo permitido
UPDATE clans SET level = 10 WHERE level > 10;
