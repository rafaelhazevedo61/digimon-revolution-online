UPDATE player_mission_progress
SET completion_count = LEAST(GREATEST(completion_count, 0), 100)
WHERE completion_count < 0 OR completion_count > 100;

ALTER TABLE player_mission_progress
    ADD CONSTRAINT ck_player_mission_progress_completion_count
    CHECK (completion_count BETWEEN 0 AND 100);
