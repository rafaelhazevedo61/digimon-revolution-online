ALTER TABLE boss_definitions
    ADD COLUMN world_attempt_chest_definition_id BIGINT REFERENCES chest_definitions(id),
    ADD COLUMN world_top_damage_chest_definition_id BIGINT REFERENCES chest_definitions(id),
    ADD COLUMN world_final_blow_chest_definition_id BIGINT REFERENCES chest_definitions(id);

CREATE INDEX idx_boss_definitions_world_attempt_chest_id
    ON boss_definitions (world_attempt_chest_definition_id);

CREATE INDEX idx_boss_definitions_world_top_damage_chest_id
    ON boss_definitions (world_top_damage_chest_definition_id);

CREATE INDEX idx_boss_definitions_world_final_blow_chest_id
    ON boss_definitions (world_final_blow_chest_definition_id);

UPDATE boss_definitions b
SET world_attempt_chest_definition_id = c.id
FROM chest_definitions c
WHERE b.code = 'WORLD_BOSS_APOCALYMON'
  AND c.code = 'CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT';

UPDATE boss_definitions b
SET world_top_damage_chest_definition_id = c.id
FROM chest_definitions c
WHERE b.code = 'WORLD_BOSS_APOCALYMON'
  AND c.code = 'CHEST_BOSS_WORLD_APOCALYMON_TOP_DAMAGE';

UPDATE boss_definitions b
SET world_final_blow_chest_definition_id = c.id
FROM chest_definitions c
WHERE b.code = 'WORLD_BOSS_APOCALYMON'
  AND c.code = 'CHEST_BOSS_WORLD_APOCALYMON_FINAL_BLOW';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM boss_definitions
        WHERE boss_type = 'WORLD'
          AND (
              world_attempt_chest_definition_id IS NULL
              OR world_top_damage_chest_definition_id IS NULL
              OR world_final_blow_chest_definition_id IS NULL
          )
    ) THEN
        RAISE EXCEPTION 'World Boss definitions must have attempt, top damage and final blow chests configured';
    END IF;
END $$;

ALTER TABLE boss_definitions
    ADD CONSTRAINT ck_world_boss_reward_chests
    CHECK (
        boss_type <> 'WORLD'
        OR (
            world_attempt_chest_definition_id IS NOT NULL
            AND world_top_damage_chest_definition_id IS NOT NULL
            AND world_final_blow_chest_definition_id IS NOT NULL
        )
    );

ALTER TABLE boss_definitions
    ADD CONSTRAINT ck_world_boss_reward_chests_distinct
    CHECK (
        boss_type <> 'WORLD'
        OR (
            world_attempt_chest_definition_id <> world_top_damage_chest_definition_id
            AND world_attempt_chest_definition_id <> world_final_blow_chest_definition_id
            AND world_top_damage_chest_definition_id <> world_final_blow_chest_definition_id
        )
    );

COMMENT ON COLUMN boss_definitions.world_attempt_chest_definition_id IS
    'Baú da recompensa por tentativa de Boss Mundial';
COMMENT ON COLUMN boss_definitions.world_top_damage_chest_definition_id IS
    'Baú da recompensa de maior dano acumulado de Boss Mundial';
COMMENT ON COLUMN boss_definitions.world_final_blow_chest_definition_id IS
    'Baú da recompensa do golpe final de Boss Mundial';
