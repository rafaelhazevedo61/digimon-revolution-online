ALTER TABLE evolution_line_steps ADD COLUMN required_level INT NOT NULL DEFAULT 1;

UPDATE evolution_line_steps SET required_level = CASE
    WHEN stage = 'BABY_II'  THEN 10
    WHEN stage = 'ROOKIE'   THEN 15
    WHEN stage = 'CHAMPION' THEN 25
    WHEN stage = 'ULTIMATE' THEN 50
    WHEN stage = 'MEGA'     THEN 75
    ELSE 1
END;
