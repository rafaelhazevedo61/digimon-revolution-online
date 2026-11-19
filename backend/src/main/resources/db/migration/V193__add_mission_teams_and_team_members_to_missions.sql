CREATE TABLE mission_teams (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    name VARCHAR(40) NOT NULL,
    digimon_1_id UUID NOT NULL,
    digimon_2_id UUID NOT NULL,
    digimon_3_id UUID NOT NULL,
    captain_digimon_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT mission_team_distinct_digimons CHECK (
        digimon_1_id <> digimon_2_id
        AND digimon_1_id <> digimon_3_id
        AND digimon_2_id <> digimon_3_id
    ),
    CONSTRAINT mission_team_captain_member CHECK (
        captain_digimon_id = digimon_1_id
        OR captain_digimon_id = digimon_2_id
        OR captain_digimon_id = digimon_3_id
    )
);

CREATE INDEX idx_mission_teams_player_id ON mission_teams(player_id);

ALTER TABLE mission_instances
    ADD COLUMN team_id UUID,
    ADD COLUMN digimon_2_id UUID,
    ADD COLUMN digimon_3_id UUID;

CREATE INDEX idx_mission_instances_team_id ON mission_instances(team_id);
CREATE INDEX idx_mission_instances_digimon_2_id ON mission_instances(digimon_2_id);
CREATE INDEX idx_mission_instances_digimon_3_id ON mission_instances(digimon_3_id);
