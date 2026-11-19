CREATE TABLE clan_level_config (
    level INT PRIMARY KEY,
    xp_required INT NOT NULL,
    max_members_bonus INT NOT NULL
);

-- xp_required = total acumulado para alcançar o nível
-- max_members_bonus = bônus de vagas conquistado ao alcançar o nível (somado ao limite base)
INSERT INTO clan_level_config (level, xp_required, max_members_bonus) VALUES
(1, 0, 0),
(2, 1000, 1),
(3, 2500, 1),
(4, 5000, 1),
(5, 10000, 2),
(6, 18000, 1),
(7, 30000, 1),
(8, 48000, 1),
(9, 72000, 1),
(10, 105000, 1),
(11, 150000, 1),
(12, 205000, 1),
(13, 275000, 1),
(14, 360000, 1),
(15, 465000, 1),
(16, 590000, 1),
(17, 740000, 1),
(18, 920000, 1),
(19, 1140000, 1),
(20, 1400000, 1);
