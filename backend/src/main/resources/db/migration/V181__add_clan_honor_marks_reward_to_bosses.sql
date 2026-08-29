ALTER TABLE boss_definitions
    ADD COLUMN clan_honor_marks_reward INT NOT NULL DEFAULT 0;

UPDATE boss_definitions
SET clan_honor_marks_reward = base_bits_reward
WHERE boss_type = 'CLAN';

COMMENT ON COLUMN boss_definitions.clan_honor_marks_reward IS
    'Quantidade base de Marcas de Honra concedida ao clã ao derrotar uma incursão';

ALTER TABLE boss_definitions
    ADD CONSTRAINT chk_boss_clan_honor_marks_reward_non_negative
    CHECK (clan_honor_marks_reward >= 0);
