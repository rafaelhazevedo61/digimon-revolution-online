-- Expansão incremental: mantém as 33 definições e todos os resgates existentes.
ALTER TABLE achievement_definitions DROP CONSTRAINT achievement_definitions_metric_check;
ALTER TABLE achievement_definitions ADD CONSTRAINT achievement_definitions_metric_check CHECK (metric IN ('AREA_FACTORIAL_TOWN','AREA_FREEZELAND','AREA_GEAR_SAVANNA','AREA_INFINITY_MOUNTAIN','AREA_NATIVE_FOREST','AREA_SERVER_DESERT','ARENA_WINS','ASCENSION','ASCENSIONS_DONE','BIRTH_ALL_PERFECT_IV','BIRTH_ELITE','BIRTH_EPIC','BIRTH_LEGENDARY','BIRTH_PERFECT_IV','BIRTH_RARE','BIRTH_TRAIT','BOSS_WINS','COMPLETE_LINES','DISTINCT_BOSSES','EQUIPPED','FULL_EQUIPPED','HATCHES','MASTERED_SPECIES','MISSIONS','RAID_ATTACKS','RAID_DEFEATS','REBIRTHS','REFINEMENT','REFINEMENT_SUCCESSES','SETS_ACTIVATED','SPECIES','SPECIES_BABY','SPECIES_CHAMPION','SPECIES_MEGA','SPECIES_ROOKIE','SPECIES_ULTIMATE','STAGE','TEAM_MISSIONS','WORLD_ATTACKS','WORLD_DEFEATS'));

CREATE TABLE achievement_birth_snapshots (
    digimon_id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    rarity VARCHAR(20) NOT NULL,
    iv_hp INTEGER NOT NULL, iv_attack INTEGER NOT NULL, iv_defense INTEGER NOT NULL,
    trait VARCHAR(40),
    born_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_achievement_birth_player ON achievement_birth_snapshots(player_id);
-- Sem FK ao Digimon: sacrificar ou excluir o parceiro não apaga o nascimento.
CREATE TABLE achievement_gameplay_events (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    metric VARCHAR(40) NOT NULL CHECK(metric IN ('FULL_EQUIPPED','SETS_ACTIVATED','REFINEMENT_SUCCESSES','ASCENSIONS_DONE')),
    event_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(player_id, metric, event_key)
);

CREATE TABLE achievement_completed_missions (
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    mission_id VARCHAR(80) NOT NULL,
    PRIMARY KEY(player_id, mission_id)
);
INSERT INTO achievement_completed_missions(player_id, mission_id)
SELECT player_id, mission_id FROM player_mission_progress WHERE completion_count > 0
UNION SELECT player_id, mission_id FROM mission_instances WHERE status = 'CLAIMED';

CREATE TABLE achievement_area_missions (
    area VARCHAR(40) NOT NULL,
    mission_id VARCHAR(80) NOT NULL,
    PRIMARY KEY(area, mission_id)
);
INSERT INTO achievement_area_missions(area, mission_id)
SELECT area, id FROM mission_definitions WHERE active = TRUE AND id <> 'MISSION_ADMIN';
-- Congela a composição das linhas elegíveis, sem requisitos móveis depois de updates de conteúdo.
CREATE TABLE achievement_line_species (
    line_id BIGINT NOT NULL,
    digimon_info_id BIGINT NOT NULL,
    PRIMARY KEY(line_id, digimon_info_id)
);
INSERT INTO achievement_line_species(line_id, digimon_info_id)
SELECT DISTINCT l.id, s.digimon_info_id FROM evolution_lines l
JOIN available_contents c ON c.id = l.content_id AND c.active = TRUE
JOIN evolution_line_steps s ON s.evolution_line_id = l.id
WHERE l.active = TRUE;

INSERT INTO player_achievement_counters(player_id, metric, value)
SELECT player_id, 'TEAM_MISSIONS', COUNT(*) FROM mission_instances
WHERE status = 'CLAIMED' AND team_id IS NOT NULL AND digimon_2_id IS NOT NULL AND digimon_3_id IS NOT NULL
GROUP BY player_id;

CREATE OR REPLACE FUNCTION achievement_count_claimed_mission() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> 'CLAIMED' THEN RETURN NEW; END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'CLAIMED' THEN RETURN NEW; END IF;
    END IF;
    INSERT INTO player_achievement_counters(player_id, metric, value) VALUES(NEW.player_id, 'MISSIONS', 1)
    ON CONFLICT(player_id, metric) DO UPDATE SET value = player_achievement_counters.value + 1;
    INSERT INTO achievement_completed_missions(player_id, mission_id) VALUES(NEW.player_id, NEW.mission_id)
    ON CONFLICT DO NOTHING;
    IF NEW.team_id IS NOT NULL AND NEW.digimon_2_id IS NOT NULL AND NEW.digimon_3_id IS NOT NULL THEN
        INSERT INTO player_achievement_counters(player_id, metric, value) VALUES(NEW.player_id, 'TEAM_MISSIONS', 1)
        ON CONFLICT(player_id, metric) DO UPDATE SET value = player_achievement_counters.value + 1;
    END IF;
    RETURN NEW;
END;
$$;

INSERT INTO achievement_definitions
(code, category, name, description, metric, target, reward_bits, reward_item_code, reward_quantity, sort_order)
VALUES
('HATCH_5', 'CRIACAO', 'Berçário em expansão — 5', 'Choque 5 Digitamas.', 'HATCHES', 5, 75, 'XP_DISC_10', 2, 340),
('HATCH_25', 'CRIACAO', 'Berçário em expansão — 25', 'Choque 25 Digitamas.', 'HATCHES', 25, 375, 'XP_DISC_10', 2, 350),
('HATCH_250', 'CRIACAO', 'Berçário em expansão — 250', 'Choque 250 Digitamas.', 'HATCHES', 250, 3750, 'XP_DISC_10', 2, 360),
('HATCH_500', 'CRIACAO', 'Berçário em expansão — 500', 'Choque 500 Digitamas.', 'HATCHES', 500, 7500, 'RARITY_REROLL', 1, 370),
('MISSION_25', 'MISSOES', 'Expedições sem fronteiras — 25', 'Resgate 25 missões.', 'MISSIONS', 25, 125, 'ENERGY_CELL', 2, 380),
('MISSION_50', 'MISSOES', 'Expedições sem fronteiras — 50', 'Resgate 50 missões.', 'MISSIONS', 50, 250, 'ENERGY_CELL', 2, 390),
('MISSION_250', 'MISSOES', 'Expedições sem fronteiras — 250', 'Resgate 250 missões.', 'MISSIONS', 250, 1250, 'VIP_ENERGY_CHARGE', 2, 400),
('MISSION_2500', 'MISSOES', 'Expedições sem fronteiras — 2500', 'Resgate 2500 missões.', 'MISSIONS', 2500, 12500, 'VIP_ENERGY_CHARGE', 4, 410),
('MISSION_5000', 'MISSOES', 'Expedições sem fronteiras — 5000', 'Resgate 5000 missões.', 'MISSIONS', 5000, 25000, 'VIP_ENERGY_CHARGE', 4, 420),
('REBIRTH_5', 'RENASCIMENTO', 'Novos ciclos — 5', 'Realize 5 renascimentos na conta.', 'REBIRTHS', 5, 500, 'XP_DISC_20', 2, 430),
('REBIRTH_50', 'RENASCIMENTO', 'Novos ciclos — 50', 'Realize 50 renascimentos na conta.', 'REBIRTHS', 50, 5000, 'XP_DISC_20', 2, 440),
('REBIRTH_100', 'RENASCIMENTO', 'Novos ciclos — 100', 'Realize 100 renascimentos na conta.', 'REBIRTHS', 100, 10000, 'RARITY_PRESERVATION', 1, 450),
('SPECIES_5', 'COLECAO', 'Descobertas digitais — 5', 'Registre 5 espécies diferentes.', 'SPECIES', 5, 100, 'XP_DISC_10', 2, 460),
('SPECIES_75', 'COLECAO', 'Descobertas digitais — 75', 'Registre 75 espécies diferentes.', 'SPECIES', 75, 1500, 'XP_DISC_10', 2, 470),
('SPECIES_150', 'COLECAO', 'Descobertas digitais — 150', 'Registre 150 espécies diferentes.', 'SPECIES', 150, 3000, 'RARITY_REROLL', 1, 480),
('ARENA_10', 'COMBATE', 'Vitórias na arena — 10', 'Vença 10 desafios iniciados por você na arena.', 'ARENA_WINS', 10, 150, 'ENERGY_CELL', 2, 490),
('ARENA_50', 'COMBATE', 'Vitórias na arena — 50', 'Vença 50 desafios iniciados por você na arena.', 'ARENA_WINS', 50, 750, 'ENERGY_CELL', 2, 500),
('ARENA_250', 'COMBATE', 'Vitórias na arena — 250', 'Vença 250 desafios iniciados por você na arena.', 'ARENA_WINS', 250, 3750, 'VIP_ENERGY_CHARGE', 2, 510),
('ARENA_500', 'COMBATE', 'Vitórias na arena — 500', 'Vença 500 desafios iniciados por você na arena.', 'ARENA_WINS', 500, 7500, 'VIP_ENERGY_CHARGE', 2, 520),
('WORLD_10', 'COMBATE', 'Vanguarda mundial — 10', 'Realize 10 ataques válidos ao Chefe Mundial.', 'WORLD_ATTACKS', 10, 100, 'ENERGY_CELL', 2, 530),
('WORLD_100', 'COMBATE', 'Vanguarda mundial — 100', 'Realize 100 ataques válidos ao Chefe Mundial.', 'WORLD_ATTACKS', 100, 1000, 'VIP_ENERGY_CHARGE', 2, 540),
('WORLD_500', 'COMBATE', 'Vanguarda mundial — 500', 'Realize 500 ataques válidos ao Chefe Mundial.', 'WORLD_ATTACKS', 500, 5000, 'VIP_ENERGY_CHARGE', 2, 550),
('RAID_1', 'CLA', 'Pelo clã — 1', 'Realize 1 ataques em incursões de clã.', 'RAID_ATTACKS', 1, 100, 'ENERGY_CELL', 1, 560),
('RAID_25', 'CLA', 'Pelo clã — 25', 'Realize 25 ataques em incursões de clã.', 'RAID_ATTACKS', 25, 375, 'ENERGY_CELL', 2, 570),
('RAID_100', 'CLA', 'Pelo clã — 100', 'Realize 100 ataques em incursões de clã.', 'RAID_ATTACKS', 100, 1500, 'VIP_ENERGY_CHARGE', 2, 580),
('RAID_250', 'CLA', 'Pelo clã — 250', 'Realize 250 ataques em incursões de clã.', 'RAID_ATTACKS', 250, 3750, 'VIP_ENERGY_CHARGE', 2, 590),
('BIRTH_RARE', 'RARIDADE', 'Um brilho diferente', 'Choque seu primeiro Digimon Raro.', 'BIRTH_RARE', 1, 300, 'ENERGY_CELL', 2, 600),
('BIRTH_EPIC', 'RARIDADE', 'Nascimento extraordinário', 'Choque seu primeiro Digimon Épico.', 'BIRTH_EPIC', 1, 750, 'XP_DISC_10', 2, 610),
('BIRTH_LEGENDARY', 'RARIDADE', 'Uma lenda nasceu', 'Choque seu primeiro Digimon Lendário.', 'BIRTH_LEGENDARY', 1, 2500, 'RARITY_PRESERVATION', 1, 620),
('BIRTH_ELITE_10', 'RARIDADE', 'Berçário de elite', 'Choque 10 Digimons Épicos ou Lendários.', 'BIRTH_ELITE', 10, 3000, 'VIP_ENERGY_CHARGE', 3, 630),
('BIRTH_PERFECT_IV', 'RARIDADE', 'Potencial perfeito', 'Choque um Digimon com pelo menos um IV 100.', 'BIRTH_PERFECT_IV', 1, 1000, 'XP_DISC_20', 2, 640),
('BIRTH_ALL_PERFECT_IV', 'RARIDADE', 'Perfeição digital', 'Choque um Digimon com os três IVs 100.', 'BIRTH_ALL_PERFECT_IV', 1, 5000, 'RARITY_REROLL', 1, 650),
('BIRTH_TRAIT_1', 'RARIDADE', 'Personalidade marcante', 'Choque seu primeiro Digimon com trait.', 'BIRTH_TRAIT', 1, 500, 'ENERGY_CELL', 2, 660),
('BIRTH_TRAIT_10', 'RARIDADE', 'Novos talentos', 'Choque 10 Digimons com trait.', 'BIRTH_TRAIT', 10, 2000, 'VIP_ENERGY_CHARGE', 2, 670),
('MASTERY_1', 'COLECAO', 'Muitas faces', 'Registre as quatro raridades de 1 espécie(s).', 'MASTERED_SPECIES', 1, 500, 'XP_DISC_20', 2, 680),
('MASTERY_5', 'COLECAO', 'Conhecimento aprofundado', 'Registre as quatro raridades de 5 espécie(s).', 'MASTERED_SPECIES', 5, 2500, 'XP_DISC_20', 2, 690),
('MASTERY_10', 'COLECAO', 'Especialista em espécies', 'Registre as quatro raridades de 10 espécie(s).', 'MASTERED_SPECIES', 10, 5000, 'XP_DISC_20', 2, 700),
('LINES_1', 'COLECAO', 'Do início ao fim', 'Registre todas as espécies de 1 linha(s) evolutiva(s).', 'COMPLETE_LINES', 1, 750, 'VIP_ENERGY_CHARGE', 1, 710),
('LINES_5', 'COLECAO', 'Caminhos conhecidos', 'Registre todas as espécies de 5 linha(s) evolutiva(s).', 'COMPLETE_LINES', 5, 3750, 'VIP_ENERGY_CHARGE', 3, 720),
('LINES_10', 'COLECAO', 'Arquivista digital', 'Registre todas as espécies de 10 linha(s) evolutiva(s).', 'COMPLETE_LINES', 10, 7500, 'VIP_ENERGY_CHARGE', 3, 730),
('COLLECTION_BABY_10', 'COLECAO', 'Pequenos companheiros', 'Registre 10 espécies diferentes do estágio BABY.', 'SPECIES_BABY', 10, 1500, 'XP_DISC_20', 2, 740),
('COLLECTION_ROOKIE_10', 'COLECAO', 'Novos aventureiros', 'Registre 10 espécies diferentes do estágio ROOKIE.', 'SPECIES_ROOKIE', 10, 1500, 'XP_DISC_20', 2, 750),
('COLLECTION_CHAMPION_10', 'COLECAO', 'Grandes guerreiros', 'Registre 10 espécies diferentes do estágio CHAMPION.', 'SPECIES_CHAMPION', 10, 1500, 'XP_DISC_20', 2, 760),
('COLLECTION_ULTIMATE_10', 'COLECAO', 'Poder extraordinário', 'Registre 10 espécies diferentes do estágio ULTIMATE.', 'SPECIES_ULTIMATE', 10, 1500, 'XP_DISC_20', 2, 770),
('COLLECTION_MEGA_10', 'COLECAO', 'Encontro de lendas', 'Registre 10 espécies diferentes do estágio MEGA.', 'SPECIES_MEGA', 10, 1500, 'XP_DISC_20', 2, 780),
('EXPLORE_NATIVE_FOREST', 'EXPLORACAO', 'Conhecedor de Floresta Nativa', 'Resgate ao menos uma vez todas as missões de Floresta Nativa incluídas nesta conquista.', 'AREA_NATIVE_FOREST', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'NATIVE_FOREST'), 2000, 'VIP_ENERGY_CHARGE', 2, 790),
('EXPLORE_GEAR_SAVANNA', 'EXPLORACAO', 'Conhecedor de Savana Gear', 'Resgate ao menos uma vez todas as missões de Savana Gear incluídas nesta conquista.', 'AREA_GEAR_SAVANNA', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'GEAR_SAVANNA'), 2000, 'VIP_ENERGY_CHARGE', 2, 800),
('EXPLORE_FACTORIAL_TOWN', 'EXPLORACAO', 'Conhecedor de Cidade Fatorial', 'Resgate ao menos uma vez todas as missões de Cidade Fatorial incluídas nesta conquista.', 'AREA_FACTORIAL_TOWN', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'FACTORIAL_TOWN'), 2000, 'VIP_ENERGY_CHARGE', 2, 810),
('EXPLORE_FREEZELAND', 'EXPLORACAO', 'Conhecedor de Freezeland', 'Resgate ao menos uma vez todas as missões de Freezeland incluídas nesta conquista.', 'AREA_FREEZELAND', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'FREEZELAND'), 2000, 'VIP_ENERGY_CHARGE', 2, 820),
('EXPLORE_SERVER_DESERT', 'EXPLORACAO', 'Conhecedor de Deserto do Server', 'Resgate ao menos uma vez todas as missões de Deserto do Server incluídas nesta conquista.', 'AREA_SERVER_DESERT', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'SERVER_DESERT'), 2000, 'VIP_ENERGY_CHARGE', 2, 830),
('EXPLORE_INFINITY_MOUNTAIN', 'EXPLORACAO', 'Conhecedor de Montanha Infinita', 'Resgate ao menos uma vez todas as missões de Montanha Infinita incluídas nesta conquista.', 'AREA_INFINITY_MOUNTAIN', (SELECT COUNT(*) FROM achievement_area_missions WHERE area = 'INFINITY_MOUNTAIN'), 2000, 'VIP_ENERGY_CHARGE', 2, 840),
('TEAM_1', 'EQUIPES', 'Melhor acompanhado', 'Resgate 1 missão(ões) com time completo de três Digimons.', 'TEAM_MISSIONS', 1, 200, 'ENERGY_CELL', 1, 850),
('TEAM_100', 'EQUIPES', 'Trabalho em equipe', 'Resgate 100 missão(ões) com time completo de três Digimons.', 'TEAM_MISSIONS', 100, 800, 'VIP_ENERGY_CHARGE', 2, 860),
('TEAM_500', 'EQUIPES', 'Esquadrão veterano', 'Resgate 500 missão(ões) com time completo de três Digimons.', 'TEAM_MISSIONS', 500, 4000, 'VIP_ENERGY_CHARGE', 2, 870),
('FULL_EQUIPPED', 'EQUIPAMENTOS', 'Pronto para o combate', 'Equipe arma, armadura e acessório no mesmo Digimon.', 'FULL_EQUIPPED', 1, 500, 'REFINEMENT_PROTECTION', 1, 880),
('SET_1', 'EQUIPAMENTOS', 'Sinergia de conjunto', 'Ative um bônus de conjunto com três peças.', 'SETS_ACTIVATED', 1, 1000, 'REFINEMENT_PROTECTION', 2, 890),
('SET_3', 'EQUIPAMENTOS', 'Arsenal diversificado', 'Ative três conjuntos diferentes com três peças cada.', 'SETS_ACTIVATED', 3, 3000, 'ASCENSION_CORE', 1, 900),
('REFINE_SUCCESS_1', 'EQUIPAMENTOS', 'Primeiro aprimoramento', 'Realize 1 refinamento(s) bem-sucedido(s).', 'REFINEMENT_SUCCESSES', 1, 250, 'ENERGY_CELL', 1, 910),
('REFINE_SUCCESS_50', 'EQUIPAMENTOS', 'Ferreiro dedicado', 'Realize 50 refinamento(s) bem-sucedido(s).', 'REFINEMENT_SUCCESSES', 50, 3000, 'REFINEMENT_PROTECTION', 3, 920),
('ASCENSION_DONE_1', 'EQUIPAMENTOS', 'Transformação de equipamento', 'Realize 1 ascensão(ões) de equipamento.', 'ASCENSIONS_DONE', 1, 1000, 'REFINEMENT_PROTECTION', 2, 930),
('ASCENSION_DONE_10', 'EQUIPAMENTOS', 'Artesão experiente', 'Realize 10 ascensão(ões) de equipamento.', 'ASCENSIONS_DONE', 10, 5000, 'ASCENSION_CORE', 2, 940),
('BOSS_WIN_1', 'CHEFES', 'Primeiro grande adversário', 'Vença 1 desafio(s) de chefes.', 'BOSS_WINS', 1, 250, 'ENERGY_CELL', 1, 950),
('BOSS_WIN_10', 'CHEFES', 'Caçador de chefes', 'Vença 10 desafio(s) de chefes.', 'BOSS_WINS', 10, 500, 'VIP_ENERGY_CHARGE', 2, 960),
('BOSS_WIN_100', 'CHEFES', 'Caçador veterano', 'Vença 100 desafio(s) de chefes.', 'BOSS_WINS', 100, 5000, 'VIP_ENERGY_CHARGE', 2, 970),
('BOSS_DISTINCT_5', 'CHEFES', 'Adversários conhecidos', 'Derrote cinco chefes diferentes.', 'DISTINCT_BOSSES', 5, 2000, 'XP_DISC_20', 3, 980),
('WORLD_PARTICIPATION', 'CHEFES', 'Unidos contra o impossível', 'Ataque um Chefe Mundial em um ciclo que termine em derrota do chefe.', 'WORLD_DEFEATS', 1, 1500, 'VIP_ENERGY_CHARGE', 2, 990),
('RAID_PARTICIPATION', 'CHEFES', 'Vitória do clã', 'Ataque um chefe de incursão em um ciclo que termine em derrota do chefe.', 'RAID_DEFEATS', 1, 1500, 'VIP_ENERGY_CHARGE', 2, 1000);
