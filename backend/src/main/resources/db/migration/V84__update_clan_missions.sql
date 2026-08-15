UPDATE clan_missions
SET target_value = 1
WHERE code = 'CLAN_DAILY_BOSSES';

INSERT INTO clan_missions (id, code, title, description, objective_type, target_value, min_honor_marks_reward, max_honor_marks_reward, clan_xp_reward, min_clan_level)
VALUES (
    gen_random_uuid(),
    'CLAN_DAILY_ARENA_DUELS',
    'Duelos na Arena',
    'Duelar na arena contra outros jogadores, ganhando ou perdendo.',
    'ARENA_DUELS',
    5,
    15,
    30,
    60,
    1
);
