-- Snapshot dos 12 tópicos do guia na entrega desta conquista.
ALTER TABLE achievement_definitions DROP CONSTRAINT achievement_definitions_metric_check;
ALTER TABLE achievement_definitions ADD CONSTRAINT achievement_definitions_metric_check CHECK (metric IN ('AREA_FACTORIAL_TOWN','AREA_FREEZELAND','AREA_GEAR_SAVANNA','AREA_INFINITY_MOUNTAIN','AREA_NATIVE_FOREST','AREA_SERVER_DESERT','ARENA_WINS','ASCENSION','ASCENSIONS_DONE','BIRTH_ALL_PERFECT_IV','BIRTH_ELITE','BIRTH_EPIC','BIRTH_LEGENDARY','BIRTH_PERFECT_IV','BIRTH_RARE','BIRTH_TRAIT','BOSS_WINS','COMPLETE_LINES','DISTINCT_BOSSES','EQUIPPED','FULL_EQUIPPED','HATCHES','MASTERED_SPECIES','MISSIONS','RAID_ATTACKS','RAID_DEFEATS','REBIRTHS','REFINEMENT','REFINEMENT_SUCCESSES','SETS_ACTIVATED','SPECIES','SPECIES_BABY','SPECIES_CHAMPION','SPECIES_MEGA','SPECIES_ROOKIE','SPECIES_ULTIMATE','STAGE','TEAM_MISSIONS','WORLD_ATTACKS','WORLD_DEFEATS','GUIDE_READ_TOPICS'));

CREATE TRIGGER achievement_queue_guide_read AFTER INSERT ON trainer_guide_read_topics
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();

INSERT INTO achievement_definitions
(code, category, name, description, metric, target, reward_bits, reward_item_code, reward_quantity, sort_order, trainer_points)
VALUES ('TRAINER_GUIDE_COMPLETE', 'PRIMEIROS_PASSOS', 'Treinador bem informado',
        'Marque Entendi nos 12 conteúdos do Guia do Treinador.', 'GUIDE_READ_TOPICS', 12,
        500, 'VIP_ENERGY_CHARGE', 1, 35, 25);
-- O trigger existente de alteração de catálogo enfileira todas as contas, incluindo leituras anteriores.
-- Códigos elegíveis (devem coincidir com a métrica no backend): 'COLLECTION', 'ACHIEVEMENTS', 'ENERGY', 'INCUBATION', 'TEAMS', 'BOSSES', 'ARENA', 'TOWER', 'RANKING', 'FORGE', 'REBIRTH', 'VIP'.
