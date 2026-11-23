-- Protótipo permanente por conta. Catálogo ajustável sem alterar a contagem.
CREATE TABLE achievement_definitions (
    code VARCHAR(60) PRIMARY KEY,
    category VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(400) NOT NULL,
    metric VARCHAR(40) NOT NULL CHECK (metric IN ('HATCHES','MISSIONS','SPECIES','STAGE','REBIRTHS','ARENA_WINS','WORLD_ATTACKS','RAID_ATTACKS','EQUIPPED','REFINEMENT','ASCENSION')),
    target BIGINT NOT NULL CHECK (target > 0),
    reward_bits INTEGER NOT NULL CHECK (reward_bits >= 0),
    reward_item_code VARCHAR(100) NOT NULL REFERENCES item_definitions(code),
    reward_quantity INTEGER NOT NULL CHECK (reward_quantity > 0),
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE player_achievements (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    achievement_code VARCHAR(60) NOT NULL REFERENCES achievement_definitions(code),
    progress BIGINT NOT NULL DEFAULT 0 CHECK (progress >= 0),
    completed_at TIMESTAMPTZ,
    claimed_at TIMESTAMPTZ,
    PRIMARY KEY (player_id, achievement_code),
    CHECK (claimed_at IS NULL OR completed_at IS NOT NULL)
);

INSERT INTO item_definitions (code, name, description, category, stackable, tradable, sellable, usable, max_stack, rarity, icon)
VALUES ('VIP_DAY_PASS', 'Passe VIP de 1 Dia',
        'Concede 24 horas de VIP ao usar. Se já estiver ativo, acrescenta 24 horas à validade atual.',
        'CONSUMABLE', TRUE, FALSE, FALSE, TRUE, 999, 'LEGENDARY', '✦');

INSERT INTO achievement_definitions
(code, category, name, description, metric, target, reward_bits, reward_item_code, reward_quantity, sort_order)
VALUES
('FIRST_HATCH', 'PRIMEIROS_PASSOS', 'Primeiro companheiro', 'Choque seu primeiro Digitama.', 'HATCHES', 1, 100, 'ENERGY_CELL', 1, 10),
('FIRST_MISSION', 'PRIMEIROS_PASSOS', 'Primeira expedição', 'Resgate sua primeira missão.', 'MISSIONS', 1, 100, 'ENERGY_CELL', 1, 20),
('FIRST_EQUIP', 'PRIMEIROS_PASSOS', 'Pronto para a aventura', 'Equipe uma peça em seu Digimon.', 'EQUIPPED', 1, 150, 'XP_DISC_5', 2, 30),
('HATCH_10', 'CRIACAO', 'Berçário digital I', 'Choque 10 Digitamas.', 'HATCHES', 10, 500, 'INCUBATOR_RARE', 1, 40),
('HATCH_50', 'CRIACAO', 'Berçário digital II', 'Choque 50 Digitamas.', 'HATCHES', 50, 2000, 'INCUBATOR_EPIC', 1, 50),
('HATCH_100', 'CRIACAO', 'Guardião do berçário', 'Choque 100 Digitamas.', 'HATCHES', 100, 5000, 'RARITY_REROLL', 1, 60),
('STAGE_BABY_II', 'EVOLUCAO', 'Os primeiros passos', 'Alcance Baby II ou um estágio superior.', 'STAGE', 1, 150, 'XP_DISC_5', 2, 70),
('STAGE_ROOKIE', 'EVOLUCAO', 'Um parceiro de verdade', 'Alcance Rookie ou um estágio superior.', 'STAGE', 2, 300, 'ENERGY_CELL', 2, 80),
('STAGE_CHAMPION', 'EVOLUCAO', 'Força de Champion', 'Alcance Champion ou um estágio superior.', 'STAGE', 3, 750, 'XP_DISC_10', 3, 90),
('STAGE_ULTIMATE', 'EVOLUCAO', 'Além dos limites', 'Alcance Ultimate ou um estágio superior.', 'STAGE', 4, 1500, 'VIP_ENERGY_CHARGE', 2, 100),
('STAGE_MEGA', 'EVOLUCAO', 'O despertar Mega', 'Alcance o estágio Mega.', 'STAGE', 5, 3000, 'VIP_DAY_PASS', 1, 110),
('REBIRTH_1', 'RENASCIMENTO', 'Um novo começo', 'Realize seu primeiro renascimento.', 'REBIRTHS', 1, 750, 'RARITY_PRESERVATION', 1, 120),
('REBIRTH_10', 'RENASCIMENTO', 'Dez novos destinos', 'Realize 10 renascimentos na conta.', 'REBIRTHS', 10, 3500, 'RARITY_REROLL', 1, 130),
('REBIRTH_25', 'RENASCIMENTO', 'O ciclo continua', 'Realize 25 renascimentos na conta.', 'REBIRTHS', 25, 7500, 'RARITY_PRESERVATION', 2, 140),
('SPECIES_10', 'COLECAO', 'Espaço para novos amigos', 'Registre 10 espécies diferentes na coleção.', 'SPECIES', 10, 500, 'STORAGE_SLOT_5', 1, 150),
('SPECIES_25', 'COLECAO', 'Olhar de colecionador', 'Registre 25 espécies diferentes na coleção.', 'SPECIES', 25, 1500, 'RARITY_REROLL', 1, 160),
('SPECIES_50', 'COLECAO', 'Uma família digital', 'Registre 50 espécies diferentes na coleção.', 'SPECIES', 50, 3000, 'STORAGE_SLOT_5', 1, 170),
('SPECIES_100', 'COLECAO', 'Enciclopédia viva', 'Registre 100 espécies diferentes na coleção.', 'SPECIES', 100, 6000, 'STORAGE_SLOT_5', 2, 180),
('MISSION_10', 'MISSOES', 'Explorador I', 'Resgate 10 missões, individuais ou em equipe.', 'MISSIONS', 10, 300, 'ENERGY_CELL', 2, 190),
('MISSION_100', 'MISSOES', 'Explorador II', 'Resgate 100 missões, incluindo resgates automáticos.', 'MISSIONS', 100, 1500, 'VIP_ENERGY_CHARGE', 2, 200),
('MISSION_500', 'MISSOES', 'Explorador III', 'Resgate 500 missões.', 'MISSIONS', 500, 5000, 'VIP_DAY_PASS', 1, 210),
('MISSION_1000', 'MISSOES', 'Lenda das expedições', 'Resgate 1.000 missões.', 'MISSIONS', 1000, 10000, 'INCUBATOR_LEGENDARY', 1, 220),
('ARENA_1', 'COMBATE', 'Primeira vitória', 'Vença um desafio iniciado por você na arena.', 'ARENA_WINS', 1, 250, 'ENERGY_CELL', 1, 230),
('ARENA_25', 'COMBATE', 'Competidor', 'Vença 25 desafios iniciados por você na arena.', 'ARENA_WINS', 25, 1500, 'XP_DISC_20', 3, 240),
('ARENA_100', 'COMBATE', 'Nome de respeito', 'Vença 100 desafios iniciados por você na arena.', 'ARENA_WINS', 100, 5000, 'RARITY_REROLL', 1, 250),
('WORLD_1', 'COMBATE', 'Na linha de frente', 'Realize um ataque válido ao Chefe Mundial.', 'WORLD_ATTACKS', 1, 250, 'ENERGY_CELL', 1, 260),
('WORLD_50', 'COMBATE', 'Presença constante', 'Realize 50 ataques ao Chefe Mundial.', 'WORLD_ATTACKS', 50, 2500, 'ASCENSION_CORE', 1, 270),
('WORLD_200', 'COMBATE', 'Defensor do Mundo Digital', 'Realize 200 ataques ao Chefe Mundial.', 'WORLD_ATTACKS', 200, 7500, 'VIP_ENERGY_CHARGE', 5, 280),
('RAID_10', 'CLA', 'Força em conjunto', 'Realize 10 ataques em incursões de clã.', 'RAID_ATTACKS', 10, 1000, 'VIP_ENERGY_CHARGE', 1, 290),
('RAID_50', 'CLA', 'Companheiro de batalha', 'Realize 50 ataques em incursões de clã.', 'RAID_ATTACKS', 50, 3000, 'ASCENSION_CORE', 1, 300),
('REFINE_5', 'EQUIPAMENTOS', 'Arsenal aprimorado', 'Tenha um equipamento com refinamento +5 ou superior.', 'REFINEMENT', 5, 1000, 'REFINEMENT_PROTECTION', 2, 310),
('REFINE_10', 'EQUIPAMENTOS', 'Obra de mestre', 'Tenha um equipamento com refinamento +10 ou superior.', 'REFINEMENT', 10, 3000, 'ASCENSION_CORE', 1, 320),
('ASCEND_1', 'EQUIPAMENTOS', 'Um novo patamar', 'Tenha um equipamento com uma ascensão ou mais.', 'ASCENSION', 1, 1500, 'REFINEMENT_PROTECTION', 3, 330);

-- Instâncias resgatadas são removidas pelo cleanup. O contador precisa sobreviver a essa limpeza.
CREATE TABLE player_achievement_counters (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    metric VARCHAR(40) NOT NULL,
    value BIGINT NOT NULL DEFAULT 0 CHECK (value >= 0),
    PRIMARY KEY (player_id, metric)
);
INSERT INTO player_achievement_counters (player_id, metric, value)
SELECT player_id, 'MISSIONS', COUNT(*) FROM mission_instances
WHERE status = 'CLAIMED' GROUP BY player_id;

CREATE FUNCTION achievement_count_claimed_mission() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status = 'CLAIMED' THEN
        IF TG_OP = 'UPDATE' THEN
            IF OLD.status = 'CLAIMED' THEN
                RETURN NEW;
            END IF;
        END IF;
        INSERT INTO player_achievement_counters (player_id, metric, value)
        VALUES (NEW.player_id, 'MISSIONS', 1)
        ON CONFLICT (player_id, metric) DO UPDATE
        SET value = player_achievement_counters.value + 1;
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_mission_claimed
AFTER INSERT OR UPDATE OF status ON mission_instances
FOR EACH ROW EXECUTE FUNCTION achievement_count_claimed_mission();
