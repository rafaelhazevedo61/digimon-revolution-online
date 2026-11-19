ALTER TABLE digimons
ADD COLUMN grade VARCHAR(10);

UPDATE digimons
SET grade = CASE
    WHEN iv_hp = 100
     AND iv_attack = 100
     AND iv_defense = 100
        THEN 'SSS'

    WHEN (
        CASE WHEN iv_hp = 100 THEN 1 ELSE 0 END +
        CASE WHEN iv_attack = 100 THEN 1 ELSE 0 END +
        CASE WHEN iv_defense = 100 THEN 1 ELSE 0 END
    ) = 2
        THEN 'SS'

    WHEN (
        CASE WHEN iv_hp = 100 THEN 1 ELSE 0 END +
        CASE WHEN iv_attack = 100 THEN 1 ELSE 0 END +
        CASE WHEN iv_defense = 100 THEN 1 ELSE 0 END
    ) = 1
        THEN 'S'

    WHEN ((iv_hp + iv_attack + iv_defense) / 3) >= 85
        THEN 'A'

    WHEN ((iv_hp + iv_attack + iv_defense) / 3) >= 70
        THEN 'B'

    WHEN ((iv_hp + iv_attack + iv_defense) / 3) >= 55
        THEN 'C'

    WHEN ((iv_hp + iv_attack + iv_defense) / 3) >= 40
        THEN 'D'

    ELSE 'E'
END
WHERE grade IS NULL;

ALTER TABLE digimons
ALTER COLUMN grade SET NOT NULL;