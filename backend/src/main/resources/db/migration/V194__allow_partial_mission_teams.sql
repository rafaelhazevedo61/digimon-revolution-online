ALTER TABLE mission_teams
    ALTER COLUMN digimon_2_id DROP NOT NULL,
    ALTER COLUMN digimon_3_id DROP NOT NULL;

COMMENT ON COLUMN mission_teams.digimon_2_id IS 'Optional second Digimon in the mission team';
COMMENT ON COLUMN mission_teams.digimon_3_id IS 'Optional third Digimon in the mission team';

ALTER TABLE mission_teams
    DROP CONSTRAINT IF EXISTS mission_team_distinct_digimons;

ALTER TABLE mission_teams
    ADD CONSTRAINT mission_team_distinct_digimons CHECK (
        (digimon_2_id IS NULL OR digimon_1_id <> digimon_2_id)
        AND (digimon_3_id IS NULL OR digimon_1_id <> digimon_3_id)
        AND (digimon_2_id IS NULL OR digimon_3_id IS NULL OR digimon_2_id <> digimon_3_id)
    );

ALTER TABLE mission_teams
    DROP CONSTRAINT IF EXISTS mission_team_captain_member;

ALTER TABLE mission_teams
    ADD CONSTRAINT mission_team_captain_member CHECK (
        captain_digimon_id = digimon_1_id
        OR captain_digimon_id = digimon_2_id
        OR captain_digimon_id = digimon_3_id
    );

COMMENT ON TABLE mission_teams IS 'Saved mission formations with one to three Digimons';
