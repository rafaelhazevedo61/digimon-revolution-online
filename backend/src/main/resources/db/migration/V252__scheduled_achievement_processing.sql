-- Fila coalescida por conta: eventos são capturados na transação original, sem recalcular metas.
CREATE TABLE achievement_pending_players (
    player_id UUID PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activity_version BIGINT NOT NULL DEFAULT 1,
    failures INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_achievement_pending_ready ON achievement_pending_players(available_at, requested_at, player_id);
CREATE TABLE achievement_processing_state (
    player_id UUID PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE FUNCTION achievement_enqueue_player(account_id UUID) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    IF account_id IS NULL THEN RETURN; END IF;
    INSERT INTO achievement_pending_players(player_id) VALUES(account_id)
    ON CONFLICT(player_id) DO UPDATE SET activity_version = achievement_pending_players.activity_version + 1;
    -- O UPDATE (em vez de DO NOTHING) espera o lock do processador. Se ele remover a linha,
    -- o evento concorrente insere outra pendência após o commit, sem perder a atividade.
    -- requested_at permanece o da primeira atividade para não penalizar contas muito ativas.
END;
$$;

CREATE FUNCTION achievement_queue_owner() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_birth AFTER INSERT ON achievement_birth_snapshots
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();
CREATE TRIGGER achievement_queue_gameplay AFTER INSERT ON achievement_gameplay_events
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();
CREATE TRIGGER achievement_queue_collection AFTER INSERT ON digimon_collection_entries
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();
CREATE TRIGGER achievement_queue_mission_coverage AFTER INSERT ON achievement_completed_missions
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();
CREATE TRIGGER achievement_queue_tutorial AFTER INSERT ON tutorial_progress
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();

CREATE FUNCTION achievement_queue_counter() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF NEW.value = OLD.value THEN RETURN NEW; END IF;
    END IF;
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_counter AFTER INSERT OR UPDATE OF value ON player_achievement_counters
FOR EACH ROW EXECUTE FUNCTION achievement_queue_counter();

CREATE FUNCTION achievement_queue_new_player() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM achievement_enqueue_player(NEW.id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_new_player AFTER INSERT ON players
FOR EACH ROW EXECUTE FUNCTION achievement_queue_new_player();

CREATE FUNCTION achievement_queue_digimon() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF (NEW.stage, NEW.status, NEW.player_id) IS NOT DISTINCT FROM (OLD.stage, OLD.status, OLD.player_id) THEN RETURN NEW; END IF;
    END IF;
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_digimon AFTER INSERT OR UPDATE OF stage, status, player_id ON digimons
FOR EACH ROW EXECUTE FUNCTION achievement_queue_digimon();

CREATE FUNCTION achievement_queue_hatch() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.source = 'DIRECT_HATCH' THEN PERFORM achievement_enqueue_player(NEW.player_id); END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_hatch AFTER INSERT ON digitama_history
FOR EACH ROW EXECUTE FUNCTION achievement_queue_hatch();

CREATE FUNCTION achievement_queue_incubation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> 'CLAIMED' THEN RETURN NEW; END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'CLAIMED' THEN RETURN NEW; END IF;
    END IF;
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_incubation AFTER INSERT OR UPDATE OF status ON incubations
FOR EACH ROW EXECUTE FUNCTION achievement_queue_incubation();

CREATE FUNCTION achievement_queue_arena() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.attacker_won THEN PERFORM achievement_enqueue_player(NEW.attacker_player_id); END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_arena AFTER INSERT ON arena_matches
FOR EACH ROW EXECUTE FUNCTION achievement_queue_arena();

CREATE FUNCTION achievement_queue_boss() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> 'VICTORY' THEN RETURN NEW; END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'VICTORY' THEN RETURN NEW; END IF;
    END IF;
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_boss AFTER INSERT OR UPDATE OF status ON boss_attempts
FOR EACH ROW EXECUTE FUNCTION achievement_queue_boss();
CREATE TRIGGER achievement_queue_world_hit AFTER INSERT ON world_boss_attacks
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();
CREATE TRIGGER achievement_queue_raid_hit AFTER INSERT ON clan_raid_attacks
FOR EACH ROW EXECUTE FUNCTION achievement_queue_owner();

CREATE FUNCTION achievement_queue_world_defeat() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> 'DEFEATED' THEN RETURN NEW; END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'DEFEATED' THEN RETURN NEW; END IF;
    END IF;
    INSERT INTO achievement_pending_players(player_id)
    SELECT DISTINCT player_id FROM world_boss_attacks WHERE world_boss_id = NEW.id AND damage > 0 ORDER BY player_id
    ON CONFLICT(player_id) DO UPDATE SET activity_version = achievement_pending_players.activity_version + 1;
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_world_defeat AFTER INSERT OR UPDATE OF status ON world_boss_instances
FOR EACH ROW EXECUTE FUNCTION achievement_queue_world_defeat();

CREATE FUNCTION achievement_queue_raid_defeat() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status <> 'DEFEATED' THEN RETURN NEW; END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'DEFEATED' THEN RETURN NEW; END IF;
    END IF;
    INSERT INTO achievement_pending_players(player_id)
    SELECT DISTINCT player_id FROM clan_raid_attacks WHERE clan_raid_id = NEW.id AND damage > 0 ORDER BY player_id
    ON CONFLICT(player_id) DO UPDATE SET activity_version = achievement_pending_players.activity_version + 1;
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_raid_defeat AFTER INSERT OR UPDATE OF status ON clan_raid_instances
FOR EACH ROW EXECUTE FUNCTION achievement_queue_raid_defeat();

-- Preserva marcos transitórios de uma peça, mesmo se for vendida/perdida antes do próximo lote.
CREATE FUNCTION achievement_queue_equipment() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF (NEW.player_id, NEW.refinement_level, NEW.ascension_level, NEW.equipped, NEW.digimon_id, NEW.set_code)
           IS NOT DISTINCT FROM
           (OLD.player_id, OLD.refinement_level, OLD.ascension_level, OLD.equipped, OLD.digimon_id, OLD.set_code) THEN RETURN NEW; END IF;
    END IF;
    IF NEW.player_id IS NULL THEN RETURN NEW; END IF;
    INSERT INTO player_achievement_counters(player_id, metric, value)
    VALUES (NEW.player_id, 'REFINEMENT', NEW.refinement_level), (NEW.player_id, 'ASCENSION', NEW.ascension_level)
    ON CONFLICT(player_id, metric) DO UPDATE SET value = GREATEST(player_achievement_counters.value, EXCLUDED.value)
    WHERE player_achievement_counters.value < EXCLUDED.value;
    PERFORM achievement_enqueue_player(NEW.player_id);
    RETURN NEW;
END;
$$;
CREATE TRIGGER achievement_queue_equipment AFTER INSERT OR UPDATE OF player_id, refinement_level, ascension_level, equipped, digimon_id, set_code ON inventory_equipments
FOR EACH ROW EXECUTE FUNCTION achievement_queue_equipment();

INSERT INTO player_achievement_counters(player_id, metric, value)
SELECT player_id, 'REFINEMENT', MAX(refinement_level) FROM inventory_equipments WHERE player_id IS NOT NULL GROUP BY player_id
UNION ALL SELECT player_id, 'ASCENSION', MAX(ascension_level) FROM inventory_equipments WHERE player_id IS NOT NULL GROUP BY player_id
ON CONFLICT(player_id, metric) DO UPDATE SET value = GREATEST(player_achievement_counters.value, EXCLUDED.value);

-- Uma alteração administrativa do catálogo precisa reavaliar as contas uma vez, não em cada tick.
CREATE FUNCTION achievement_queue_catalog_change() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO achievement_pending_players(player_id) SELECT id FROM players ORDER BY id
    ON CONFLICT(player_id) DO UPDATE SET activity_version = achievement_pending_players.activity_version + 1;
    RETURN NULL;
END;
$$;
CREATE TRIGGER achievement_queue_catalog_change AFTER INSERT OR UPDATE ON achievement_definitions
FOR EACH STATEMENT EXECUTE FUNCTION achievement_queue_catalog_change();

-- Carga inicial única; depois só contas afetadas por atividade/catálogo entram na fila.
INSERT INTO achievement_pending_players(player_id) SELECT id FROM players ORDER BY id
ON CONFLICT DO NOTHING;
