CREATE TABLE clan_missions (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(60) NOT NULL,
    description VARCHAR(280),
    objective_type VARCHAR(30) NOT NULL,
    target_value INT NOT NULL,
    min_honor_marks_reward INT NOT NULL,
    max_honor_marks_reward INT NOT NULL,
    clan_xp_reward INT NOT NULL,
    min_clan_level INT NOT NULL DEFAULT 1
);

CREATE TABLE player_clan_missions (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    clan_mission_id UUID NOT NULL REFERENCES clan_missions(id),
    clan_id UUID NOT NULL REFERENCES clans(id),
    progress INT NOT NULL DEFAULT 0,
    honor_marks_reward INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    accepted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX idx_player_clan_missions_player_id ON player_clan_missions(player_id);
CREATE INDEX idx_player_clan_missions_status ON player_clan_missions(status);

INSERT INTO clan_missions (id, code, title, description, objective_type, target_value, min_honor_marks_reward, max_honor_marks_reward, clan_xp_reward, min_clan_level) VALUES
(gen_random_uuid(), 'CLAN_DAILY_MISSIONS', 'Contribuição em Missões', 'Complete missões normais para contribuir com o clã.', 'MISSIONS_COMPLETED', 5, 10, 20, 50, 1),
(gen_random_uuid(), 'CLAN_DAILY_BOSSES', 'Caça a Bosses', 'Derrote bosses para fortalecer o clã.', 'BOSSES_DEFEATED', 3, 15, 30, 60, 1),
(gen_random_uuid(), 'CLAN_DAILY_ARENA', 'Vitórias na Arena', 'Vença combates na arena pelo clã.', 'ARENA_WINS', 3, 20, 40, 70, 2),
(gen_random_uuid(), 'CLAN_WEEKLY_REBIRTH', 'Rebirths Coletivos', 'Realize rebirths para impulsionar o clã.', 'REBIRTHS_DONE', 1, 30, 60, 100, 3);
